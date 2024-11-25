package physicsengine.physics.optimization;

import physicsengine.math.Vec3;

public class Tracker {
    private final int capacity;
    private final double[] values;
    private double sum = 0.0;
    private int index = 0;

    public Tracker(int capacity) {
        this.capacity = capacity;
        this.values = new double[this.capacity];
    }

    public void update(Vec3 velocity) {
        double old = this.values[this.index];
        this.sum -= old;

        double value = velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z;
        this.values[this.index] = value;
        this.sum += value;

        this.index = (this.index + 1) % this.capacity;
    }

    /**
     * Approach: Σ |v|² <= K
     * Assuming the velocity values are "good" (mostly the same with little deviation), we can substitute K = Σ R² where R is the reference value.
     *          K = Σ R² = C * R²
     *
     * @param referenceValue A value that represents a feasible minimum velocity for letting an object count as awake.
     *
     * @return {@code true} if the tracked object counts as sleeping
     */
    public boolean canSleep(double referenceValue) {
        return this.sum <= this.capacity * referenceValue * referenceValue;
    }
}
