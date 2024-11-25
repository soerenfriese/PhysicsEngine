package physicsengine.graphics;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.*;

public class Graphics {
    private static boolean initialized = false;

    public static void initialize() {
        if (!Graphics.initialized) {
            System.out.println("LWJGL Version:" + Version.getVersion());

            GLFWErrorCallback.createPrint(System.err).set();

            if (!glfwInit()) {
                throw new IllegalStateException("Unable to initialize GLFW");
            }

            Graphics.initialized = true;
        }
    }

    public static void terminate() {
        if (Graphics.initialized) {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }
}
