package physicsengine.math.boundingvolumes;

import physicsengine.math.Quaternion;
import physicsengine.math.Vec3;

public interface BoundingVolume {
    /**
     * Calculates the nearest intersection of a bounding volume with a ray.
     *
     * <p>The Ray direction is a normalized vector so that the distance to the origin of an intersection point in parametric form {@code origin + t * direction} can be determined by the {@code t} parameter.</p>
     *
     * @param origin the ray origin
     * @param direction the ray direction
     * @param clip if {@code true}, intersections behind the raycaster are ignored
     * @return a {@code Vec3} representing the closest intersection point or {@code null} if none was found
     */
    Vec3 getIntersection(Vec3 origin, Vec3 direction, boolean clip);

    /**
     * Calculates the nearest intersection of a bounding volume with position and orientation offset with a ray.
     *
     * <p>The Ray direction is a normalized vector so that the distance to the origin of an intersection point in parametric form {@code origin + t * direction} can be determined by the {@code t} parameter.</p>
     *
     * @param boundingVolume the bounding volume to check for intersections
     * @param origin the ray origin
     * @param direction the ray direction
     * @param clip if {@code true}, intersections behind the raycaster are ignored
     * @param position the boundingVolume's position
     * @param orientation  the boundingVolume's orientation
     * @return a {@code Vec3} representing the closest intersection point or {@code null} if none was found
     */
    static Vec3 getIntersection(BoundingVolume boundingVolume, Vec3 origin, Vec3 direction, boolean clip, Vec3 position, Quaternion orientation) {
        Quaternion inverseOrientation = orientation.invert();
        Vec3 origin2 = inverseOrientation.apply(origin.sub(position));
        Vec3 direction2 = inverseOrientation.apply(direction);
        Vec3 intersection = boundingVolume.getIntersection(origin2, direction2, clip);

        if (intersection == null) return null;

        intersection = orientation.apply(intersection);
        intersection.increment(position);
        return intersection;
    }

    /**
     * @param x first value to compare
     * @param y second value to compare
     * @return the lesser of two values greater than 0. Returns max(x, y) if both values are negative.
     */
    static double min0(double x, double y) {
        if (x > 0 && y > 0) {
            return Math.min(x, y);
        }

        return Math.max(x, y);
    }
}
