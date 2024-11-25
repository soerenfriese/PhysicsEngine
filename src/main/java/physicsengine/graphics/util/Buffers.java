package physicsengine.graphics.util;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Buffers {
    public static IntBuffer wrap(int[] data) {
        return BufferUtils.createIntBuffer(data.length).put(data).flip();
    }

    public static FloatBuffer wrap(float[] data) {
        return BufferUtils.createFloatBuffer(data.length).put(data).flip();
    }
}
