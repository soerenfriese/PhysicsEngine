package physicsengine.graphics.util;

import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL46.*;

public class Model {
    private final BufferFormat bufferFormat;
    private final int[] triangles;
    private final float[] vertices;
    private int elementArrayBuffer;
    private int arrayBuffer;

    public Model(float[] vertices, BufferFormat bufferFormat) {
        this(IntStream.range(0, vertices.length / bufferFormat.count).toArray(), vertices, bufferFormat);
    }

    public Model(int[] triangles, float[] vertices, BufferFormat bufferFormat) {
        if (triangles.length % 3 != 0) throw new IllegalStateException("Triangles are of invalid size!");
        if (vertices.length % bufferFormat.count != 0) throw new IllegalStateException("Vertices are of invalid size!");

        this.bufferFormat = bufferFormat;
        this.triangles = triangles;
        this.vertices = vertices;
    }

    public void load() {
        this.elementArrayBuffer = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.elementArrayBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Buffers.wrap(this.triangles), GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        this.arrayBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.arrayBuffer);
        glBufferData(GL_ARRAY_BUFFER, Buffers.wrap(this.vertices), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void delete() {
        glDeleteBuffers(this.elementArrayBuffer);
        glDeleteBuffers(this.arrayBuffer);
    }

    public void draw(Shader shader) {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.elementArrayBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, this.arrayBuffer);
        shader.format.enable(this.bufferFormat);

        glDrawElements(GL_TRIANGLES, this.triangles.length, GL_UNSIGNED_INT, 0);

        shader.format.disable();
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
