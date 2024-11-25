package physicsengine.physics;

import physicsengine.math.Vec3;
import physicsengine.physics.forces.GravityForce;
import physicsengine.physics.forces.SpringForce;
import physicsengine.physics.forces.rigidbody.AnchoredRigidBodySpringForce;
import physicsengine.physics.forces.rigidbody.RigidBodySpringForce;
import physicsengine.physics.particle.Particle;
import physicsengine.physics.particle.ParticleCollision;
import physicsengine.physics.rigidbody.CollisionData;
import physicsengine.physics.rigidbody.Constraint;
import physicsengine.physics.rigidbody.RigidBody;
import physicsengine.physics.rigidbody.RigidBodyCollision;
import physicsengine.physics.rigidbody.colliders.*;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private final List<PhysicsObject> physicsObjects = new ArrayList<>();
    private final List<RigidBody> rigidBodies = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Runnable> forceGenerators = new ArrayList<>();
    private final List<Constraint> constraints = new ArrayList<>();
    private final CollisionData collisionData = new CollisionData(128);

    public Scene() {
        this.forceGenerators.add(GravityForce::apply);
        this.forceGenerators.add(SpringForce::apply);
        this.forceGenerators.add(RigidBodySpringForce::apply);
        this.forceGenerators.add(AnchoredRigidBodySpringForce::apply);
    }

    public void add(PhysicsObject physicsObject) {
        this.physicsObjects.add(physicsObject);

        if (physicsObject instanceof RigidBody rigidBody) {
            this.rigidBodies.add(rigidBody);
        } else if (physicsObject instanceof Particle particle) {
            this.particles.add(particle);
        }
    }

    public void add(Constraint constraint) {
        this.constraints.add(constraint);
    }

    public List<PhysicsObject> getPhysicsObjects() {
        return this.physicsObjects;
    }

    public List<RigidBody> getRigidBodies() {
        return this.rigidBodies;
    }

    public List<Particle> getParticles() {
        return this.particles;
    }

    public List<Constraint> getConstraints() {
        return this.constraints;
    }

    public void update(double dt) {
        this.forceGenerators.forEach(Runnable::run);

        for (PhysicsObject physicsObject : this.physicsObjects) {
            physicsObject.integrate(dt);

            if (physicsObject instanceof RigidBody rigidBody) {
                rigidBody.collisionMarker = RigidBody.COLLISION_MARKER_NONE;
            }
        }

        this.collisionData.reset();
        int size = this.rigidBodies.size();

        label1: for (int i = 0; i < size - 1; ++i) {
            RigidBody rigidBody1 = this.rigidBodies.get(i);

            boolean flag = !rigidBody1.hasFiniteMass();

            for (int j = i + 1; j < size; ++j) {
                RigidBody rigidBody2 = this.rigidBodies.get(j);

                if (flag && !rigidBody2.hasFiniteMass()) continue;

                if (this.detectBroadCollision(rigidBody1, rigidBody2)) {
                    rigidBody1.collisionMarker = Math.max(rigidBody1.collisionMarker, RigidBody.COLLISION_MARKER_BROAD);
                    rigidBody2.collisionMarker = Math.max(rigidBody2.collisionMarker, RigidBody.COLLISION_MARKER_BROAD);

                    this.detectCollision(rigidBody1, rigidBody2, this.collisionData);

                    if (this.collisionData.hasMaxCapacity()) break label1;
                }
            }
        }

        int i = 0;

        while (i < this.constraints.size() && !this.collisionData.hasMaxCapacity()) {
            this.constraints.get(i).perform(this.collisionData);
            ++i;
        }

        this.collisionData.resolve();

        for (Particle particle : this.particles) {
            if (particle.hasFiniteMass()) {
                for (RigidBody rigidBody : this.rigidBodies) {
                    if (rigidBody.getCollider() instanceof PlaneCollider planeCollider) {
                        this.collideParticleWithPlane(particle, rigidBody, planeCollider);
                    }
                }
            }
        }
    }

    private boolean detectBroadCollision(RigidBody rigidBody1, RigidBody rigidBody2) {
        Vec3 position1 = rigidBody1.getPosition();
        Vec3 position2 = rigidBody2.getPosition();
        Collider collider1 = rigidBody1.getCollider();
        Collider collider2 = rigidBody2.getCollider();
        double distance = position2.distance(position1);

        return distance <= collider1.getMinDiagonalRadius() + collider2.getMinDiagonalRadius();
    }

    private void detectCollision(RigidBody rigidBody1, RigidBody rigidBody2, CollisionData collisionData) {
        if (rigidBody2.getCollider() instanceof BoxCollider boxCollider) {
            rigidBody1.getCollider().collide(rigidBody1, rigidBody2, boxCollider, collisionData);
        } else if (rigidBody2.getCollider() instanceof SphereCollider sphereCollider) {
            rigidBody1.getCollider().collide(rigidBody1, rigidBody2, sphereCollider, collisionData);
        } else if (rigidBody2.getCollider() instanceof PlaneCollider planeCollider) {
            rigidBody1.getCollider().collide(rigidBody1, rigidBody2, planeCollider, collisionData);
        }
    }

    private void collideParticleWithPlane(Particle particle, RigidBody rigidBody, PlaneCollider collider) {
        Vec3 particlePosition = particle.getPosition();
        Vec3 rigidBodyPosition = rigidBody.getPosition();
        Vec3 normal = rigidBody.getOrientation().apply(collider.normal);

        Vec3 relativePosition = particlePosition.sub(rigidBodyPosition);
        double projection = CollisionAlgorithms.project(relativePosition, normal);

        if (projection < Particle.PARTICLE_RADIUS) {
            double penetration = Particle.PARTICLE_RADIUS - projection;
            ParticleCollision.resolve(particle, normal, penetration, RigidBodyCollision.DEFAULT_RESTITUTION);
        }
    }
}
