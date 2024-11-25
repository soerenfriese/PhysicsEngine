package physicsengine.physics.forces;

import physicsengine.math.Vec3;
import physicsengine.physics.PhysicsObject;

import java.util.ArrayList;
import java.util.List;

public class DragForce {
    private static final List<PhysicsObject> OBJECTS = new ArrayList<>();
    private static final double K_1 = 0.5;
    private static final double K_2 = 0.04;

    public static void apply(PhysicsObject receiver) {
        if (!receiver.hasFiniteMass()) return;

        double speed = receiver.getVelocity().length();
        double force = K_1 * speed + K_2 * speed * speed;

        Vec3 direction = receiver.getVelocity().normalize(-1.0);

        receiver.addForce(direction.mul(force));
    }

    public static void register(PhysicsObject physicsObject) {
        OBJECTS.add(physicsObject);
    }

    public static void apply() {
        OBJECTS.forEach(DragForce::apply);
    }
}
