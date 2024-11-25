package physicsengine.math;

public class Plane {
    protected final Vec3 normal;
    protected final double d;

    public Plane(Vec3 normal, double d) {
        this.normal = normal;
        this.d = d;
    }

    public Plane(Vec3 normal, Vec3 point) {
        this(normal, normal.dot(point));
    }

    public Plane(double a, double b, double c, double d) {
        this(new Vec3(a, b, c), d);
    }

    public Vec3 getIntersection(Vec3 origin, Vec3 direction, boolean clip) {
        double p = this.normal.dot(direction);
        if (p == 0.0) return null; // Ray is parallel to plane

        double t = (this.d - this.normal.dot(origin)) / p;
        if (clip && t < 0) return null; // Intersection lies behind Ray-caster

        return origin.add(direction.mul(t));
    }
}
