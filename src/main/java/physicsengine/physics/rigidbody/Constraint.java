package physicsengine.physics.rigidbody;

import physicsengine.math.Vec3;

/**
 * A simple implementation model of a constraint.
 */
public class Constraint {
    private final RigidBody rigidBody1;
    private final RigidBody rigidBody2;
    private final Vec3 localAttachmentPoint1;
    private final Vec3 localAttachmentPoint2;
    private final double restLength;

    public Constraint(RigidBody rigidBody1, RigidBody rigidBody2, Vec3 localAttachmentPoint1, Vec3 localAttachmentPoint2, double restLength) {
        this.rigidBody1 = rigidBody1;
        this.rigidBody2 = rigidBody2;
        this.localAttachmentPoint1 = localAttachmentPoint1;
        this.localAttachmentPoint2 = localAttachmentPoint2;
        this.restLength = restLength;
    }

    public void perform(CollisionData data) {
        Vec3 point1 = this.rigidBody1.getPointInWorldSpace(this.localAttachmentPoint1);
        Vec3 point2 = this.rigidBody2.getPointInWorldSpace(this.localAttachmentPoint2);
        Vec3 deltaVector = point1.sub(point2); // point2 -> point1
        double distance = deltaVector.length();

        if (distance == 0.0) return;

        Vec3 normal = deltaVector.mul(1.0 / distance);
        double penetration = this.restLength - distance;

        if (distance > this.restLength) {
            normal.scale(-1.0);
            penetration *= -1.0;
        }

        Vec3 contactPoint = (point1.add(point2)).mul(0.5);
        data.set(this.rigidBody1, this.rigidBody2, normal, contactPoint, penetration, 0.0);
    }

    public RigidBody getRigidBody1() {
        return this.rigidBody1;
    }

    public RigidBody getRigidBody2() {
        return this.rigidBody2;
    }

    public Vec3 getLocalAttachmentPoint1() {
        return this.localAttachmentPoint1;
    }

    public Vec3 getLocalAttachmentPoint2() {
        return this.localAttachmentPoint2;
    }
}
