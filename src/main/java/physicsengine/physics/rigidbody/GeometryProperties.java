package physicsengine.physics.rigidbody;

import physicsengine.math.Mat3;

public class GeometryProperties {
    public static double calculateCuboidMass(double density, double width, double height, double depth) {
        double volume = width * height * depth;
        return density * volume;
    }

    public static double calculateSphereMass(double density, double radius) {
        double volume = 4.0 / 3.0 * Math.PI * radius * radius * radius;
        return density * volume;
    }

    public static double calculateCylinderMass(double density, double radius, double height) {
        double volume = Math.PI * radius * radius * height;
        return density * volume;
    }

    public static Mat3 calculateCuboidInertiaTensor(double mass, double width, double height, double depth) {
        return new Mat3((1.0 / 12.0) * mass * (height * height + depth * depth), 0.0, 0.0, 0.0, (1.0 / 12.0) * mass * (width * width + depth * depth), 0.0, 0.0, 0.0, (1.0 / 12.0) * mass * (width * width + height * height));
    }

    public static Mat3 calculateSphereInertiaTensor(double mass, double radius) {
        double I = (2.0 / 5.0) * mass * radius * radius;
        return new Mat3(I, 0.0, 0.0, 0.0, I, 0.0, 0.0, 0.0, I);
    }

    public static Mat3 calculateCylinderInertiaTensor(double mass, double radius, double height) {
        double Ixz = (1.0 / 12.0) * mass * (3.0 * radius * radius + height * height);
        double Iy = (1.0 / 2.0) * mass * radius * radius;
        return new Mat3(Ixz, 0.0, 0.0, 0.0, Iy, 0.0, 0.0, 0.0, Ixz);
    }
}
