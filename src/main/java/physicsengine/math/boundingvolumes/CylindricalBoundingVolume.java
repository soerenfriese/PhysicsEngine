package physicsengine.math.boundingvolumes;

import physicsengine.math.Vec3;

public class CylindricalBoundingVolume implements BoundingVolume {
    private final double radius;
    private final double height;

    public CylindricalBoundingVolume(double radius, double height) {
        this.radius = radius;
        this.height = height;
    }

    @Override
    public Vec3 getIntersection(Vec3 origin, Vec3 direction, boolean clip) {
        // Intersection with curved surface
        boolean flag1 = false;
        double t1 = 0.0;

        double a = (direction.x * direction.x + direction.z * direction.z);
        double b = 2.0 * (origin.x * direction.x + origin.z * direction.z);
        double c = (origin.x * origin.x + origin.z * origin.z) - this.radius * this.radius;
        double d = b * b - 4.0 * a * c;

        if (d == 0.0) {
            t1 = -b / (2.0 * a);

            if (!(clip && t1 < 0.0)) {
                double y = origin.y + t1 * direction.y;
                flag1 = Math.abs(y) <= this.height / 2.0;
            }
        } else if (d > 0.0) {
            double sqrt_d = Math.sqrt(d);
            double t2 = (-b - sqrt_d) / (2.0 * a);
            double t3 = (-b + sqrt_d) / (2.0 * a);

            double y2 = origin.y + t2 * direction.y;
            double y3 = origin.y + t3 * direction.y;
            boolean flag2 = Math.abs(y2) <= this.height / 2.0;
            boolean flag3 = Math.abs(y3) <= this.height / 2.0;

            if (flag2 || flag3) {
                t1 = getNearest(t2, t3, flag2, flag3, clip);

                if (!(clip && t1 < 0.0)) {
                    flag1 = true;
                }
            }
        }

        // Intersection with circular surfaces
        boolean flag2 = false;
        double t2 = 0.0;

        if (direction.y != 0.0) {
            double t3 = (this.height / 2.0 - origin.y) / direction.y;
            double t4 = (-this.height / 2.0 - origin.y) / direction.y;

            double x1 = origin.x + t3 * direction.x;
            double z1 = origin.z + t3 * direction.z;
            double x2 = origin.x + t4 * direction.x;
            double z2 = origin.z + t4 * direction.z;
            boolean flag3 = x1 * x1 + z1 * z1 <= this.radius * this.radius;
            boolean flag4 = x2 * x2 + z2 * z2 <= this.radius * this.radius;

            if (flag3 || flag4) {
                t2 = getNearest(t3, t4, flag3, flag4, clip);

                if (!(clip && t2 < 0.0)) {
                    flag2 = true;
                }
            }
        }

        if (flag1 || flag2) {
            double t = getNearest(t1, t2, flag1, flag2);
            return origin.add(direction.mul(t));
        } else {
            return null;
        }
    }

    /**
     * Determines the value that is closer to 0 and also valid.
     * This functions assumes that the implementing code ensures that both flag1 and flag2 are false.
     * If clip is true then the implementing code will have to ensure that the result is non-negative.
     *
     * @param t1 the first value to compare
     * @param t2 the second value to compare
     * @param flag1 {@code true} if t1 is valid
     * @param flag2 {@code true} if t2 is valid
     * @param clip if {@code true}, intersections behind the raycaster are ignored
     * @return the value that is closer to 0 and also valid
     */
    protected static double getNearest(double t1, double t2, boolean flag1, boolean flag2, boolean clip) {
        if (clip) {
            // the implementing code will have to assure that it handles the case of negative return values with the conscious that both values did not fit the requirements.
            if (flag1 && flag2) {
                return BoundingVolume.min0(t1, t2);
            } else { // (flag1 ^ flag2) == true, return the respective parameter.
                return flag1 ? t1 : t2;
            }
        } else {
            return getNearest(t1, t2, flag1, flag2);
        }
    }

    /**
     * Determines the value that is closer to 0 and also valid.
     * This functions assumes that the implementing code ensures that both flag1 and flag2 are false.
     *
     * @param t1 the first value to compare
     * @param t2 the second value to compare
     * @param flag1 {@code true} if t1 is valid
     * @param flag2 {@code true} if t2 is valid
     * @return the value that is closer to 0 and also valid
     */
    private static double getNearest(double t1, double t2, boolean flag1, boolean flag2) {
        if (flag1 && flag2) {
            return Math.abs(t1) < Math.abs(t2) ? t1 : t2;
        } else {
            return flag1 ? t1 : t2;
        }
    }
}
