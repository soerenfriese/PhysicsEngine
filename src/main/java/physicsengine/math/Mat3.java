package physicsengine.math;

import org.joml.Matrix4f;

public class Mat3 {
    private final double m00;
    private final double m01;
    private final double m02;
    private final double m10;
    private final double m11;
    private final double m12;
    private final double m20;
    private final double m21;
    private final double m22;

    public Mat3(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
    }

    public Vec3 transform(Vec3 v) {
        return new Vec3(v.x * this.m00 + v.y * this.m01 + v.z * this.m02, v.x * this.m10 + v.y * this.m11 + v.z * this.m12, v.x * this.m20 + v.y * this.m21 + v.z * this.m22);
    }

    public Mat3 mul(Mat3 m) {
        double m00 = this.m00 * m.m00 + this.m01 * m.m10 + this.m02 * m.m20;
        double m01 = this.m00 * m.m01 + this.m01 * m.m11 + this.m02 * m.m21;
        double m02 = this.m00 * m.m02 + this.m01 * m.m12 + this.m02 * m.m22;

        double m10 = this.m10 * m.m00 + this.m11 * m.m10 + this.m12 * m.m20;
        double m11 = this.m10 * m.m01 + this.m11 * m.m11 + this.m12 * m.m21;
        double m12 = this.m10 * m.m02 + this.m11 * m.m12 + this.m12 * m.m22;

        double m20 = this.m20 * m.m00 + this.m21 * m.m10 + this.m22 * m.m20;
        double m21 = this.m20 * m.m01 + this.m21 * m.m11 + this.m22 * m.m21;
        double m22 = this.m20 * m.m02 + this.m21 * m.m12 + this.m22 * m.m22;

        return new Mat3(m00, m01, m02, m10, m11, m12, m20, m21, m22);
    }

    public Mat3 invert() {
        double det = this.m00 * this.m11 * this.m22 + this.m01 * this.m12 * this.m20 + this.m02 * this.m10 * this.m21 - this.m02 * this.m11 * this.m20 - this.m01 * this.m10 * this.m22 - this.m00 * this.m12 * this.m21;

        if (det == 0.0) return null;

        double inv_det = 1.0 / det;

        double m00 = inv_det * (this.m11 * this.m22 - this.m12 * this.m21);
        double m01 = inv_det * (this.m02 * this.m21 - this.m01 * this.m22);
        double m02 = inv_det * (this.m01 * this.m12 - this.m02 * this.m11);

        double m10 = inv_det * (this.m12 * this.m20 - this.m10 * this.m22);
        double m11 = inv_det * (this.m00 * this.m22 - this.m02 * this.m20);
        double m12 = inv_det * (this.m02 * this.m10 - this.m00 * this.m12);

        double m20 = inv_det * (this.m10 * this.m21 - this.m11 * this.m20);
        double m21 = inv_det * (this.m01 * this.m20 - this.m00 * this.m21);
        double m22 = inv_det * (this.m00 * this.m11 - this.m01 * this.m10);

        return new Mat3(m00, m01, m02, m10, m11, m12, m20, m21, m22);
    }

    public Mat3 transpose() {
        return new Mat3(this.m00, this.m10, this.m20, this.m01, this.m11, this.m21, this.m02, this.m12, this.m22);
    }

    public Vec3 getColumn(int column) {
        return switch (column) {
            case 0 -> new Vec3(this.m00, this.m10, this.m20);
            case 1 -> new Vec3(this.m01, this.m11, this.m21);
            case 2 -> new Vec3(this.m02, this.m12, this.m22);
            default -> throw new IllegalStateException("Unexpected value: " + column);
        };
    }

    @Override
    public String toString() {
        return "⎡ " + this.m00 + ", " + this.m01 + ", " + this.m02 + " ⎤\n⎢ " + this.m10 + ", " + this.m11 + ", " + this.m12 + " ⎥\n⎣ " + this.m20 + ", " + this.m21 + ", " + this.m22 + " ⎦";
    }

    public Matrix4f matrix4f() {
        return new Matrix4f((float)this.m00, (float)this.m10, (float)this.m20, 0.0f, (float)this.m01, (float)this.m11, (float)this.m21, 0.0f, (float)this.m02, (float)this.m12, (float)this.m22, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
    }
}
