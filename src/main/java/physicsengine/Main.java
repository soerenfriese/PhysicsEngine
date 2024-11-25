package physicsengine;

import physicsengine.graphics.Window;
import physicsengine.math.Mat3;
import physicsengine.math.Vec3;
import physicsengine.physics.PhysicsObject;
import physicsengine.physics.Scene;
import physicsengine.physics.forces.GravityForce;
import physicsengine.physics.forces.rigidbody.AnchoredRigidBodySpringForce;
import physicsengine.physics.forces.rigidbody.RigidBodySpringForce;
import physicsengine.physics.particle.Particle;
import physicsengine.physics.rigidbody.Constraint;
import physicsengine.physics.rigidbody.GeometryProperties;
import physicsengine.physics.rigidbody.RigidBody;
import physicsengine.physics.rigidbody.colliders.BoxCollider;
import physicsengine.physics.rigidbody.colliders.Collider;
import physicsengine.physics.rigidbody.colliders.PlaneCollider;
import physicsengine.physics.rigidbody.colliders.SphereCollider;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scene scene = new Scene();
        // Constants

        // All objects have the average density of plastic for simplicity. This is an arbitrarily chosen value.
        double density = 1175.0;

        double width = 1.0;
        double height = 1.0;
        double depth = 1.0;

        double radius = 0.5;

        double cubeMass = GeometryProperties.calculateCuboidMass(density, width, height, depth);
        double sphereMass = GeometryProperties.calculateSphereMass(density, radius);

        Mat3 inverseCubeInertiaTensor = GeometryProperties.calculateCuboidInertiaTensor(cubeMass, width, height, depth).invert();
        Mat3 inverseSphereInertiaTensor = GeometryProperties.calculateSphereInertiaTensor(sphereMass, radius).invert();

        Collider cubeCollider = new BoxCollider(1.0, 1.0, 1.0);
        Collider sphereCollider = new SphereCollider(0.5);
        // A ground collision plane

        RigidBody ground = new RigidBody(new PlaneCollider(Vec3.UP));
        scene.add(ground);


        // Two spheres

        RigidBody sphere1 = new RigidBody(sphereMass, sphereCollider, inverseSphereInertiaTensor);
        sphere1.setPosition(-2.0, 1.5, 0.0);
        GravityForce.register(sphere1);
        scene.add(sphere1);

        RigidBody sphere2 = new RigidBody(sphereMass, sphereCollider, inverseSphereInertiaTensor);
        sphere2.setPosition(-3.5, 4.0, 1.0);
        GravityForce.register(sphere2);
        scene.add(sphere2);


        // Stacked Cubes

        for (int x = 0; x < 2; ++x) {
            for (int y = 0; y < 2; ++y) {
                for (int z = 0; z < 2; ++z) {
                    RigidBody cube3 = new RigidBody(cubeMass, cubeCollider, inverseCubeInertiaTensor);
                    cube3.setPosition(-0.5 - x, 0.5 + y * 1.5, -3.5 - z);
                    GravityForce.register(cube3);
                    scene.add(cube3);
                }
            }
        }


        // Some Cubes held together

        int cubeCount = 3;

        // Cubes connected with Springs (more stable, less realistic)

        {
            PhysicsObject parent = new Particle(Double.POSITIVE_INFINITY);
            parent.setPosition(4.0, 5.0 + cubeCount, -2.0);
            scene.add(parent);

            for (int i = 0; i < cubeCount; ++i) {
                RigidBody cube = new RigidBody(cubeMass, cubeCollider, inverseCubeInertiaTensor);
                cube.setPosition(parent.getPosition().add(-1.0, -1.0, 1.0));
                GravityForce.register(cube);

                if (i == 0) {
                    AnchoredRigidBodySpringForce.register(cube, (Particle)parent, new Vec3(0.5, 0.5, -0.5), 30000.0, 0.0);
                } else {
                    RigidBodySpringForce.register((RigidBody)parent, cube, new Vec3(-0.5, -0.5, 0.5), new Vec3(0.5, 0.5, -0.5), 30000.0, 0.0);
                }

                scene.add(cube);
                parent = cube;
            }
        }

        // Cubes connected with Constraints (less stable, more realistic)

        {
            PhysicsObject parent = new Particle(Double.POSITIVE_INFINITY);
            parent.setPosition(8.0, 4.0 + cubeCount, -2.0);
            scene.add(parent);

            for (int i = 0; i < cubeCount; ++i) {
                RigidBody cube = new RigidBody(cubeMass, cubeCollider, inverseCubeInertiaTensor);
                cube.setPosition(parent.getPosition().add(-1.0, -1.0, 1.0));
                GravityForce.register(cube);

                if (i == 0) {
                    AnchoredRigidBodySpringForce.register(cube, (Particle)parent, new Vec3(0.5, 0.5, -0.5), 30000.0, 0.0);
                } else {
                    scene.add(new Constraint((RigidBody)parent, cube, new Vec3(-0.5, -0.5, 0.5), new Vec3(0.5, 0.5, -0.5), 0.0));
                }

                scene.add(cube);
                parent = cube;
            }
        }


        try {
            new Window(scene);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static String loadResource(String path) throws IllegalStateException {
        InputStream inputStream = Main.class.getResourceAsStream(path);

        if (inputStream == null) {
            throw new IllegalStateException("Could not find file at " + path);
        }

        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            return scanner.useDelimiter("\\A").next();
        }
    }
}