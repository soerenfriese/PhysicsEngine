package physicsengine.editor;

import physicsengine.math.Quaternion;
import physicsengine.math.Vec3;
import physicsengine.math.boundingvolumes.BoundingVolume;
import physicsengine.physics.PhysicsObject;
import physicsengine.physics.Scene;
import physicsengine.physics.rigidbody.RigidBody;

import static org.lwjgl.glfw.GLFW.*;

public class Editor {
    private final Scene scene;
    private final Camera camera;
    private OrientationScope orientationScope = OrientationScope.GLOBAL;
    private EditMode editMode = EditMode.TRANSLATION;
    private Action action = Action.NONE;
    private Editable selected = null;
    private boolean viewerAdjustsCamera = false;
    private int width;
    private int height;
    private double mouseX;
    private double mouseY;
    private Action hoveredAction = Action.NONE;

    public Editor(Scene scene) {
        this.scene = scene;
        this.camera = new Camera();
    }

    public void processKeyInput(double dt) {
        this.camera.processKeyInput(dt);

        if (this.action != Action.NONE && (Input.isKeyDown(GLFW_KEY_W) || Input.isKeyDown(GLFW_KEY_A) || Input.isKeyDown(GLFW_KEY_S) || Input.isKeyDown(GLFW_KEY_D) || Input.isKeyDown(GLFW_KEY_SPACE) || Input.isKeyDown(GLFW_KEY_LEFT_SHIFT))) {
            this.processMouseMove(this.mouseX, this.mouseY, 0.0, 0.0, this.width, this.height);
        }
    }

    public void framebufferSizeCallback(long window, int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_TAB && action == GLFW_RELEASE && this.selected != null && this.action == Action.NONE) {
            this.orientationScope = this.orientationScope.opposite();
        }

        if (key == GLFW_KEY_Q && action == GLFW_RELEASE && this.selected != null && this.action == Action.NONE) {
            this.editMode = this.editMode.toggle();
        }

