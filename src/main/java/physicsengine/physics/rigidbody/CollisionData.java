package physicsengine.physics.rigidbody;

import physicsengine.math.Vec3;

public class CollisionData {
    private final RigidBodyCollision[] collisions;
    private int index = 0;

    public CollisionData(int size) {
        this.collisions = new RigidBodyCollision[size];

        for (int i = 0; i < size; ++i) {
            this.collisions[i] = new RigidBodyCollision();
        }
    }

    public void reset() {
        this.index = 0;
    }

    public boolean hasMaxCapacity() {
        return this.index >= this.collisions.length;
    }

    public void set(RigidBody rigidBody1, RigidBody rigidBody2, Vec3 contactNormal, Vec3 contactPoint, double penetration, double restitution) {
        if (this.hasMaxCapacity()) {
            throw new IllegalStateException("CollisionData already contains maximum amount of possible collisions!");
        }

        RigidBodyCollision collision = this.collisions[this.index++];

        collision.rigidBody1 = rigidBody1;
        collision.rigidBody2 = rigidBody2;
        collision.contactNormal = contactNormal;
        collision.contactPoint = contactPoint;
        collision.penetration = penetration;
        collision.restitution = restitution;

        rigidBody1.collisionMarker = Math.max(rigidBody1.collisionMarker, RigidBody.COLLISION_MARKER_COLLISION);
        rigidBody2.collisionMarker = Math.max(rigidBody2.collisionMarker, RigidBody.COLLISION_MARKER_COLLISION);
    }

    public void resolve() {
        int i = 0;

        while (i < this.index) {
            RigidBodyCollision collision = this.collisions[i];
            collision.resolve();

            // Clean up references to free memory
            collision.rigidBody1 = null;
            collision.rigidBody2 = null;
            collision.contactNormal = null;
            collision.contactPoint = null;

            ++i;
        }
    }
}