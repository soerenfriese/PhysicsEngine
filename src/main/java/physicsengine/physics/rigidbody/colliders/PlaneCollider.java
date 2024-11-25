package physicsengine.physics.rigidbody.colliders;

import physicsengine.math.Vec3;
import physicsengine.math.boundingvolumes.AxisAlignedBoundingBox;
import physicsengine.math.boundingvolumes.BoundingVolume;
import physicsengine.physics.rigidbody.CollisionData;
import physicsengine.physics.rigidbody.RigidBody;

public class PlaneCollider implements Collider {
    public static final double SELECT_SIZE = 0.125;
    private static final BoundingVolume BOUNDING_VOLUME = new AxisAlignedBoundingBox(new Vec3(-SELECT_SIZE, -SELECT_SIZE, -SELECT_SIZE), new Vec3(SELECT_SIZE, SELECT_SIZE, SELECT_SIZE));
    private static final double SQRT2 = Math.sqrt(2.0);
    public final Vec3 normal;
    public final double size;

    public PlaneCollider(Vec3 normal) {
        this(normal, 256.0);
    }

    public PlaneCollider(Vec3 normal, double size) {
        this.normal = normal;
        this.size = size;
    }

    @Override
    public BoundingVolume getBoundingVolume() {
        return BOUNDING_VOLUME;
    }

    @Override
    public double getMinDiagonalRadius() {
        return SQRT2 * this.size;
    }

    @Override
    public void collide(RigidBody parent, RigidBody rigidBody, BoxCollider collider, CollisionData collisionData) {
        CollisionAlgorithms.collideBoxPlane(rigidBody, collider, parent, this, collisionData);
    }

    @Override
    public void collide(RigidBody parent, RigidBody rigidBody, SphereCollider collider, CollisionData collisionData) {
        CollisionAlgorithms.collideSpherePlane(rigidBody, collider, parent, this, collisionData);
    }

    @Override
    public void collide(RigidBody parent, RigidBody rigidBody, PlaneCollider collider, CollisionData collisionData) {
    }
}
