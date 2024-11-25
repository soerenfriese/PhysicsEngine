package physicsengine.physics.rigidbody.colliders;

import physicsengine.math.boundingvolumes.BoundingVolume;
import physicsengine.math.boundingvolumes.SphericalBoundingVolume;
import physicsengine.physics.rigidbody.CollisionData;
import physicsengine.physics.rigidbody.RigidBody;

public class SphereCollider implements Collider {
    public final double radius;
    private final BoundingVolume boundingVolume;

    public SphereCollider(double radius) {
        this.radius = radius;
        this.boundingVolume = new SphericalBoundingVolume(this.radius);
    }

    @Override
    public BoundingVolume getBoundingVolume() {
        return this.boundingVolume;
    }

    @Override
    public double getMinDiagonalRadius() {
        return this.radius;
    }

    @Override
    public void collide(RigidBody parent, RigidBody rigidBody, BoxCollider collider, CollisionData collisionData) {
        CollisionAlgorithms.collideBoxSphere(rigidBody, collider, parent, this, collisionData);
    }

    @Override
    public void collide(RigidBody parent, RigidBody rigidBody, SphereCollider collider, CollisionData collisionData) {
        CollisionAlgorithms.collideSphereSphere(parent, this, rigidBody, collider, collisionData);
    }

    @Override
    public void collide(RigidBody parent, RigidBody rigidBody, PlaneCollider collider, CollisionData collisionData) {
        CollisionAlgorithms.collideSpherePlane(parent, this, rigidBody, collider, collisionData);
    }
}
