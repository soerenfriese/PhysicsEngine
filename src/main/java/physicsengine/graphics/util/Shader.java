package physicsengine.graphics.util;

import physicsengine.Main;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL46.*;

public class Shader {
    private final int program;
    public final BufferFormat format;

    static {
        ShaderDefinition.add("#define shadow(v) v", "/assets/shader_definitions/shadow.glsl");
        ShaderDefinition.add("#define snoise(v) -1.0", "/assets/shader_definitions/external/noise2D.glsl");
        ShaderDefinition.add("#define simplex(v) -1.0", "/assets/shader_definitions/simplex.glsl");
        ShaderDefinition.add("#define skybox(v) v", "/assets/shader_definitions/skybox.glsl");
    }

    public Shader(String path, BufferFormat format) throws IllegalStateException {
        this("/assets/shaders/" + path + "/" + path + ".vsh.glsl", "/assets/shaders/" + path + "/" + path + ".fsh.glsl", format);
    }

    public Shader(String vertexShaderPath, String fragmentShaderPath, BufferFormat format) throws IllegalStateException {
        this.format = format;

        String vertexShaderSource = ShaderDefinition.format(Main.loadResource(vertexShaderPath));
        String fragmentShaderSource = ShaderDefinition.format(Main.loadResource(fragmentShaderPath));

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        int i = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
        if (i == GL_FALSE) {
            throw new IllegalStateException(glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);

        i = glGetShaderi(fragmentShader, GL_COMPILE_STATUS);
        if (i == GL_FALSE) {
            throw new IllegalStateException(glGetShaderInfoLog(fragmentShader));
        }

        this.program = glCreateProgram();
        glAttachShader(this.program, vertexShader);
        glAttachShader(this.program, fragmentShader);
        glLinkProgram(this.program);

        i = glGetProgrami(this.program, GL_LINK_STATUS);
        if (i == GL_FALSE) {
            throw new IllegalStateException(glGetProgramInfoLog(this.program));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void delete() {
        glDeleteProgram(this.program);
    }

    public void bind() {
        glUseProgram(this.program);
    }

    public void detach() {
        glUseProgram(0);
    }

    public void setUniform(String name, int i) {
        int location = glGetUniformLocation(this.program, name);
        if (location == -1) return;
        glUniform1i(location, i);
    }

    public void setUniform(String name, float f) {
        int location = glGetUniformLocation(this.program, name);
        if (location == -1) return;
        glUniform1f(location, f);
    }

    public void setUniform(String name, float x, float y) {
        int location = glGetUniformLocation(this.program, name);
        if (location == -1) return;
        glUniform2f(location, x, y);
    }

    public void setUniform(String name, float x, float y, float z) {
        int location = glGetUniformLocation(this.program, name);
        if (location == -1) return;
        glUniform3f(location, x, y, z);
    }

    public void setUniform(String name, float x, float y, float z, float w) {
        int location = glGetUniformLocation(this.program, name);
        if (location == -1) return;
        glUniform4f(location, x, y, z, w);
    }

    public void setUniform(String name, FloatBuffer floatBuffer) {
        int location = glGetUniformLocation(this.program, name);
        if (location == -1) return;
        glUniformMatrix4fv(location, false, floatBuffer);
    }

    public void setUniform(String name, Vector2f v) {
        this.setUniform(name, v.x, v.y);
    }

    public void setUniform(String name, Vector3f v) {
        this.setUniform(name, v.x, v.y, v.z);
    }

    public void setUniform(String name, Vector4f v) {
        this.setUniform(name, v.x, v.y, v.z, v.w);
    }

    public void setUniform(String name, Matrix4f matrix) {
        this.setUniform(name, matrix.get(BufferUtils.createFloatBuffer(16)));
    }

    private static class ShaderDefinition {
        private static final Set<ShaderDefinition> SHADER_DEFINITIONS = new HashSet<>();
        private final String key;
        private final String path;
        private String source;

        public ShaderDefinition(String key, String path) {
            this.key = key;
            this.path = path;
        }

        public static void add(String key, String path) {
            SHADER_DEFINITIONS.add(new ShaderDefinition(key, path));
        }

        public static String format(String source) {
            String s = source;

            for (ShaderDefinition shaderDefinition : SHADER_DEFINITIONS) {
                if (!source.contains(shaderDefinition.key)) continue;

                if (shaderDefinition.source == null) {
                    shaderDefinition.source = Main.loadResource(shaderDefinition.path);
                }

                s = s.replace(shaderDefinition.key, shaderDefinition.source);
            }

            return s;
        }
    }
}
