package physicsengine.physics.particle;

import physicsengine.editor.Editable;
import physicsengine.math.Quaternion;
import physicsengine.math.Vec3;
import physicsengine.math.boundingvolumes.BoundingVolume;
import physicsengine.math.boundingvolumes.SphericalBoundingVolume;
import physicsengine.physics.PhysicsObject;
import physicsengine.physics.Scene;
import physicsengine.physics.forces.GravityForce;
import physicsengine.physics.forces.SpringForce;

public class Particle implements PhysicsObject, Editable {
    public static final double PARTICLE_RADIUS = 0.15;
    private static final BoundingVolume BOUNDING_VOLUME = new SphericalBoundingVolume(Particle.PARTICLE_RADIUS);
    private final double mass;
    protected final Vec3 position = new Vec3(0.0, 0.0, 0.0);
    protected final Vec3 velocity = new Vec3(0.0, 0.0, 0.0);
    protected final Vec3 force = new Vec3(0.0, 0.0, 0.0);
    private boolean selected = false;

    public Particle(double mass) {
        this.mass = mass;
    }

    @Override
    public void integrate(double dt) {
        if (!this.hasFiniteMass()) return;

        Vec3 acceleration = this.force.mul(1.0 / this.mass);
        this.velocity.increment(acceleration.mul(dt));
        this.velocity.scale(Math.pow(PhysicsObject.LINEAR_DAMPING_PER_SECOND, dt));
        this.position.increment(this.velocity.mul(dt));

        this.force.set(0.0, 0.0, 0.0);
    }

    @Override
    public double getMass() {
        return this.mass;
    }

    @Override
    public Vec3 getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Vec3 position) {
        this.position.set(position);
    }

    @Override
    public Vec3 getVelocity() {
        return this.velocity;
    }

    @Override
    public void setVelocity(Vec3 velocity) {
        this.velocity.set(velocity);
    }

    @Override
    public void addForce(Vec3 force) {
        this.force.increment(force);
    }

    @Override
    public void select(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    @Override
    public Vec3 getIntersection(Vec3 origin, Vec3 direction, boolean clip) {
        return BoundingVolume.getIntersection(BOUNDING_VOLUME, origin, direction, clip, this.position, Quaternion.NO_ROTATION);
    }

    public static void createRigidCube(Scene scene) {
        createCube(scene, new Vec3(0.0, 0.5, 0.0), 1.0, 1.0, 700.0);
    }

    public static void createSoftCube(Scene scene) {
        createCube(scene, new Vec3(0.0, 0.5, 0.0), 1.0, 1.0, 300.0);
    }

    public static void createCube(Scene scene, Vec3 center, double sideLength, double particleMass, double k) {
        Particle[] cube = new Particle[8];

        for (int i = 0; i < 8; ++i) {
            Particle particle = new Particle(particleMass);
            cube[i] = particle;
            scene.add(particle);
            GravityForce.register(particle);
        }

        cube[0].setPosition(center.add(-sideLength / 2.0, -sideLength / 2.0, -sideLength / 2.0));
        cube[1].setPosition(center.add(sideLength / 2.0, -sideLength / 2.0, -sideLength / 2.0));
        cube[2].setPosition(center.add(sideLength / 2.0, sideLength / 2.0, -sideLength / 2.0));
        cube[3].setPosition(center.add(-sideLength / 2.0, sideLength / 2.0, -sideLength / 2.0));
        cube[4].setPosition(center.add(-sideLength / 2.0, -sideLength / 2.0, sideLength / 2.0));
        cube[5].setPosition(center.add(sideLength / 2.0, -sideLength / 2.0, sideLength / 2.0));
        cube[6].setPosition(center.add(sideLength / 2.0, sideLength / 2.0, sideLength / 2.0));
        cube[7].setPosition(center.add(-sideLength / 2.0, sideLength / 2.0, sideLength / 2.0));

        for (int i = 0; i < 8 - 1; ++i) {
            Particle a = cube[i];

            for (int j = i + 1; j < 8; ++j) {
                Particle b = cube[j];

                SpringForce.register(a, b, k, a.getPosition().distance(b.getPosition()));
            }
        }
    }
}