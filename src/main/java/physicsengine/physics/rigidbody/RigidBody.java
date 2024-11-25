package physicsengine.physics.rigidbody;

import physicsengine.editor.Editable;
import physicsengine.math.Mat3;
import physicsengine.math.Quaternion;
import physicsengine.math.Vec3;
import physicsengine.math.boundingvolumes.BoundingVolume;
import physicsengine.physics.PhysicsObject;
import physicsengine.physics.rigidbody.colliders.Collider;

public class RigidBody implements PhysicsObject, Editable {
    public static final boolean HIGHLIGHT_RIGID_BODY_COLLISIONS = false;
    public static final int COLLISION_MARKER_NONE = 0;
    public static final int COLLISION_MARKER_BROAD = 1;
    public static final int COLLISION_MARKER_COLLISION = 2;
    private final double mass;
    private final Mat3 inverseInertiaTensor;
    private final Collider collider;

    private final Vec3 position = new Vec3(0.0, 0.0, 0.0);
    private final Vec3 velocity = new Vec3(0.0, 0.0, 0.0);
    private final Vec3 force = new Vec3(0.0, 0.0, 0.0);

    private final Quaternion orientation = new Quaternion(1.0, 0.0, 0.0, 0.0);
    private final Vec3 angularVelocity = new Vec3(0.0, 0.0, 0.0); // angular velocity
    private final Vec3 torque = new Vec3(0.0, 0.0, 0.0);

    private boolean selected = false;
    public int collisionMarker = COLLISION_MARKER_NONE;

    public RigidBody(double mass, Collider collider, Mat3 inverseInertiaTensor) {
        this.mass = mass;
        this.inverseInertiaTensor = inverseInertiaTensor;
        this.collider = collider;
    }

    public RigidBody(Collider collider) {
        this(Double.POSITIVE_INFINITY, collider, null);
    }

    @Override
    public void integrate(double dt) {
        if (!this.hasFiniteMass()) return;

        Vec3 acceleration = this.force.mul(1.0 / this.mass);
        this.velocity.increment(acceleration.mul(dt));
        this.velocity.scale(Math.pow(PhysicsObject.LINEAR_DAMPING_PER_SECOND, dt));
        this.position.increment(this.velocity.mul(dt));

        Vec3 angularAcceleration = this.inverseInertiaTensor.transform(this.torque);
        this.angularVelocity.increment(angularAcceleration.mul(dt));
        this.angularVelocity.scale(Math.pow(PhysicsObject.ANGULAR_DAMPING_PER_SECOND, dt));
        this.orientation.addScaledVector(this.angularVelocity, dt);

        this.force.set(0.0, 0.0, 0.0);
        this.torque.set(0.0, 0.0, 0.0);

        this.orientation.normalize();
    }

    @Override
    public double getMass() {
        return this.mass;
    }

    @Override
    public Vec3 getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Vec3 position) {
        this.position.set(position);
    }

    @Override
    public Vec3 getVelocity() {
        return this.velocity;
    }

    @Override
    public void setVelocity(Vec3 velocity) {
        this.velocity.set(velocity);
    }

    @Override
    public Quaternion getOrientation() {
        return this.orientation;
    }

    @Override
    public void setOrientation(Quaternion orientation) {
        this.orientation.set(orientation);
    }

    public Vec3 getAngularVelocity() {
        return this.angularVelocity;
    }

    public void setAngularVelocity(Vec3 angularVelocity) {
        this.angularVelocity.set(angularVelocity);
    }

    /**
     * Applies force at the center of mass.
     *
     * @param force the force to apply
     */
    @Override
    public void addForce(Vec3 force) {
        this.force.increment(force);
    }

    /**
     * Applies a force at a point in world space, relative to the center of mass.
     *
     * @param force the force to apply in world space
     * @param point a point in world coordinates where the force is applied
     */
    public void addForce(Vec3 force, Vec3 point) {
        Vec3 relativeVector = point.sub(this.position);

        this.force.increment(force);
        this.torque.increment(relativeVector.cross(force));
    }

    /**
     * Applies a force at a point in local space, relative to the center of mass.
     *
     * @param force the force to apply in world space
     * @param point a point in local coordinates where the force is applied
     */
    public void addForceAtLocalPoint(Vec3 force, Vec3 point) {
        this.addForce(force, this.getPointInWorldSpace(point));
    }

    @Override
    public void select(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    @Override
    public Vec3 getIntersection(Vec3 origin, Vec3 direction, boolean clip) {
        return BoundingVolume.getIntersection(this.collider.getBoundingVolume(), origin, direction, clip, this.position, this.orientation);
    }

    public Collider getCollider() {
        return this.collider;
    }

    public Mat3 getInverseInertiaTensor() {
        return this.inverseInertiaTensor;
    }

    public final Vec3 getPointInWorldSpace(Vec3 point) {
        return this.orientation.apply(point).add(this.position);
    }

    public final Vec3 getPointInLocalSpace(Vec3 point) {
        return this.orientation.invert().apply(point.sub(this.position));
    }
}
