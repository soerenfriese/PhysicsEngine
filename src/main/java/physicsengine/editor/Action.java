package physicsengine.editor;

import physicsengine.math.Plane;
import physicsengine.math.Quaternion;
import physicsengine.math.Vec3;
import physicsengine.math.boundingvolumes.AxisAlignedBoundingBox;
import physicsengine.math.boundingvolumes.BoundingVolume;

public interface Action {
    Action NONE = null;
    double HITBOX_SIZE = 0.1;

    BoundingVolume getHitbox();

    void prepare(Vec3 origin, Vec3 direction, Camera camera, OrientationScope scope, Editable selected, Vec3 position, Quaternion orientation);

    void apply(Vec3 origin, Vec3 direction, Camera camera, OrientationScope scope, Editable selected);

    abstract class TranslationAction implements Action {
        private Vec3 offset;

        @Override
        public void prepare(Vec3 origin, Vec3 direction, Camera camera, OrientationScope scope, Editable selected, Vec3 position, Quaternion orientation) {
            Vec3 projected = this.project(origin, direction, position);
            this.offset = position.sub(projected); // projected -> position
        }

        @Override
        public void apply(Vec3 origin, Vec3 direction, Camera camera, OrientationScope scope, Editable selected) {
            Vec3 projected = this.project(origin, direction, selected.getPosition());
            Vec3 position = projected.add(this.offset);
            selected.setPosition(position);
        }

        protected abstract Vec3 project(Vec3 origin, Vec3 direction, Vec3 position);
    }

    class AxisTranslationAction extends TranslationAction {
        public static final Action TRANSLATE_X = new AxisTranslationAction(Vec3.POS_X, new AxisAlignedBoundingBox(new Vec3(2.0 * HITBOX_SIZE, -HITBOX_SIZE, -HITBOX_SIZE), new Vec3(1.0 + 2.0 * HITBOX_SIZE, HITBOX_SIZE, HITBOX_SIZE)));
        public static final Action TRANSLATE_Y = new AxisTranslationAction(Vec3.POS_Y, new AxisAlignedBoundingBox(new Vec3(-HITBOX_SIZE, 2.0 * HITBOX_SIZE, -HITBOX_SIZE), new Vec3(HITBOX_SIZE, 1.0 + 2.0 * HITBOX_SIZE, HITBOX_SIZE)));
        public static final Action TRANSLATE_Z = new AxisTranslationAction(Vec3.POS_Z, new AxisAlignedBoundingBox(new Vec3(-HITBOX_SIZE, -HITBOX_SIZE, 2.0 * HITBOX_SIZE), new Vec3(HITBOX_SIZE, HITBOX_SIZE, 1.0 + 2.0 * HITBOX_SIZE)));
        private final Vec3 axis;
        private final BoundingVolume hitbox;
        private Vec3 direction;

        public AxisTranslationAction(Vec3 axis, BoundingVolume hitbox) {
            this.axis = axis;
            this.hitbox = hitbox;
        }

        @Override
        public BoundingVolume getHitbox() {
            return this.hitbox;
        }

        @Override
        public void prepare(Vec3 origin, Vec3 direction, Camera camera, OrientationScope scope, Editable selected, Vec3 position, Quaternion orientation) {
            this.direction = orientation.apply(this.axis);

            super.prepare(origin, direction, camera, scope, selected, position, orientation);
        }

        @Override
        protected Vec3 project(Vec3 origin, Vec3 direction, Vec3 position) {
            Vec3 projection = position.projectOnLine(origin, this.direction);
            Vec3 normal = projection.sub(position); // position -> projection

            if (normal.dot(direction) == 0.0) return position;

            Plane plane = new Plane(normal, position);
            Vec3 intersection = plane.getIntersection(origin, direction, false);

            return intersection.projectOnLine(position, this.direction);
        }
    }

    class PlaneTranslationAction extends TranslationAction {
        public static final Action TRANSLATE_PLANE = new PlaneTranslationAction(new AxisAlignedBoundingBox(new Vec3(-2.0 * HITBOX_SIZE, -2.0 * HITBOX_SIZE, -2.0 * HITBOX_SIZE), new Vec3(2.0 * HITBOX_SIZE, 2.0 * HITBOX_SIZE, 2.0 * HITBOX_SIZE)));
        private final BoundingVolume hitbox;
        private Plane plane;

        public PlaneTranslationAction(BoundingVolume hitbox) {
            this.hitbox = hitbox;
        }

        @Override
        public BoundingVolume getHitbox() {
            return this.hitbox;
        }

