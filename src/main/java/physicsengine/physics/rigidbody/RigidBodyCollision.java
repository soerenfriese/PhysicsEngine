package physicsengine.physics.rigidbody;

import physicsengine.math.Mat3;
import physicsengine.math.Vec3;

public class RigidBodyCollision {
    private static final Mat3 ZERO_MATRIX = new Mat3(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public static final double DEFAULT_RESTITUTION = 0.4;
    public RigidBody rigidBody1;
    public RigidBody rigidBody2;
    public Vec3 contactNormal;
    public Vec3 contactPoint;
    public double restitution;
    public double penetration;

    /**
     * <a href="https://en.wikipedia.org/wiki/Collision_response#Impulse-based_contact_model">Source</a>
     */
    public void resolve() {
        this.resolveInterpenetration();

        // The formulas assume that the contact normal is given from the second object's perspective.
        // Therefore, the contact normal gets inverted.
        //this.contactNormal.scale(-1.0);

        boolean flag1 = this.rigidBody1.hasFiniteMass();
        boolean flag2 = this.rigidBody2.hasFiniteMass();

        Vec3 r1 = this.contactPoint.sub(this.rigidBody1.getPosition()); // m1 -> contactPoint
        Vec3 r2 = this.contactPoint.sub(this.rigidBody2.getPosition()); // m2 -> contactPoint

        Vec3 velocity1 = this.rigidBody1.getVelocity();
        Vec3 velocity2 = this.rigidBody2.getVelocity();
        Vec3 angularVelocity1 = this.rigidBody1.getAngularVelocity();
        Vec3 angularVelocity2 = this.rigidBody2.getAngularVelocity();

        Vec3 contactVelocity1 = velocity1.add(angularVelocity1.cross(r1));
        Vec3 contactVelocity2 = velocity2.add(angularVelocity2.cross(r2));

        // Step 1

        Vec3 relativeVelocity = contactVelocity2.sub(contactVelocity1);
        double inverseMass1 = this.rigidBody1.getInverseMass();
        double inverseMass2 = this.rigidBody2.getInverseMass();
        Mat3 inverseInertiaTensor1 = flag1 ? this.getWorldSpaceInverseInertiaTensor(this.rigidBody1) : ZERO_MATRIX;
        Mat3 inverseInertiaTensor2 = flag2 ? this.getWorldSpaceInverseInertiaTensor(this.rigidBody2) : ZERO_MATRIX;

        Vec3 totalAngularInertia = new Vec3(0.0, 0.0, 0.0);

        if (flag1) {
            totalAngularInertia.increment(inverseInertiaTensor1.transform(r1.cross(this.contactNormal)).cross(r1));
        }

        if (flag2) {
            totalAngularInertia.increment(inverseInertiaTensor2.transform(r2.cross(this.contactNormal)).cross(r2));
        }

        double j = (-(1.0 + this.restitution) * relativeVelocity.dot(this.contactNormal)) / (inverseMass1 + inverseMass2 + totalAngularInertia.dot(this.contactNormal));
        Vec3 impulse = this.contactNormal.mul(j);

        // Step 3: Compute linear velocities

        if (flag1) {
            Vec3 velocity1p = velocity1.sub(impulse.mul(inverseMass1));
            this.rigidBody1.setVelocity(velocity1p);
        }

        if (flag2) {
            Vec3 velocity2p = velocity2.add(impulse.mul(inverseMass2));
            this.rigidBody2.setVelocity(velocity2p);
        }

        // Step 4: Compute angular velocities

        if (flag1) {
            Vec3 angularVelocity1p = angularVelocity1.sub(inverseInertiaTensor1.transform(r1.cross(impulse)));
            this.rigidBody1.setAngularVelocity(angularVelocity1p);
        }

        if (flag2) {
            Vec3 angularVelocity2p = angularVelocity2.add(inverseInertiaTensor2.transform(r2.cross(impulse)));
            this.rigidBody2.setAngularVelocity(angularVelocity2p);
        }
    }

    /**
     * Applies a  simple linear projection that may not be the most realistic approach but suffices the needs of resolving interpenetration.
     */
    private void resolveInterpenetration() {
        if (this.penetration > 0.0) {
            double inverseMassSum = this.rigidBody1.getInverseMass() + this.rigidBody2.getInverseMass();

            double movement1 = this.penetration * (this.rigidBody1.getInverseMass() / inverseMassSum);
            double movement2 = this.penetration * (this.rigidBody2.getInverseMass() / inverseMassSum);
            this.rigidBody1.setPosition(this.rigidBody1.getPosition().add(this.contactNormal.mul(movement1)));
            this.rigidBody2.setPosition(this.rigidBody2.getPosition().add(this.contactNormal.mul(-movement2)));
        }
    }

    private Mat3 getWorldSpaceInverseInertiaTensor(RigidBody rigidBody) {
        Mat3 orientation = rigidBody.getOrientation().getMatrix();
        Mat3 inverseInertiaTensor = rigidBody.getInverseInertiaTensor();
        return orientation.mul(inverseInertiaTensor).mul(orientation.transpose());
    }
}
