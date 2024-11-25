package physicsengine.editor;

import physicsengine.math.Quaternion;
import physicsengine.math.Vec3;
import physicsengine.math.boundingvolumes.BoundingVolume;

public interface Editable {
    void select(boolean selected);

    boolean isSelected();

    /**
     * @see BoundingVolume
     */
    Vec3 getIntersection(Vec3 origin, Vec3 direction, boolean clip);

    Vec3 getPosition();

    void setPosition(Vec3 position);

    default Quaternion getOrientation() {
        return Quaternion.NO_ROTATION;
    }

    default void setOrientation(Quaternion orientation) {
    }
}
