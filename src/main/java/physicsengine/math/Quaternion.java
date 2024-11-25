package physicsengine.math;

public class Quaternion {
    public static final Quaternion NO_ROTATION = new Quaternion(1.0, 0.0, 0.0, 0.0);
    public double w; // cos(t / 2)
    public double x; // x * sin(t / 2)
    public double y; // y * sin(t / 2)
    public double z; // z * sin(t / 2)

    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Quaternion(double t, Vec3 axis) {
        double l = axis.length();
        double sin = Math.sin(t / 2.0);

        this.w = Math.cos(t / 2.0);
        this.x = (axis.x / l) * sin;
        this.y = (axis.y / l) * sin;
        this.z = (axis.z / l) * sin;
    }

    public void set(Quaternion q) {
        this.set(q.w, q.x, q.y, q.z);
    }

    public void set(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3 apply(Vec3 v) {
        return this.getMatrix().transform(v);
    }

    public Quaternion mul(Quaternion q) {
        double w = this.w * q.w - this.x * q.x - this.y * q.y - this.z * q.z;
        double x = this.w * q.x + this.x * q.w + this.y * q.z - this.z * q.y;
        double y = this.w * q.y - this.x * q.z + this.y * q.w + this.z * q.x;
        double z = this.w * q.z + this.x * q.y - this.y * q.x + this.z * q.w;

        return new Quaternion(w, x, y, z);
    }

    public void normalize() {
        double squared_length = this.w * this.w + this.x * this.x + this.y * this.y + this.z * this.z;

        if (squared_length != 0.0) {
            double l = 1.0 / Math.sqrt(squared_length);
            this.w *= l;
            this.x *= l;
            this.y *= l;
            this.z *= l;
        }
    }

    public Quaternion invert() {
        double inv_squared_length = 1.0 / (this.w * this.w + this.x * this.x + this.y * this.y + this.z * this.z);
        double w = this.w * inv_squared_length;
        double x = -this.x * inv_squared_length;
        double y = -this.y * inv_squared_length;
        double z = -this.z * inv_squared_length;

        return new Quaternion(w, x, y, z);
    }

    public Mat3 getMatrix() {
        return new Mat3(
                1.0 - (2.0 * this.y * this.y + 2.0 * this.z * this.z), 2.0 * this.x * this.y - 2.0 * this.z * this.w, 2.0 * this.x * this.z + 2.0 * this.y * this.w,
                2.0 * this.x * this.y + 2.0 * this.z * this.w, 1.0 - (2.0 * this.x * this.x + 2.0 * this.z * this.z), 2.0 * this.y * this.z - 2.0 * this.x * this.w,
                2.0 * this.x * this.z - 2.0 * this.y * this.w, 2.0 * this.y * this.z + 2.0 * this.x * this.w, 1.0 - (2.0 * this.x * this.x + 2.0 * this.y * this.y)
        );
    }

    @Override
    public String toString() {
        return "[" + this.w + ", " + this.x + ", " + this.y + ", " + this.z + "]";
    }

    public void addScaledVector(Vec3 v, double scale) {
        Quaternion q = new Quaternion(0.0, v.x * scale, v.y * scale, v.z * scale);
        q.multiplyBy(this);
        this.w += q.w * 0.5f;
        this.x += q.x * 0.5f;
        this.y += q.y * 0.5f;
        this.z += q.z * 0.5f;
    }

    public void multiplyBy(Quaternion q) {
        double w = this.w * q.w - this.x * q.x - this.y * q.y - this.z * q.z;
        double x = this.w * q.x + this.x * q.w + this.y * q.z - this.z * q.y;
        double y = this.w * q.y - this.x * q.z + this.y * q.w + this.z * q.x;
        double z = this.w * q.z + this.x * q.y - this.y * q.x + this.z * q.w;

        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
