package physicsengine.editor;

import physicsengine.math.Quaternion;

public enum OrientationScope {
    GLOBAL,
    LOCAL;

    public Quaternion getOrientation(Editable editable) {
        return switch (this) {
            case GLOBAL -> Quaternion.NO_ROTATION;
            case LOCAL -> editable.getOrientation();
        };
    }

    public OrientationScope opposite() {
        return switch (this) {
            case GLOBAL -> LOCAL;
            case LOCAL -> GLOBAL;
        };
    }
}
