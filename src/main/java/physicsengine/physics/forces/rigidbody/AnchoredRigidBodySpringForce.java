package physicsengine.physics.forces.rigidbody;

import physicsengine.math.Vec3;
import physicsengine.physics.particle.Particle;
import physicsengine.physics.rigidbody.RigidBody;

import java.util.ArrayList;
import java.util.List;

public record AnchoredRigidBodySpringForce(RigidBody rigidBody, Particle particle, Vec3 localAttachmentPoint, double k, double restLength) {
    public static final List<AnchoredRigidBodySpringForce> SPRINGS = new ArrayList<>();

    public static void apply(RigidBody rigidBody, Particle particle, Vec3 localAttachmentPoint, double k, double restLength) {
        if (!rigidBody.hasFiniteMass()) return;

        Vec3 p1 = rigidBody.getPointInWorldSpace(localAttachmentPoint);
        Vec3 p2 = particle.getPosition();
        Vec3 deltaVector = p2.sub(p1);

        double f = k * (deltaVector.length() - restLength);
        Vec3 force = deltaVector.normalize(f);

        rigidBody.addForce(force, p1);
    }

    public static void register(RigidBody rigidBody, Particle particle, Vec3 localAttachmentPoint, double k, double restLength) {
        SPRINGS.add(new AnchoredRigidBodySpringForce(rigidBody, particle, localAttachmentPoint, k, restLength));
    }

    public static void apply() {
        for (AnchoredRigidBodySpringForce anchoredRigidBodySpringForce : SPRINGS) {
            AnchoredRigidBodySpringForce.apply(anchoredRigidBodySpringForce.rigidBody(), anchoredRigidBodySpringForce.particle(), anchoredRigidBodySpringForce.localAttachmentPoint(), anchoredRigidBodySpringForce.k(), anchoredRigidBodySpringForce.restLength());
        }
    }
}
