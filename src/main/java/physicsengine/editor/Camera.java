package physicsengine.editor;

import physicsengine.math.Vec3;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private static final double DEFAULT_X = 2.0;
    private static final double DEFAULT_Y = 3.0;
    private static final double DEFAULT_Z = 5.0;
    private static final double SENSITIVITY = 14.0;
    public double x = DEFAULT_X;
    public double y = DEFAULT_Y;
    public double z = DEFAULT_Z;
    public double roll = 0.0;
    public double pitch = 0.0;
    public double yaw = 0.0;
    public double fov = Math.PI / 2.0;

    public Camera() {
    }

    public void processKeyInput(double dt) {
        Vec3 movement = new Vec3(0.0, 0.0, 0.0);

        if (Input.isKeyDown(GLFW_KEY_W)) {
            movement.increment(Vec3.FORWARD);
        }

        if (Input.isKeyDown(GLFW_KEY_A)) {
            movement.increment(Vec3.LEFT);
        }

        if (Input.isKeyDown(GLFW_KEY_S)) {
            movement.increment(Vec3.BACKWARD);
        }

        if (Input.isKeyDown(GLFW_KEY_D)) {
            movement.increment(Vec3.RIGHT);
        }

        if (Input.isKeyDown(GLFW_KEY_SPACE)) {
            movement.increment(Vec3.UP);
        }

        if (Input.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
            movement.increment(Vec3.DOWN);
        }

        if (movement.x != 0.0 || movement.y != 0.0 || movement.z != 0.0) {
            double multiplier = Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) ? 8.0 : 1.0;
            this.move(movement.normalize(SENSITIVITY * dt * multiplier));
        }
    }

    public void processMouseInput(double dx, double dy) {
        if (Input.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            this.pitch -= dy * Math.PI;
            this.yaw += dx * Math.PI;
        }

        if (Input.isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            double tan = Math.tan(this.fov / 2.0);
            Vec3 movement = new Vec3(0.0, 0.0, 0.0);
            movement.increment(Vec3.RIGHT.mul(-dx * tan * SENSITIVITY / 2.0));
            movement.increment(Vec3.UP.mul(-dy * tan * SENSITIVITY / 2.0));
            this.move(movement);
        }
    }

    public void processScrollInput(double dy) {
        double multiplier = Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) ? 8.0 : 1.0;
        this.move(Vec3.FORWARD.mul(dy * multiplier));
    }

    private void move(Vec3 direction) {
        direction.rotateZ(-this.roll);
        direction.rotateX(-this.pitch);
        direction.rotateY(-this.yaw);

        this.x += direction.x;
        this.y += direction.y;
        this.z += direction.z;
    }

    public Matrix4f getProjectionMatrix(int width, int height) {
        return (new Matrix4f()).perspective((float)this.fov, (float)width / (float)height, 0.01f, 2048.0f);
    }

    public Matrix4f getViewMatrix() {
        return (new Matrix4f()).rotateZ((float)this.roll).rotateX((float)this.pitch).rotateY((float)this.yaw).translate(-((float)this.x), -((float)this.y), -((float)this.z));
    }
}