        @Override
        public void prepare(Vec3 origin, Vec3 direction, Camera camera, OrientationScope scope, Editable selected, Vec3 position, Quaternion orientation) {
            Vec3 normal = new Vec3(0.0, 0.0, -1.0);
            normal.rotateZ(-camera.roll);
            normal.rotateX(-camera.pitch);
            normal.rotateY(-camera.yaw);
            this.plane = new Plane(normal, position);

            super.prepare(origin, direction, camera, scope, selected, position, orientation);
        }

        @Override
        protected Vec3 project(Vec3 origin, Vec3 direction, Vec3 position) {
            return this.plane.getIntersection(origin, direction, false);
        }
    }

    abstract class RotationAction implements Action {
        private Vec3 direction;
        private Plane plane;
        private Quaternion offset;

        @Override
        public void prepare(Vec3 origin, Vec3 direction, Camera camera, OrientationScope scope, Editable selected, Vec3 position, Quaternion orientation) {
            this.direction = this.getAxis(scope, orientation, camera);
            this.plane = new Plane(this.direction, position);
            double angle = this.project(origin, direction, position);
            this.offset = (new Quaternion(-angle, this.direction)).mul(selected.getOrientation());
        }

        @Override
        public void apply(Vec3 origin, Vec3 direction, Camera camera, OrientationScope scope, Editable selected) {
            double angle = this.project(origin, direction, selected.getPosition());
            Quaternion orientation = (new Quaternion(angle, this.direction)).mul(this.offset);
            selected.setOrientation(orientation);
        }

        private double project(Vec3 origin, Vec3 direction, Vec3 position) {
            if (this.direction.dot(direction) == 0.0) return 0.0;

            Vec3 intersection = this.plane.getIntersection(origin, direction, false);

            double angleZ = -Math.acos(this.direction.y / this.direction.length());
            double angleY = Math.atan2(-this.direction.z, this.direction.x);

            Vec3 uv = intersection.sub(position); // position -> intersection
            uv.rotateY(-angleY);
            uv.rotateZ(-angleZ);

            double u = uv.x;
            double v = -uv.z;

            return Math.atan2(v, u);
        }

        protected abstract Vec3 getAxis(OrientationScope scope, Quaternion orientation, Camera camera);
    }

    class AxisRotationAction extends RotationAction {
        public static final Action ROTATE_X = new AxisRotationAction(Vec3.POS_X, new AxisAlignedBoundingBox(new Vec3(0.0, -1.0, -1.0), new Vec3(0.0, 1.0, 1.0)));
        public static final Action ROTATE_Y = new AxisRotationAction(Vec3.POS_Y, new AxisAlignedBoundingBox(new Vec3(-1.0, 0.0, -1.0), new Vec3(1.0, 0.0, 1.0)));
        public static final Action ROTATE_Z = new AxisRotationAction(Vec3.POS_Z, new AxisAlignedBoundingBox(new Vec3(-1.0, -1.0, 0.0), new Vec3(1.0, 1.0, 0.0)));
        private final Vec3 axis;
        private final BoundingVolume hitbox;

        public AxisRotationAction(Vec3 axis, BoundingVolume hitbox) {
            this.axis = axis;
            this.hitbox = hitbox;
        }

        @Override
        public BoundingVolume getHitbox() {
            return this.hitbox;
        }

        @Override
        protected Vec3 getAxis(OrientationScope scope, Quaternion orientation, Camera camera) {
            return orientation.apply(this.axis);
        }
    }

    class PlaneRotationAction extends RotationAction {
        public static final Action ROTATE_PLANE = new PlaneRotationAction(new AxisAlignedBoundingBox(new Vec3(-3.0 * HITBOX_SIZE, -3.0 * HITBOX_SIZE, -3.0 * HITBOX_SIZE), new Vec3(3.0 * HITBOX_SIZE, 3.0 * HITBOX_SIZE, 3.0 * HITBOX_SIZE)));
        private final BoundingVolume hitbox;

        public PlaneRotationAction(BoundingVolume hitbox) {
            this.hitbox = hitbox;
        }

        @Override
        public BoundingVolume getHitbox() {
            return this.hitbox;
        }

        @Override
        protected Vec3 getAxis(OrientationScope scope, Quaternion orientation, Camera camera) {
            Vec3 normal = new Vec3(0.0, 0.0, -1.0);
            normal.rotateZ(-camera.roll);
            normal.rotateX(-camera.pitch);
            normal.rotateY(-camera.yaw);
            return normal;
        }
    }
}
