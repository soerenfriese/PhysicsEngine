package physicsengine.physics.particle;

import physicsengine.math.Vec3;

public class ParticleCollision {

    public static void resolve(Particle particle, Vec3 contactNormal, double penetration, double restitution) {
        // Resolve Interpenetration
        if (penetration > 0.0) {
            Vec3 movement = contactNormal.mul(penetration);
            particle.setPosition(particle.getPosition().add(movement));
        }

        // Resolve Velocity
        double separatingVelocity = contactNormal.dot(particle.getVelocity());

        if (separatingVelocity < 0.0) {
            double deltaVelocity = -(1.0 + restitution) * separatingVelocity;
            Vec3 impulse = contactNormal.mul(deltaVelocity);
            particle.setVelocity(particle.getVelocity().add(impulse));
        }
    }
}
