package physicsengine.editor;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
    public static final int[] KEYS = new int[GLFW_KEY_LAST];
    private static final int[] BUTTONS = new int[GLFW_MOUSE_BUTTON_LAST];

    public static void keyCallback(int key, int action) {
        if (key != GLFW_KEY_UNKNOWN) Input.KEYS[key] = action;
    }

    public static void mouseButtonCallback(int button, int action) {
        Input.BUTTONS[button] = action;
    }

    public static boolean isKeyDown(int code) {
        return Input.KEYS[code] > GLFW_RELEASE;
    }

    public static boolean isMouseButtonDown(int code) {
        return Input.BUTTONS[code] > GLFW_RELEASE;
    }
}
