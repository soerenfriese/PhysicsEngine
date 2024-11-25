package physicsengine.math;

import org.joml.Vector3f;

public class Vec3 {
    public static final Vec3 POS_X = new Vec3(1.0, 0.0, 0.0);
    public static final Vec3 NEG_X = new Vec3(-1.0, 0.0, 0.0);
    public static final Vec3 POS_Y = new Vec3(0.0, 1.0, 0.0);
    public static final Vec3 NEG_Y = new Vec3(0.0, -1.0, 0.0);
    public static final Vec3 POS_Z = new Vec3(0.0, 0.0, 1.0);
    public static final Vec3 NEG_Z = new Vec3(0.0, 0.0, -1.0);
    public static final Vec3 BACKWARD = POS_Z, FORWARD = NEG_Z, RIGHT = POS_X, LEFT = NEG_X, UP = POS_Y, DOWN = NEG_Y;
    public double x;
    public double y;
    public double z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vec3 v) {
        this.set(v.x, v.y, v.z);
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3 add(double x, double y, double z) {
        return new Vec3(this.x + x, this.y + y, this.z + z);
    }

    public Vec3 add(Vec3 v) {
        return this.add(v.x, v.y, v.z);
    }

    public Vec3 sub(double x, double y, double z) {
        return new Vec3(this.x - x, this.y - y, this.z - z);
    }

    public Vec3 sub(Vec3 v) {
        return this.sub(v.x, v.y, v.z);
    }

    public Vec3 mul(double t) {
        return new Vec3(this.x * t, this.y * t, this.z * t);
    }

    public void increment(Vec3 v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    public void decrement(Vec3 v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
    }

    public void scale(double t) {
        this.x *= t;
        this.y *= t;
        this.z *= t;
    }

    public double dot(Vec3 v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public Vec3 cross(Vec3 v) {
        return new Vec3(this.y * v.z - this.z * v.y, this.z * v.x - this.x * v.z, this.x * v.y - this.y * v.x);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public void normalize() {
        double squared_length = this.x * this.x + this.y * this.y + this.z * this.z;

        if (squared_length != 0.0) {
            this.scale(1.0 / Math.sqrt(squared_length));
        }
    }

    public Vec3 normalize(double t) {
        Vec3 v = new Vec3(this.x, this.y, this.z);
        double squared_length = this.x * this.x + this.y * this.y + this.z * this.z;

        if (squared_length != 0.0) v.scale(t / Math.sqrt(squared_length));

        return v;
    }

    public double distance(Vec3 v) {
        double dx = this.x - v.x;
        double dy = this.y - v.y;
        double dz = this.z - v.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public void rotateX(double angle) {
        double sin = Math.sin(angle);
        double cos = org.joml.Math.cosFromSin(sin, angle);
        double y = this.y * cos - this.z * sin;
        double z = this.y * sin + this.z * cos;
        this.y = y;
        this.z = z;
    }

    public void rotateY(double angle) {
        double sin = Math.sin(angle);
        double cos = org.joml.Math.cosFromSin(sin, angle);
        double x =  this.x * cos + this.z * sin;
        double z = -this.x * sin + this.z * cos;
        this.x = x;
        this.z = z;
    }

    public void rotateZ(double angle) {
        double sin = Math.sin(angle);
        double cos = org.joml.Math.cosFromSin(sin, angle);
        double x = this.x * cos - this.y * sin;
        double y = this.x * sin + this.y * cos;
        this.x = x;
        this.y = y;
    }

    public Vec3 projectOnLine(Vec3 origin, Vec3 direction) {
        Plane plane = new Plane(direction, this);
        return plane.getIntersection(origin, direction, false);
    }

    public Vector3f vector3f() {
        return new Vector3f((float)this.x, (float)this.y, (float)this.z);
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }
}
