package physicsengine.math.boundingvolumes;

import physicsengine.math.Vec3;

public class SphericalBoundingVolume implements BoundingVolume {
    protected final double radius;

    public SphericalBoundingVolume(double radius) {
        this.radius = radius;

        if (this.radius < 0.0) throw new IllegalArgumentException("SphericalBoundingVolume radius holds negative value.");
    }

    @Override
    public Vec3 getIntersection(Vec3 origin, Vec3 direction, boolean clip) {
        double origin_length = origin.length();
        double direction_length = direction.length();

        double a = direction_length * direction_length;
        double b = 2.0 * origin.dot(direction);
        double c = origin_length * origin_length - this.radius * this.radius;
        double d = b * b - 4.0 * a * c;

        if (d < 0.0) return null;

        if (d == 0.0) {
            double t = -b / (2.0 * a);

            if (clip && t < 0.0) return null;

            return origin.add(direction.mul(t));
        }

        double sqrt_d = Math.sqrt(d);
        double t1 = (-b - sqrt_d) / (2.0 * a);
        double t2 = (-b + sqrt_d) / (2.0 * a);
        /*double t;

        if (clip) {
            t = BoundingVolume.min0(t1, t2);

            if (t < 0) {
                return null;
            }
        } else {
            if (Math.abs(t1) < Math.abs(t2)) {
                t = t1;
            } else {
                t = t2;
            }
        }

        return origin.add(direction.mul(t));*/

        double t = CylindricalBoundingVolume.getNearest(t1, t2, true, true, clip);

        if (clip && t < 0.0) {
            return null;
        } else {
            return origin.add(direction.mul(t));
        }
    }
}
