package physicsengine.physics.rigidbody.colliders;

import physicsengine.math.Vec3;
import physicsengine.math.boundingvolumes.AxisAlignedBoundingBox;
import physicsengine.math.boundingvolumes.BoundingVolume;
import physicsengine.physics.rigidbody.CollisionData;
import physicsengine.physics.rigidbody.RigidBody;

public class BoxCollider implements Collider {
    public static final Vec3[] VERTICES = new Vec3[]{
            new Vec3(-1.0, -1.0, -1.0),
            new Vec3( 1.0, -1.0, -1.0),
            new Vec3( 1.0,  1.0, -1.0),
            new Vec3(-1.0,  1.0, -1.0),
            new Vec3(-1.0, -1.0,  1.0),
            new Vec3( 1.0, -1.0,  1.0),
            new Vec3( 1.0,  1.0,  1.0),
            new Vec3(-1.0,  1.0,  1.0)
    };
    public final double width;
    public final double height;
    public final double depth;
    private final double minDiagonalRadius;
    private final BoundingVolume boundingVolume;

    public BoxCollider(double width, double height, double depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.minDiagonalRadius = Math.sqrt(width * width + height * height + depth * depth) / 2.0;
        this.boundingVolume = new AxisAlignedBoundingBox(new Vec3(-width / 2.0, -height / 2.0, -depth / 2.0), new Vec3(width / 2.0, height / 2.0, depth / 2.0));
    }

    @Override
    public BoundingVolume getBoundingVolume() {
        return this.boundingVolume;
    }

    @Override
    public double getMinDiagonalRadius() {
        return this.minDiagonalRadius;
    }

    @Override
    public void collide(RigidBody parent, RigidBody rigidBody, BoxCollider collider, CollisionData collisionData) {
        CollisionAlgorithms.collideBoxBox(parent, this, rigidBody, collider, collisionData);
    }

    @Override
    public void collide(RigidBody parent, RigidBody rigidBody, SphereCollider collider, CollisionData collisionData) {
        CollisionAlgorithms.collideBoxSphere(parent, this, rigidBody, collider, collisionData);
    }

    @Override
    public void collide(RigidBody parent, RigidBody rigidBody, PlaneCollider collider, CollisionData collisionData) {
        CollisionAlgorithms.collideBoxPlane(parent, this, rigidBody, collider, collisionData);
    }
}
