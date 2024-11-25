package physicsengine.physics.rigidbody.colliders;

import physicsengine.math.Vec3;
import physicsengine.physics.rigidbody.CollisionData;
import physicsengine.physics.rigidbody.RigidBody;
import physicsengine.physics.rigidbody.RigidBodyCollision;
import physicsengine.physics.rigidbody.colliders.external.BoxCollision;

/*
 * Intersection Priority Order:
 * 1. Vertex - Face / Edge - Edge (nonparallel)
 * 2. Edge   - Face / Face - Face
 * 3. Vertex - Edge / Vertex - Vertex / Edge - Edge (parallel)
 */
public class CollisionAlgorithms {
    public static double project(Vec3 point, Vec3 axis) {
        return axis.dot(point);
    }

    public static void collideBoxBox(RigidBody rigidBody1, BoxCollider collider1, RigidBody rigidBody2, BoxCollider collider2, CollisionData data) {
        BoxCollision.boxAndBox(rigidBody1, collider1, rigidBody2, collider2, data);
    }

    public static void collideSphereSphere(RigidBody rigidBody1, SphereCollider collider1, RigidBody rigidBody2, SphereCollider collider2, CollisionData data) {
        Vec3 position1 = rigidBody1.getPosition();
        Vec3 position2 = rigidBody2.getPosition();
        Vec3 deltaVector = position1.sub(position2); // rigidBody2 -> rigidBody1
        double distance = deltaVector.length();

        //if (distance > collider1.radius + collider2.radius) return; // can be safely ignored because of the broad collision detection stage

        deltaVector.scale(1.0 / distance); // Vec3 normal = deltaVector.normalize();
        double surfaceDistance = distance - collider1.radius - collider2.radius;
        Vec3 collisionPoint = position2.add(deltaVector.mul(collider2.radius + surfaceDistance / 2.0));

        data.set(rigidBody1, rigidBody2, deltaVector, collisionPoint, -surfaceDistance, RigidBodyCollision.DEFAULT_RESTITUTION);
    }

    private static double clamp(double d, double min, double max) {
        return Math.max(min, Math.min(d, max));
    }

    public static void collideBoxSphere(RigidBody rigidBody1, BoxCollider collider1, RigidBody rigidBody2, SphereCollider collider2, CollisionData data) {
        Vec3 center = rigidBody2.getPosition();
        Vec3 relativeCenter = rigidBody1.getPointInLocalSpace(center);

        Vec3 closestPoint = new Vec3(0.0, 0.0, 0.0);
        closestPoint.x = clamp(relativeCenter.x, -collider1.width / 2.0, collider1.width / 2.0);
        closestPoint.y = clamp(relativeCenter.y, -collider1.height / 2.0, collider1.height / 2.0);
        closestPoint.z = clamp(relativeCenter.z, -collider1.depth / 2.0, collider1.depth / 2.0);

        double distance = closestPoint.distance(relativeCenter);
        if (distance > collider2.radius) return;

        Vec3 closestPointWorld = rigidBody1.getPointInWorldSpace(closestPoint);
        data.set(rigidBody1, rigidBody2, closestPointWorld.sub(center).normalize(1.0), closestPointWorld, collider2.radius - distance, RigidBodyCollision.DEFAULT_RESTITUTION);
    }

    public static void collideBoxPlane(RigidBody rigidBody1, BoxCollider collider1, RigidBody rigidBody2, PlaneCollider collider2, CollisionData data) {
        Vec3 center = rigidBody2.getPosition();
        Vec3 normal = rigidBody2.getOrientation().apply(collider2.normal).normalize(1.0);

        double penetration = Double.NEGATIVE_INFINITY;
        Vec3 contactVertex = null;

        for (Vec3 vertex : BoxCollider.VERTICES) {
            Vec3 boxVertex = new Vec3(vertex.x * collider1.width / 2.0, vertex.y * collider1.height / 2.0, vertex.z * collider1.depth / 2.0);
            Vec3 point = rigidBody1.getPointInWorldSpace(boxVertex);
            double depth = -project(point.sub(center), normal);

            if (depth >= 0 && depth > penetration) {
                penetration = depth;
                contactVertex = point;
            }
        }

        if (penetration >= 0) {
            Vec3 contactPoint = contactVertex.add(normal.mul(penetration / 2.0));
            data.set(rigidBody1, rigidBody2, normal, contactPoint, penetration, RigidBodyCollision.DEFAULT_RESTITUTION);
        }
    }

    public static void collideSpherePlane(RigidBody rigidBody1, SphereCollider collider1, RigidBody rigidBody2, PlaneCollider collider2, CollisionData data) {
        Vec3 center = rigidBody2.getPosition();
        Vec3 normal = rigidBody2.getOrientation().apply(collider2.normal);
        Vec3 position = rigidBody1.getPosition();

        Vec3 relativeSphereCenter = position.sub(center);
        double projection = project(relativeSphereCenter, normal);

        if (projection <= collider1.radius) {
            double depth = -projection;
            double penetration = depth + collider1.radius;
            Vec3 contactPoint = position.add(normal.mul(-1.0 * penetration / 2.0));

            data.set(rigidBody1, rigidBody2, normal, contactPoint, penetration, RigidBodyCollision.DEFAULT_RESTITUTION);
        }
    }
}
