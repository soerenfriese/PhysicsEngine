package physicsengine.physics.forces.rigidbody;

import physicsengine.math.Vec3;
import physicsengine.physics.rigidbody.RigidBody;

import java.util.ArrayList;
import java.util.List;

public record RigidBodySpringForce(RigidBody rigidBody1, RigidBody rigidBody2, Vec3 localAttachmentPoint1, Vec3 localAttachmentPoint2, double k, double restLength) {
    public static final List<RigidBodySpringForce> SPRINGS = new ArrayList<>();

    public static void apply(RigidBody rigidBody1, RigidBody rigidBody2, Vec3 localAttachmentPoint1, Vec3 localAttachmentPoint2, double k, double restLength) {
        if (!rigidBody1.hasFiniteMass() && !rigidBody2.hasFiniteMass()) return;

        Vec3 p1 = rigidBody1.getPointInWorldSpace(localAttachmentPoint1);
        Vec3 p2 = rigidBody2.getPointInWorldSpace(localAttachmentPoint2);
        Vec3 deltaVector = p2.sub(p1);

        double f = k * (deltaVector.length() - restLength);
        Vec3 force = deltaVector.normalize(f);

        if (rigidBody1.hasFiniteMass()) rigidBody1.addForce(force, p1);
        if (rigidBody2.hasFiniteMass()) rigidBody2.addForce(force.mul(-1.0), p2);
    }

    public static void register(RigidBody rigidBody1, RigidBody rigidBody2, Vec3 localAttachmentPoint1, Vec3 localAttachmentPoint2, double k, double restLength) {
        SPRINGS.add(new RigidBodySpringForce(rigidBody1, rigidBody2, localAttachmentPoint1, localAttachmentPoint2, k, restLength));
    }

    public static void apply() {
        for (RigidBodySpringForce rigidBodySpringForce : SPRINGS) {
            RigidBodySpringForce.apply(rigidBodySpringForce.rigidBody1(), rigidBodySpringForce.rigidBody2(), rigidBodySpringForce.localAttachmentPoint1(), rigidBodySpringForce.localAttachmentPoint2(), rigidBodySpringForce.k(), rigidBodySpringForce.restLength());
        }
    }
}
