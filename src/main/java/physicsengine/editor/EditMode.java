package physicsengine.editor;

public enum EditMode {
    TRANSLATION(Action.AxisTranslationAction.TRANSLATE_X, Action.AxisTranslationAction.TRANSLATE_Y, Action.AxisTranslationAction.TRANSLATE_Z, Action.PlaneTranslationAction.TRANSLATE_PLANE),
    ROTATION(Action.AxisRotationAction.ROTATE_X, Action.AxisRotationAction.ROTATE_Y, Action.AxisRotationAction.ROTATE_Z);

    public final Action[] actions;

    EditMode(Action ...actions) {
        this.actions = actions;
    }

    public EditMode toggle() {
        return switch (this) {
            case TRANSLATION -> ROTATION;
            case ROTATION -> TRANSLATION;
        };
    }
}
