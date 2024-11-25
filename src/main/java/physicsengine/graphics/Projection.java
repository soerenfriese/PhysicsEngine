package physicsengine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Projection {
    private static float min(float ...afloat) {
        float f = afloat[0];

        for (int i = 1; i < afloat.length; ++i) {
            if (afloat[i] < f) {
                f = afloat[i];
            }
        }

        return f;
    }

    private static float max(float ...afloat) {
        float f = afloat[0];

        for (int i = 1; i < afloat.length; ++i) {
            if (afloat[i] > f) {
                f = afloat[i];
            }
        }

        return f;
    }

    /**
     * Creates an orthographic projection matrix so that a bounding box is encapsulated in screen space.
     * The Axis aligned bounding box's vertices are projected into view space.
     * The vertices' minimum and maximum values for the x and y-axis are then used for the left, right, bottom and top parameters of the orthographic viewing frustum.
     *
     * @param min the bounding box's minimum vertex corner
     * @param max the bounding box's maximum vertex corner
     * @param view a view matrix
     * @param zNear the projection matrix's near clip plane
     * @param zFar  the projection matrix's far clip plane
     * @return an orthographic projection matrix encapsulating a bounding box
     */
    public static Matrix4f ortho(Vector3f min, Vector3f max, Matrix4f view, float zNear, float zFar) {
        Vector4f v1 = (new Vector4f(min.x, min.y, min.z, 1.0f)).mul(view);
        Vector4f v2 = (new Vector4f(max.x, min.y, min.z, 1.0f)).mul(view);
        Vector4f v3 = (new Vector4f(max.x, max.y, min.z, 1.0f)).mul(view);
        Vector4f v4 = (new Vector4f(min.x, max.y, min.z, 1.0f)).mul(view);
        Vector4f v5 = (new Vector4f(min.x, min.y, max.z, 1.0f)).mul(view);
        Vector4f v6 = (new Vector4f(max.x, min.y, max.z, 1.0f)).mul(view);
        Vector4f v7 = (new Vector4f(max.x, max.y, max.z, 1.0f)).mul(view);
        Vector4f v8 = (new Vector4f(min.x, max.y, max.z, 1.0f)).mul(view);

        float minX = min(v1.x, v2.x, v3.x, v4.x, v5.x, v6.x, v7.x, v8.x);
        float maxX = max(v1.x, v2.x, v3.x, v4.x, v5.x, v6.x, v7.x, v8.x);
        float minY = min(v1.y, v2.y, v3.y, v4.y, v5.y, v6.y, v7.y, v8.y);
        float maxY = max(v1.y, v2.y, v3.y, v4.y, v5.y, v6.y, v7.y, v8.y);

        return (new Matrix4f()).ortho(minX, maxX, minY, maxY, zNear, zFar);
    }
}
