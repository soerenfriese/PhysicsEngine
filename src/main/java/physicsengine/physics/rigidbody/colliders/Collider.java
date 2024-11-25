package physicsengine.physics.rigidbody.colliders;

import physicsengine.math.boundingvolumes.BoundingVolume;
import physicsengine.physics.rigidbody.CollisionData;
import physicsengine.physics.rigidbody.RigidBody;

public interface Collider {
    BoundingVolume getBoundingVolume();

    /**
     * During the broad collision stage of collision detection two objects need to determine whether a collision is even possible.
     * The minimum diagonal radius is the maximum possible distance from the rigid body's center at which it can collide with another rigid body.
     * It is therefore the radius used for a broad sphere collision test to check whether a thorough collision test is necessary.
     * The name is given from the case of the radius for a cube, where the sphere that fully encapsulates the cube has the diameter of the cube's diagonal.
     *
     * @return the maximum radius at which the rigid body can collide with other rigid bodies
     */
    double getMinDiagonalRadius();

    void collide(RigidBody parent, RigidBody rigidBody, BoxCollider collider, CollisionData collisionData);

    void collide(RigidBody parent, RigidBody rigidBody, SphereCollider collider, CollisionData collisionData);

    void collide(RigidBody parent, RigidBody rigidBody, PlaneCollider collider, CollisionData collisionData);
}
