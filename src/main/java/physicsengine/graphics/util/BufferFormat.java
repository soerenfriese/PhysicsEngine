package physicsengine.graphics.util;

import static org.lwjgl.opengl.GL46.*;

public enum BufferFormat {
    POSITION(Element.POSITION),
    POSITION_NORMAL(Element.POSITION, Element.NORMAL);

    private final Element[] elements;
    private final int stride;
    public final int count;
    private final boolean[] supported = new boolean[Element.COUNT];
    private final int[] offsets = new int[Element.COUNT];

    BufferFormat(Element... elements) {
        this.elements = elements;

        int stride = 0;
        int count = 0;

        for (Element element : this.elements) {
            int j = element.ordinal();

            this.supported[j] = true;
            this.offsets[j] = stride;

            stride += element.bytes;
            count += element.count;
        }

        this.stride = stride;
        this.count = count;
    }

    public void enable(BufferFormat bufferFormat) {
        for (int i = 0; i < this.elements.length; ++i) {
            Element element = this.elements[i];
            int j = element.ordinal();

            if (!bufferFormat.supported[j]) throw new IllegalStateException("Buffer Format " + bufferFormat + " does not support element " + element + ", requested by " + this);

            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, element.count, element.type.glType, false, bufferFormat.stride, bufferFormat.offsets[j]);
        }
    }

    public void disable() {
        for (int i = 0; i < this.elements.length; ++i) {
            glDisableVertexAttribArray(i);
        }
    }

    protected enum Element {
        POSITION(3, Type.FLOAT),
        NORMAL(3, Type.FLOAT);

        public static final int COUNT = Element.values().length;
        private final int count;
        private final Type type;
        private final int bytes;

        Element(int count, Type type) {
            this.count = count;
            this.type = type;
            this.bytes = this.count * this.type.bytes;
        }
    }

    protected enum Type {
        FLOAT(GL_FLOAT, Float.BYTES);

        private final int glType;
        private final int bytes;

        Type(int glType, int bytes) {
            this.glType = glType;
            this.bytes = bytes;
        }
    }
}
