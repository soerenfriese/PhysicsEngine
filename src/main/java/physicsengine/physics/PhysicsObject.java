package physicsengine.physics;

import physicsengine.editor.Editable;
import physicsengine.math.Vec3;

public interface PhysicsObject {
    double LINEAR_DAMPING_PER_SECOND = Math.pow(0.99, 60.0);                          // this.velocity.scale(Math.pow(PhysicsObject.LINEAR_DAMPING_PER_SECOND, dt));
    double ANGULAR_DAMPING_PER_SECOND = Math.pow(0.96, 60.0);                         // this.angularVelocity.scale(Math.pow(PhysicsObject.ANGULAR_DAMPING_PER_SECOND, dt));
    double EXPONENTIAL_LINEAR_DAMPING_FACTOR = Math.log(LINEAR_DAMPING_PER_SECOND);   // this.velocity.scale(Math.exp(PhysicsObject.EXPONENTIAL_LINEAR_DAMPING_FACTOR * dt));
    double EXPONENTIAL_ANGULAR_DAMPING_FACTOR = Math.log(ANGULAR_DAMPING_PER_SECOND); // this.angularVelocity.scale(Math.exp(PhysicsObject.EXPONENTIAL_ANGULAR_DAMPING_FACTOR * dt));

    void integrate(double dt);

    double getMass();

    default double getInverseMass() {
        return this.hasFiniteMass() ? 1.0 / this.getMass() : 0.0;
    }

    default boolean hasFiniteMass() {
        return !(this instanceof Editable editable && editable.isSelected()) && Double.isFinite(this.getMass());
    }

    Vec3 getPosition();

    void setPosition(Vec3 position);

    default void setPosition(double x, double y, double z) {
        this.setPosition(new Vec3(x, y, z));
    }

    Vec3 getVelocity();

    void setVelocity(Vec3 velocity);

    default void setVelocity(double x, double y, double z) {
        this.setVelocity(new Vec3(x, y, z));
    }

    void addForce(Vec3 force);
}
