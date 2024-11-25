package physicsengine.physics.forces;

import physicsengine.math.Vec3;
import physicsengine.physics.PhysicsObject;

import java.util.ArrayList;
import java.util.List;

public class GravityForce {
    private static final List<PhysicsObject> OBJECTS = new ArrayList<>();
    private static final Vec3 DIRECTION = Vec3.DOWN;
    private static final double G = 9.81;

    public static void apply(PhysicsObject receiver) {
        if (!receiver.hasFiniteMass()) return;

        double force = receiver.getMass() * G; // F = m * g
        receiver.addForce(DIRECTION.mul(force));
    }

    public static void register(PhysicsObject physicsObject) {
        OBJECTS.add(physicsObject);
    }

    public static void apply() {
        OBJECTS.forEach(GravityForce::apply);
    }
}
