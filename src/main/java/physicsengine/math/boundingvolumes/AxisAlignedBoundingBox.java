package physicsengine.math.boundingvolumes;

import physicsengine.math.Plane;
import physicsengine.math.Vec3;

public class AxisAlignedBoundingBox implements BoundingVolume {
    protected final Vec3 min;
    protected final Vec3 max;

    public AxisAlignedBoundingBox(Vec3 min, Vec3 max) {
        this.min = min;
        this.max = max;

        if (this.min.x > this.max.x || this.min.y > this.max.y || this.min.z > this.max.z) {
            throw new IllegalArgumentException("AxisAlignedBoundingBox minimum corner exceeds maximum corner!");
        }
    }

    @Override
    public Vec3 getIntersection(Vec3 origin, Vec3 direction, boolean clip) {
        Vec3 intersection = null;
        double d = Double.POSITIVE_INFINITY;

        Plane planeMinX = new Plane(1.0, 0.0, 0.0, this.min.x);
        Vec3 p1 = planeMinX.getIntersection(origin, direction, clip);
        if (p1 != null && this.intersectsYZ(p1)) {
            double d2 = p1.distance(origin);

            if (d2 < d) {
                intersection = p1;
                d = d2;
            }
        }

        Plane planeMinY = new Plane(0.0, 1.0, 0.0, this.min.y);
        Vec3 p2 = planeMinY.getIntersection(origin, direction, clip);
        if (p2 != null && this.intersectsXZ(p2)) {
            double d2 = p2.distance(origin);

            if (d2 < d) {
                intersection = p2;
                d = d2;
            }
        }

        Plane planeMinZ = new Plane(0.0, 0.0, 1.0, this.min.z);
        Vec3 p3 = planeMinZ.getIntersection(origin, direction, clip);
        if (p3 != null && this.intersectsXY(p3)) {
            double d2 = p3.distance(origin);

            if (d2 < d) {
                intersection = p3;
                d = d2;
            }
        }

        Plane planeMaxX = new Plane(1.0, 0.0, 0.0, this.max.x);
        Vec3 p4 = planeMaxX.getIntersection(origin, direction, clip);
        if (p4 != null && this.intersectsYZ(p4)) {
            double d2 = p4.distance(origin);

            if (d2 < d) {
                intersection = p4;
                d = d2;
            }
        }

        Plane planeMaxY = new Plane(0.0, 1.0, 0.0, this.max.y);
        Vec3 p5 = planeMaxY.getIntersection(origin, direction, clip);
        if (p5 != null && this.intersectsXZ(p5)) {
            double d2 = p5.distance(origin);

            if (d2 < d) {
                intersection = p5;
                d = d2;
            }
        }

        Plane planeMaxZ = new Plane(0.0, 0.0, 1.0, this.max.z);
        Vec3 p6 = planeMaxZ.getIntersection(origin, direction, clip);
        if (p6 != null && this.intersectsXY(p6)) {
            double d2 = p6.distance(origin);

            if (d2 < d) {
                intersection = p6;
                //d = d2;
            }
        }

        return intersection;
    }

    private boolean intersectsYZ(Vec3 point) {
        return point.y >= this.min.y && point.z >= this.min.z && point.y <= this.max.y && point.z <= this.max.z;
    }

    private boolean intersectsXZ(Vec3 point) {
        return point.x >= this.min.x && point.z >= this.min.z && point.x <= this.max.x && point.z <= this.max.z;
    }

    private boolean intersectsXY(Vec3 point) {
        return point.x >= this.min.x && point.y >= this.min.y && point.x <= this.max.x && point.y <= this.max.y;
    }

    public static final int[] BOX_INTEGERS = new int[]{0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7, 7, 4, 0, 4, 1, 5, 2, 6, 3, 7};

    public static float[] generateVertices(AxisAlignedBoundingBox box) {
        return new float[]{
                (float)box.min.x, (float)box.min.y, (float)box.min.z,
                (float)box.max.x, (float)box.min.y, (float)box.min.z,
                (float)box.max.x, (float)box.max.y, (float)box.min.z,
                (float)box.min.x, (float)box.max.y, (float)box.min.z,
                (float)box.min.x, (float)box.min.y, (float)box.max.z,
                (float)box.max.x, (float)box.min.y, (float)box.max.z,
                (float)box.max.x, (float)box.max.y, (float)box.max.z,
                (float)box.min.x, (float)box.max.y, (float)box.max.z
        };
    }
}
