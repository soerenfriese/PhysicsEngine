package physicsengine.graphics;

import physicsengine.editor.Action;
import physicsengine.editor.Camera;
import physicsengine.editor.Editor;
import physicsengine.graphics.util.*;
import physicsengine.math.Vec3;
import physicsengine.math.boundingvolumes.AxisAlignedBoundingBox;
import physicsengine.math.boundingvolumes.BoundingVolume;
import physicsengine.physics.Scene;
import physicsengine.physics.forces.SpringForce;
import physicsengine.physics.forces.rigidbody.AnchoredRigidBodySpringForce;
import physicsengine.physics.forces.rigidbody.RigidBodySpringForce;
import physicsengine.physics.particle.Particle;
import physicsengine.physics.rigidbody.Constraint;
import physicsengine.physics.rigidbody.RigidBody;
import physicsengine.physics.rigidbody.colliders.BoxCollider;
import physicsengine.physics.rigidbody.colliders.PlaneCollider;
import physicsengine.physics.rigidbody.colliders.SphereCollider;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL46.*;

public class Renderer {
    private static final int SHADOW_MAP_SIZE = 1024;
    private static final float LIGHT_NEAR_PLANE = 0.1f;
    private static final float LIGHT_FAR_PLANE = 128.0f;
    private static final Vector3f LIGHT_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);
    private static final Vector3f LIGHT_TARGET = new Vector3f(0.0f, 0.0f, 0.0f);

    private static final Vector3f COLLISION_MARKER_NONE_COLOR = new Vector3f(0.6f, 0.6f, 0.8f);
    private static final Vector3f COLLISION_MARKER_BROAD_COLOR = new Vector3f(0.0f, 0.8f, 0.0f);
    private static final Vector3f COLLISION_MARKER_COLLISION_COLOR = new Vector3f(0.8f, 0.0f, 0.0f);

    private static final float[] QUAD_GEOMETRY = {
            -1.0f, 0.0f,  1.0f, 0.0f, 1.0f, 0.0f,
            1.0f, 0.0f,  1.0f, 0.0f, 1.0f, 0.0f,
            1.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            1.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            -1.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            -1.0f, 0.0f,  1.0f, 0.0f, 1.0f, 0.0f
    };

    private Vector3f lightDirection;
    private final Editor editor;
    private Matrix4f lightViewMatrix;
    private Matrix4f lightProjectionMatrix;

    private int depthMapFramebuffer;
    private int depthTexture;

    private int dynamicElementArrayBuffer;
    private int dynamicArrayBuffer;

    private Shader depthShader;
    private Shader skyboxShader;
    private Shader defaultShader;
    private Shader staticShader;

    private Model quad;
    private Model cube;
    private Model sphere;

    public Renderer(Editor editor) {
        this.editor = editor;
        this.setLightDirection((new Vector3f(0.25f, -0.6f, 1.0f)).normalize().mul(1.0f, 1.0f, -1.0f).rotateY(-(float) Math.PI / 4.0f).mul(1.0f, 1.0f, 1.0f));
    }

    public void setLightDirection(Vector3f lightDirection) {
        this.lightDirection = lightDirection;
        Vector3f lightPosition = LIGHT_TARGET.add(lightDirection.mul(-(LIGHT_FAR_PLANE - LIGHT_NEAR_PLANE) / 2.0f, new Vector3f()), new Vector3f());
        this.lightViewMatrix = (new Matrix4f()).lookAt(lightPosition, LIGHT_TARGET, Vec3.UP.vector3f());
        this.lightProjectionMatrix = Projection.ortho(LIGHT_TARGET.sub(10.0f, 2.5f, 10.0f, new Vector3f()), LIGHT_TARGET.add(10.0f, 5.0f, 10.0f, new Vector3f()), lightViewMatrix, LIGHT_NEAR_PLANE, LIGHT_FAR_PLANE);
    }

    public void load() throws IllegalStateException {
        // Shaders
        this.depthShader = new Shader("depth", BufferFormat.POSITION);
        this.skyboxShader = new Shader("skybox", BufferFormat.POSITION);
        this.staticShader = new Shader("static", BufferFormat.POSITION);
        this.defaultShader = new Shader("default", BufferFormat.POSITION_NORMAL);

        // Models
        this.quad = new Model(QUAD_GEOMETRY, BufferFormat.POSITION_NORMAL);
        this.cube = ObjLoader.load("/assets/models/cube.obj");
        this.sphere = ObjLoader.load("/assets/models/icosphere.obj");
        this.quad.load();
        this.cube.load();
        this.sphere.load();

        // Buffers
        this.dynamicElementArrayBuffer = glGenBuffers();
        this.dynamicArrayBuffer = glGenBuffers();

        // Depth Texture and Buffer
        this.depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.depthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, new float[]{1.0f, 1.0f, 1.0f, 1.0f});

        this.depthMapFramebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, this.depthMapFramebuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.depthTexture, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void delete() {
        this.depthShader.delete();
        this.skyboxShader.delete();
        this.staticShader.delete();
        this.defaultShader.delete();

        this.quad.delete();
        this.cube.delete();
        this.sphere.delete();

        glDeleteBuffers(this.dynamicArrayBuffer);
        glDeleteBuffers(this.dynamicElementArrayBuffer);

        glDeleteFramebuffers(this.depthMapFramebuffer);
        glDeleteTextures(this.depthTexture);
    }

    public void render(Scene scene, int width, int height) {
        glDisable(GL_MULTISAMPLE); // enabled by default

        // DEPTH PASS
        glBindFramebuffer(GL_FRAMEBUFFER, this.depthMapFramebuffer);
        glViewport(0, 0, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
        glClear(GL_DEPTH_BUFFER_BIT);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(1.0f, 1.0f);

        // Prepare Uniforms
        this.depthShader.bind();
        this.depthShader.setUniform("projection", lightProjectionMatrix);
        this.depthShader.setUniform("view", lightViewMatrix);
        this.depthShader.detach();

        this.renderScene(scene, this.depthShader, this.depthShader);

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glDisable(GL_POLYGON_OFFSET_FILL);

        // RENDER PASS
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, width, height);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Camera camera = this.editor.getCamera();
        Matrix4f projectionMatrix = camera.getProjectionMatrix(width, height);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Vector3f viewPosition = new Vector3f((float)camera.x, (float)camera.y, (float)camera.z);
        float time = (float)GLFW.glfwGetTime();

        this.renderSkybox(projectionMatrix, viewMatrix, this.lightDirection, time);

        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(1.0f, 1.0f);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.depthTexture);

        // Prepare Uniforms
        this.staticShader.bind();
        this.staticShader.setUniform("projection", projectionMatrix);
        this.staticShader.setUniform("view", viewMatrix);
        this.staticShader.detach();

        this.defaultShader.bind();
        this.defaultShader.setUniform("projection", projectionMatrix);
        this.defaultShader.setUniform("view", viewMatrix);
        this.defaultShader.setUniform("lightProjection", this.lightProjectionMatrix);
        this.defaultShader.setUniform("lightView", this.lightViewMatrix);

        this.defaultShader.setUniform("lightDirection", this.lightDirection);
        this.defaultShader.setUniform("lightColor", LIGHT_COLOR);
        this.defaultShader.setUniform("viewPosition", viewPosition);
        this.defaultShader.setUniform("depthTexture", 0);
        this.defaultShader.detach();

        this.renderScene(scene, this.staticShader, this.defaultShader);

        glClear(GL_DEPTH_BUFFER_BIT);

        this.drawEditorWidgets();

        glDisable(GL_MULTISAMPLE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glDisable(GL_POLYGON_OFFSET_FILL);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void renderSkybox(Matrix4f projectionMatrix, Matrix4f viewMatrix, Vector3f lightDirection, float time) {
        this.skyboxShader.bind();
        this.skyboxShader.setUniform("projection", projectionMatrix);
        this.skyboxShader.setUniform("view", viewMatrix);
        this.skyboxShader.setUniform("lightDirection", lightDirection);
        this.skyboxShader.setUniform("lightColor", LIGHT_COLOR);
        this.skyboxShader.setUniform("time", time);
        this.cube.draw(this.skyboxShader);
        this.skyboxShader.detach();
    }

    private void renderScene(Scene scene, Shader staticShader, Shader defaultShader) {
        staticShader.bind();
        staticShader.setUniform("model", (new Matrix4f()));
        this.drawCoordinateAxis(staticShader, 1.0f, 1.0f);
        staticShader.detach();

        for (RigidBody rigidBody : scene.getRigidBodies()) {
            defaultShader.bind();

            if (RigidBody.HIGHLIGHT_RIGID_BODY_COLLISIONS) {
                switch (rigidBody.collisionMarker) {
                    default -> defaultShader.setUniform("color", COLLISION_MARKER_NONE_COLOR);
                    case RigidBody.COLLISION_MARKER_BROAD -> defaultShader.setUniform("color", COLLISION_MARKER_BROAD_COLOR);
                    case RigidBody.COLLISION_MARKER_COLLISION -> defaultShader.setUniform("color", COLLISION_MARKER_COLLISION_COLOR);
                }
            } else {
                defaultShader.setUniform("color", COLLISION_MARKER_NONE_COLOR);
            }

            Matrix4f modelMatrix = (new Matrix4f()).translate(rigidBody.getPosition().vector3f()).mul(rigidBody.getOrientation().getMatrix().matrix4f());

            if (rigidBody.getCollider() instanceof BoxCollider boxCollider) {
                defaultShader.setUniform("model", modelMatrix.scale((float)boxCollider.width / 2.0f, (float)boxCollider.height / 2.0f, (float)boxCollider.depth / 2.0f));
                this.cube.draw(defaultShader);
            } else if (rigidBody.getCollider() instanceof SphereCollider sphereCollider) {
                defaultShader.setUniform("model", modelMatrix.scale((float)sphereCollider.radius));
                this.sphere.draw(defaultShader);
            } else if (rigidBody.getCollider() instanceof PlaneCollider planeCollider) {
                Vector3f modelNormal = Vec3.POS_Y.vector3f();
                Vector3f normal = planeCollider.normal.vector3f();
                float angle = (float)Math.acos(modelNormal.dot(normal));

                if (angle > 0.0) {
                    Vector3f axis = modelNormal.cross(normal).normalize();
                    modelMatrix.rotate(angle, axis);
                }

                defaultShader.setUniform("model", modelMatrix.scale((float)PlaneCollider.SELECT_SIZE, new Matrix4f()));
                this.cube.draw(defaultShader);
                defaultShader.setUniform("model", modelMatrix.scale((float)planeCollider.size));
                defaultShader.setUniform("color", 0.6f, 0.6f, 0.6f);
                this.quad.draw(defaultShader);
            }

            defaultShader.detach();
        }

        this.drawParticles(staticShader, scene.getParticles(), 15.0f);
        this.drawLinks(staticShader, scene);
    }

    private void drawEditorWidgets() {
        if (this.editor.getSelected() != null) {
            Matrix4f model = (new Matrix4f()).translate(this.editor.getSelected().getPosition().vector3f()).mul(this.editor.getOrientationScope().getOrientation(this.editor.getSelected()).getMatrix().matrix4f());

            this.staticShader.bind();
            this.staticShader.setUniform("model", model);
            this.drawCoordinateAxis(this.staticShader, 1.2f, 8.0f);

            if (this.editor.getAction() != Action.NONE) {
                this.drawBoundingVolume(this.staticShader, this.editor.getAction().getHitbox(), 1.0f, 0.7f, 0.0f);
            } else {
                for (Action action : this.editor.getEditMode().actions) {
                    float g = 1.0f;
                    float b = 1.0f;

                    if (action == this.editor.getHoveredAction()) {
                        g = 0.7f;
                        b = 0.0f;
                    }

                    this.drawBoundingVolume(this.staticShader, action.getHitbox(), 1.0f, g, b);
                }
            }


            this.staticShader.detach();
        }
    }

    private void drawCoordinateAxis(Shader staticShader, float size, float lineWidth) {
        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f)};

        float f = glGetFloat(GL_LINE_WIDTH);
        glLineWidth(lineWidth);
        glBindBuffer(GL_ARRAY_BUFFER, this.dynamicArrayBuffer);

        for (Vector3f axis : vector3fs) {
            staticShader.setUniform("color", axis);

            glBufferData(GL_ARRAY_BUFFER, Buffers.wrap(new float[]{0.0f,0.0f, 0.0f, axis.x * size, axis.y * size, axis.z * size}), GL_DYNAMIC_DRAW);
            staticShader.format.enable(BufferFormat.POSITION);
            glDrawArrays(GL_LINES, 0, 6);
            staticShader.format.disable();
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glLineWidth(f);
    }

    private void drawBoundingVolume(Shader staticShader, BoundingVolume boundingVolume, float r, float g, float b) {
        if (boundingVolume instanceof AxisAlignedBoundingBox axisAlignedBoundingBox) {
            int[] aint = AxisAlignedBoundingBox.BOX_INTEGERS;
            float[] afloat = AxisAlignedBoundingBox.generateVertices(axisAlignedBoundingBox);

            staticShader.bind();
            staticShader.setUniform("color", r, g, b);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.dynamicElementArrayBuffer);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, Buffers.wrap(aint), GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, this.dynamicArrayBuffer);
            glBufferData(GL_ARRAY_BUFFER, Buffers.wrap(afloat), GL_DYNAMIC_DRAW);
            staticShader.format.enable(BufferFormat.POSITION);

            glDrawElements(GL_LINES, aint.length, GL_UNSIGNED_INT, 0);

            staticShader.format.disable();
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            staticShader.detach();
        }
    }

    private void drawParticles(Shader staticShader, List<Particle> particles, float pointSize) {
        float[] afloat = new float[particles.size() * 3];

        for (int i = 0; i < particles.size(); ++i) {
            Vector3f particle = particles.get(i).getPosition().vector3f();
            afloat[3 * i] = particle.x;
            afloat[3 * i + 1] = particle.y;
            afloat[3 * i + 2] = particle.z;
        }

        glEnable(GL_POINT_SMOOTH);
        float f = glGetFloat(GL_POINT_SIZE);
        glPointSize(pointSize);

        staticShader.bind();
        staticShader.setUniform("model", new Matrix4f());
        staticShader.setUniform("color", 1.0f, 1.0f, 1.0f);
        glBindBuffer(GL_ARRAY_BUFFER, this.dynamicArrayBuffer);
        glBufferData(GL_ARRAY_BUFFER, Buffers.wrap(afloat), GL_DYNAMIC_DRAW);
        staticShader.format.enable(BufferFormat.POSITION);

        glDrawArrays(GL_POINTS, 0, particles.size());

        staticShader.format.disable();
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        staticShader.detach();

        glPointSize(f);
        glDisable(GL_POINT_SMOOTH);
    }


    private void drawLinks(Shader staticShader, Scene scene) {
        int initialCapacity = SpringForce.SPRINGS.size() * 2 + RigidBodySpringForce.SPRINGS.size() * 2 + AnchoredRigidBodySpringForce.SPRINGS.size() * 2 + scene.getConstraints().size() * 2;

        if (initialCapacity == 0) {
            return;
        }

        List<Vector3f> positions = new ArrayList<>(initialCapacity);

        for (SpringForce.Spring spring : SpringForce.SPRINGS) {
            positions.add(spring.receiver().getPosition().vector3f());
            positions.add(spring.anchor().getPosition().vector3f());
        }

        for (RigidBodySpringForce spring : RigidBodySpringForce.SPRINGS) {
            positions.add(spring.rigidBody1().getPointInWorldSpace(spring.localAttachmentPoint1()).vector3f());
            positions.add(spring.rigidBody2().getPointInWorldSpace(spring.localAttachmentPoint2()).vector3f());
        }

        for (AnchoredRigidBodySpringForce spring : AnchoredRigidBodySpringForce.SPRINGS) {
            positions.add(spring.rigidBody().getPointInWorldSpace(spring.localAttachmentPoint()).vector3f());
            positions.add(spring.particle().getPosition().vector3f());
        }

        for (Constraint constraint : scene.getConstraints()) {
            positions.add(constraint.getRigidBody1().getPointInWorldSpace(constraint.getLocalAttachmentPoint1()).vector3f());
            positions.add(constraint.getRigidBody2().getPointInWorldSpace(constraint.getLocalAttachmentPoint2()).vector3f());
        }

        float[] afloat = new float[positions.size() * 3];
        int i = 0;

        for (Vector3f v : positions) {
            afloat[3 * i] = v.x;
            afloat[3 * i + 1] = v.y;
            afloat[3 * i + 2] = v.z;
            ++i;
        }

        staticShader.bind();
        staticShader.setUniform("color", 1.0f, 0.0f, 0.0f);
        staticShader.setUniform("model", new Matrix4f());
        glBindBuffer(GL_ARRAY_BUFFER, this.dynamicArrayBuffer);
        glBufferData(GL_ARRAY_BUFFER, Buffers.wrap(afloat), GL_DYNAMIC_DRAW);
        staticShader.format.enable(BufferFormat.POSITION);

        glDrawArrays(GL_LINES, 0, afloat.length / 3);

        staticShader.format.disable();
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        staticShader.detach();
    }
}
