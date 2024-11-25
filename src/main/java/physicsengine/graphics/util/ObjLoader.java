package physicsengine.graphics.util;

import physicsengine.Main;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ObjLoader {
    private final List<Vector3f> vertices = new ArrayList<>();
    private final List<Vector3f> normals = new ArrayList<>();
    private final List<Vector3i> triangles = new ArrayList<>();
    private final List<FaceVertex> faceVertices = new ArrayList<>();

    private void readVertex(String[] arguments) {
        this.vertices.add(new Vector3f(Float.parseFloat(arguments[0]), Float.parseFloat(arguments[1]), Float.parseFloat(arguments[2])));
    }

    private void readNormal(String[] arguments) {
        this.normals.add(new Vector3f(Float.parseFloat(arguments[0]), Float.parseFloat(arguments[1]), Float.parseFloat(arguments[2])));
    }

    private int getIndex(List<?> list, int index) {
        return index < 0 ? (list.size() + index + 1) : index;
    }

    private void readFace(String[] arguments) {
        if (arguments.length < 3) {
            throw new IllegalStateException("A face must contain at least 3 vertices!");
        }

        FaceVertex[] face = new FaceVertex[arguments.length];

        for (int i = 0; i < arguments.length; ++i) {
            String vertex = arguments[i];
            String[] indices = vertex.split("/");

            if (indices.length == 3) { // only accept v//vn and v//vt//vn format
                int v = this.getIndex(this.vertices, Integer.parseInt(indices[0]));
                int vn = this.getIndex(this.normals, Integer.parseInt(indices[2]));
                face[i] = new FaceVertex(v, vn);
            } else {
                throw new IllegalStateException("Unexpected value: " + vertex);
            }
        }

        this.triangulateFace(face);
    }

    private void triangulateFace(FaceVertex[] face) {
        int index0 = this.faceVertices.size();
        int triangleCount = face.length - 2;

        for (int triangleIndex = 0; triangleIndex < triangleCount; ++triangleIndex) {
            int j = index0 + triangleIndex + 1;
            int k = index0 + triangleIndex + 2;
            this.triangles.add(new Vector3i(index0, j, k));
        }

        Collections.addAll(this.faceVertices, face);
    }

    public static Model load(String path) {
        ObjLoader loader = new ObjLoader();

        String source = Main.loadResource(path);
        String[] lines = source.split("\n");

        for (int i = 0; i < lines.length; ++i) {
            String line = lines[i];
            String[] elements = line.strip().split(" ");
            String command = elements[0];
            String[] arguments = Arrays.copyOfRange(elements, 1, elements.length);

            try {
                switch (command) {
                    case "v" -> loader.readVertex(arguments);
                    case "vn" -> loader.readNormal(arguments);
                    case "f" -> loader.readFace(arguments);
                }
            } catch (Throwable throwable) {
                throw new IllegalStateException("Error at line " + (i + 1) + ": " + throwable.getMessage());
            }
        }

        int[] triangles = new int[loader.triangles.size() * 3];
        float[] vertices = new float[loader.faceVertices.size() * 6];

        for (int i = 0; i < loader.triangles.size(); ++i) {
            Vector3i triangle = loader.triangles.get(i);
            triangles[3 * i] = triangle.x;
            triangles[3 * i + 1] = triangle.y;
            triangles[3 * i + 2] = triangle.z;
        }

        for (int i = 0; i < loader.faceVertices.size(); ++i) {
            FaceVertex faceVertex = loader.faceVertices.get(i);
            Vector3f vertex = loader.vertices.get(faceVertex.vertex - 1);
            Vector3f normal = loader.normals.get(faceVertex.normal - 1);
            vertices[6 * i] = vertex.x;
            vertices[6 * i + 1] = vertex.y;
            vertices[6 * i + 2] = vertex.z;
            vertices[6 * i + 3] = normal.x;
            vertices[6 * i + 4] = normal.y;
            vertices[6 * i + 5] = normal.z;
        }

        return new Model(triangles, vertices, BufferFormat.POSITION_NORMAL);
    }

    private static class FaceVertex {
        private final int vertex;
        private final int normal;

        public FaceVertex(int vertex, int normal) {
            this.vertex = vertex;
            this.normal = normal;
        }
    }
}