        if (key == GLFW_KEY_R && action == GLFW_RELEASE && this.selected != null) {
            this.selected.setOrientation(Quaternion.NO_ROTATION);
        }
    }

    public void cursorPosCallback(long window, double xpos, double ypos) {
        double x = xpos;
        double y = (double)this.height - ypos;
        double dx = x - this.mouseX;
        double dy = y - this.mouseY;
        this.mouseX = x;
        this.mouseY = y;

        this.processMouseMove(x, y, dx, dy, this.width, this.height);
        this.calculateHoveredAction();
    }

    public void mouseButtonCallback(long window, int button, int action, int mods) {
        this.processMouseButtonPress(button, action, this.mouseX, this.mouseY, this.width, this.height);
    }

    public void scrollCallback(long window, double xoffset, double yoffset) {
        this.camera.processScrollInput(yoffset);
        this.processMouseMove(this.mouseX, this.mouseY, 0.0, 0.0, this.width, this.height);
    }

    private void processMouseMove(double x, double y, double dx, double dy, int width, int height) {
        if (this.action == Action.NONE) {
            this.camera.processMouseInput(2.0 * dx / (double)this.height, 2.0 * dy / (double)this.height);
            this.viewerAdjustsCamera = Input.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) || Input.isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT);
        } else {
            Vec3 origin = new Vec3(this.camera.x, this.camera.y, this.camera.z);
            Vec3 direction = this.calculateRayDirection(x, y, width, height);
            this.action.apply(origin, direction, this.camera, this.orientationScope, this.selected);
        }
    }

    private void processMouseButtonPress(int button, int action, double x, double y, int width, int height) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            switch (action) {
                case GLFW_PRESS -> this.onPress(x, y, width, height);
                case GLFW_RELEASE -> this.onRelease(x, y, width, height);
            }
        }
    }

    private void onPress(double x, double y, int width, int height) {
        if (this.selected == null) return;

        Vec3 origin = new Vec3(this.camera.x, this.camera.y, this.camera.z);
        Vec3 direction = this.calculateRayDirection(x, y, width, height);
        Vec3 position = this.selected.getPosition();
        Quaternion orientation = this.orientationScope.getOrientation(this.selected);

        Action nearest = Action.NONE;
        double d = Double.POSITIVE_INFINITY;

        for (Action action : this.editMode.actions) {
            Vec3 intersection = BoundingVolume.getIntersection(action.getHitbox(), origin, direction, true, position, orientation);

            if (intersection != null) {
                double d2 = intersection.distance(origin);

                if (d2 <= d) {
                    nearest = action;
                    d = d2;
                }
            }
        }

        if (nearest != Action.NONE) {
            this.action = nearest;
            this.action.prepare(origin, direction, this.camera, this.orientationScope, this.selected, position, orientation);
        }
    }

    private void onRelease(double x, double y, int width, int height) {
        if (this.action != Action.NONE || this.viewerAdjustsCamera) {
            this.action = Action.NONE;
            this.viewerAdjustsCamera = false;

            if (Input.isKeyDown(GLFW_KEY_H)) {
                this.setSelected(null);
            }

            return;
        }

        Vec3 origin = new Vec3(this.camera.x, this.camera.y, this.camera.z);
        Vec3 direction = this.calculateRayDirection(x, y, width, height);

        Editable nearest = null;
        double d = Double.POSITIVE_INFINITY;

        for (PhysicsObject physicsObject : this.scene.getPhysicsObjects()) {
            if (physicsObject instanceof Editable editable) {
                Vec3 intersection = editable.getIntersection(origin, direction, true);

                if (intersection != null) {
                    double d2 = intersection.distance(origin);

                    if (d2 < d) {
                        nearest = editable;
                        d = d2;
                    }
                }
            }
        }

        this.setSelected(nearest);

        if (this.selected instanceof RigidBody rigidBody) {
            rigidBody.setVelocity(new Vec3(0.0, 0.0, 0.0));
            rigidBody.setAngularVelocity(new Vec3(0.0, 0.0, 0.0));
        }
    }

    private Vec3 calculateRayDirection(double x, double y, int width, int height) {
        double ndcX = x / (double)width * 2.0 - 1.0;
        double ndcY = y / (double)height * 2.0 - 1.0;
        double aspect = (double)width / (double)height;
        double tan = Math.tan(this.camera.fov / 2.0);

        Vec3 direction = new Vec3(ndcX * aspect * tan, ndcY * tan, -1.0);
        direction.rotateZ(-this.camera.roll);
        direction.rotateX(-this.camera.pitch);
        direction.rotateY(-this.camera.yaw);
        direction.normalize();

        return direction;
    }

    private void setSelected(Editable editable) {
        if (this.selected != null) {
            this.selected.select(false);
        }

        this.selected = editable;

        if (this.selected != null) {
            this.selected.select(true);
        }
    }

    private void calculateHoveredAction() {
        if (this.selected == null) {
            this.hoveredAction = Action.NONE;
            return;
        }

        Vec3 origin = new Vec3(this.camera.x, this.camera.y, this.camera.z);
        Vec3 direction = this.calculateRayDirection(this.mouseX, this.mouseY, this.width, this.height);
        Vec3 position = this.selected.getPosition();
        Quaternion orientation = this.orientationScope.getOrientation(this.selected);

        Action nearest = Action.NONE;
        double d = Double.POSITIVE_INFINITY;

        for (Action action : this.editMode.actions) {
            Vec3 intersection = BoundingVolume.getIntersection(action.getHitbox(), origin, direction, true, position, orientation);

            if (intersection != null) {
                double d2 = intersection.distance(origin);

                if (d2 <= d) {
                    nearest = action;
                    d = d2;
                }
            }
        }

        this.hoveredAction = nearest;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public OrientationScope getOrientationScope() {
        return this.orientationScope;
    }

    public EditMode getEditMode() {
        return this.editMode;
    }

    public Action getAction() {
        return this.action;
    }

    public Editable getSelected() {
        return this.selected;
    }

    public Action getHoveredAction() {
        return this.hoveredAction;
    }
}