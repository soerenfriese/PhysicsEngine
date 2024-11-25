package physicsengine.physics.forces;

import physicsengine.math.Vec3;
import physicsengine.physics.PhysicsObject;

import java.util.ArrayList;
import java.util.List;

public class SpringForce {
    public static final List<Spring> SPRINGS = new ArrayList<>();

    public static void apply(PhysicsObject receiver, PhysicsObject anchor, double k, double restLength) {
        if (!receiver.hasFiniteMass()) return;

        Vec3 deltaVector = anchor.getPosition().sub(receiver.getPosition());
        double distance = deltaVector.length();
        double displacement = (distance - restLength);
        double force = k * displacement;

        Vec3 direction = deltaVector.normalize(1.0);

        receiver.addForce(direction.mul(force));
    }

    public static void registerAnchor(PhysicsObject receiver, PhysicsObject anchor, double k, double restLength) {
        SPRINGS.add(new Spring(receiver, anchor, k, restLength));
    }

    public static void register(PhysicsObject object1, PhysicsObject object2, double k, double restLength) {
        SpringForce.registerAnchor(object1, object2, k, restLength);
        SpringForce.registerAnchor(object2, object1, k, restLength);
    }

    public static void apply() {
        for (Spring spring : SPRINGS) {
            SpringForce.apply(spring.receiver(), spring.anchor(), spring.k(), spring.restLength());
        }
    }

    public record Spring(PhysicsObject receiver, PhysicsObject anchor, double k, double restLength) {
    }
}
