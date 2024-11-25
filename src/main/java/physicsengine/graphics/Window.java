package physicsengine.graphics;

import physicsengine.editor.Editor;
import physicsengine.editor.Input;
import physicsengine.physics.Scene;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    public int width = 800;
    public int height = 600;
    private final long window;
    private final Editor editor;

    public Window(Scene scene) throws IllegalStateException {
        this.editor = new Editor(scene);

        Graphics.initialize();

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        this.window = glfwCreateWindow(this.width, this.height, "Physics Engine", NULL, NULL);

        if (this.window == NULL) {
            throw new IllegalStateException("Failed to create the GLFW window");
        }

        glfwSetFramebufferSizeCallback(this.window, this::framebufferSizeCallback);
        glfwSetKeyCallback(this.window, this::keyCallback);
        glfwSetCursorPosCallback(this.window, this::cursorPosCallback);
        glfwSetMouseButtonCallback(this.window, this::mouseButtonCallback);
        glfwSetScrollCallback(this.window, this::scrollCallback);

        glfwMakeContextCurrent(this.window);
        glfwSwapInterval(1);
        glfwShowWindow(this.window);
        GL.createCapabilities();

        int[] w = new int[1], h = new int[1];
        glfwGetWindowSize(this.window, w, h);
        this.width = w[0]; this.height = h[0];

        //this.editor = new Editor(scene);
        this.editor.framebufferSizeCallback(this.window, this.width, this.height);
        Renderer renderer = new Renderer(this.editor);
        renderer.load();

        double t = glfwGetTime();

        while (!glfwWindowShouldClose(this.window)) {
            double now = glfwGetTime();
            double dt = now - t;
            t = now;

            if (dt > 0) {
                dt = Math.min(dt, 1.0 / 30.0); // avoid lag occurring from frame drops

                scene.update(dt);
                this.editor.processKeyInput(dt);
            }

            renderer.render(scene, this.width, this.height);

            glfwSwapBuffers(this.window);
            glfwPollEvents();
        }

        renderer.delete();

        glfwFreeCallbacks(this.window);
        glfwDestroyWindow(this.window);

        Graphics.terminate();
    }

    private void framebufferSizeCallback(long window, int width, int height) {
        this.width = width;
        this.height = height;
        this.editor.framebufferSizeCallback(window, width, height);
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) glfwSetWindowShouldClose(this.window, true);

        Input.keyCallback(key, action);
        this.editor.keyCallback(window, key, scancode, action, mods);
    }

    private void cursorPosCallback(long window, double xpos, double ypos) {
        this.editor.cursorPosCallback(window, xpos, ypos);
    }

    private void mouseButtonCallback(long window, int button, int action, int mods) {
        Input.mouseButtonCallback(button, action);
        this.editor.mouseButtonCallback(window, button, action, mods);
    }

    private void scrollCallback(long window, double xoffset, double yoffset) {
        this.editor.scrollCallback(window, xoffset, yoffset);
    }
}
