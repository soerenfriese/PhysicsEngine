package physicsengine.physics.rigidbody.colliders.external;

import physicsengine.math.Mat3;
import physicsengine.math.Vec3;
import physicsengine.physics.rigidbody.CollisionData;
import physicsengine.physics.rigidbody.RigidBody;
import physicsengine.physics.rigidbody.RigidBodyCollision;
import physicsengine.physics.rigidbody.colliders.BoxCollider;

/*
Modified version of Ian Millington's Box Collision Detector.
The Code is modified to work in the Java Programming Language and has been adjusted to fit this Physics Engine.
Last Modified 2024-11-24

Source: https://github.com/idmillington/cyclone-physics/blob/master/src/collide_fine.cpp

License: https://github.com/idmillington/cyclone-physics/blob/master/LICENSE

The MIT License

Copyright (c) 2003-2009 Ian Millington

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
public class BoxCollision {
    private static double transformToAxis(BoxCollider collider, Vec3 axis, Mat3 orientation) {
        return collider.width / 2.0 * Math.abs(axis.dot(orientation.getColumn(0))) +
                collider.height / 2.0 * Math.abs(axis.dot(orientation.getColumn(1))) +
                collider.depth / 2.0 * Math.abs(axis.dot(orientation.getColumn(2)));
    }

    private static double penetrationOnAxis(BoxCollider collider1, BoxCollider collider2, Vec3 axis, Vec3 toCentre, Mat3 orientation1, Mat3 orientation2) {
        double oneProject = transformToAxis(collider1, axis, orientation1);
        double twoProject = transformToAxis(collider2, axis, orientation2);

        double distance = Math.abs(toCentre.dot(axis));

        return oneProject + twoProject - distance;
    }

    private static boolean tryAxis(BoxCollider collider1, BoxCollider collider2, Vec3 axis, Vec3 toCentre, int index, Pointer<Double> smallestPenetration, Pointer<Integer> smallestCase, Mat3 orientation1, Mat3 orientation2) {
        if (axis.x * axis.x + axis.y * axis.y + axis.z * axis.z < 0.0001) {
            return true;
        }

        axis.normalize();

        double penetration = penetrationOnAxis(collider1, collider2, axis, toCentre, orientation1, orientation2);

        if (penetration < 0) {
            return false;
        }

        if (penetration < smallestPenetration.value) {
            smallestPenetration.value = penetration;
            smallestCase.value = index;
        }

        return true;
    }

    private static void fillPointFaceBoxBox(RigidBody rigidBody1, RigidBody rigidBody2, BoxCollider collider2, Vec3 toCentre, CollisionData data, int best, double pen, Mat3 orientation1, Mat3 orientation2) {
        Vec3 normal = orientation1.getColumn(best);

        if (normal.dot(toCentre) > 0) {
            normal = normal.mul(-1.0);
        }

        Vec3 vertex = new Vec3(collider2.width / 2.0, collider2.height / 2.0, collider2.depth / 2.0);
        if (orientation2.getColumn(0).dot(normal) < 0) vertex.x = -vertex.x;
        if (orientation2.getColumn(1).dot(normal) < 0) vertex.y = -vertex.y;
        if (orientation2.getColumn(2).dot(normal) < 0) vertex.z = -vertex.z;

        data.set(rigidBody1, rigidBody2, normal, rigidBody2.getPointInWorldSpace(vertex), pen, RigidBodyCollision.DEFAULT_RESTITUTION);
    }

    private static Vec3 contactPoint(Vec3 pOne, Vec3 dOne, double oneSize, Vec3 pTwo, Vec3 dTwo, double twoSize, boolean useOne) {
        Vec3 toSt, cOne, cTwo;
        double dpStaOne, dpStaTwo, dpOneTwo, smOne, smTwo;
        double denom, mua, mub;

        smOne = dOne.x * dOne.x + dOne.y * dOne.y + dOne.z * dOne.z;
        smTwo = dTwo.x * dTwo.x + dTwo.y * dTwo.y + dTwo.z * dTwo.z;
        dpOneTwo = dTwo.dot(dOne);

        toSt = pOne.sub(pTwo);
        dpStaOne = dOne.dot(toSt);
        dpStaTwo = dTwo.dot(toSt);

        denom = smOne * smTwo - dpOneTwo * dpOneTwo;

        if (Math.abs(denom) < 0.0001) {
            return useOne ? pOne : pTwo;
        }

        mua = (dpOneTwo * dpStaTwo - smTwo * dpStaOne) / denom;
        mub = (smOne * dpStaTwo - dpOneTwo * dpStaOne) / denom;

        if (mua > oneSize || mua < -oneSize || mub > twoSize || mub < -twoSize) {
            return useOne ? pOne : pTwo;
        } else {
            cOne = pOne.add(dOne.mul(mua));
            cTwo = pTwo.add(dTwo.mul(mub));

            return cOne.mul(0.5).add(cTwo.mul(0.5));
        }
    }

    public static void boxAndBox(RigidBody rigidBody1, BoxCollider collider1, RigidBody rigidBody2, BoxCollider collider2, CollisionData data) {
        Vec3 toCentre = rigidBody2.getPosition().sub(rigidBody1.getPosition());

        Pointer<Double> penetration = new Pointer<>(Double.POSITIVE_INFINITY);
        Pointer<Integer> bestAxis = new Pointer<>(0xffffff);

        Mat3 orientation1 = rigidBody1.getOrientation().getMatrix();
        Mat3 orientation2 = rigidBody2.getOrientation().getMatrix();
        Vec3[] axes = new Vec3[15];

        for (int i = 0; i < 3; ++i) {
            axes[i] = orientation1.getColumn(i);
            axes[3 + i] = orientation2.getColumn(i);
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                axes[6 + 3 * i + j] = axes[i].cross(axes[3 + j]);
            }
        }

        for (int i = 0; i < 6; ++i) {
            if (!tryAxis(collider1, collider2, axes[i], toCentre, i, penetration, bestAxis, orientation1, orientation2)) {
                return;
            }
        }

        int bestSingleAxis = bestAxis.value;

        for (int i = 6; i < 15; ++i) {
            if (!tryAxis(collider1, collider2, axes[i], toCentre, i, penetration, bestAxis, orientation1, orientation2)) {
                return;
            }
        }

        if (bestAxis.value < 3) {
            fillPointFaceBoxBox(rigidBody1, rigidBody2, collider2, toCentre, data, bestAxis.value, penetration.value, orientation1, orientation2);
        } else if (bestAxis.value < 6) {
            fillPointFaceBoxBox(rigidBody2, rigidBody1, collider1, toCentre.mul(-1.0), data, bestAxis.value - 3, penetration.value, orientation2, orientation1);
        } else {
            bestAxis.value -= 6;
            int oneAxisIndex = bestAxis.value / 3;
            int twoAxisIndex = bestAxis.value % 3;
            Vec3 oneAxis = axes[oneAxisIndex];
            Vec3 twoAxis = axes[3 + twoAxisIndex];
            Vec3 axis = axes[6 + 3 * oneAxisIndex + twoAxisIndex];

            if (axis.dot(toCentre) > 0) {
                axis = axis.mul(-1.0);
            }

            Vec3 ptOnOneEdge = new Vec3(collider1.width / 2.0, collider1.height / 2.0, collider1.depth / 2.0);
            Vec3 ptOnTwoEdge = new Vec3(collider2.width / 2.0, collider2.height / 2.0, collider2.depth / 2.0);

            for (int i = 0; i < 3; i++) {
                if (i == oneAxisIndex) {
                    switch (i) {
                        case 0 -> ptOnOneEdge.x = 0;
                        case 1 -> ptOnOneEdge.y = 0;
                        case 2 -> ptOnOneEdge.z = 0;
                    }
                } else if (orientation1.getColumn(i).dot(axis) > 0) {
                    switch (i) {
                        case 0 -> ptOnOneEdge.x = -ptOnOneEdge.x;
                        case 1 -> ptOnOneEdge.y = -ptOnOneEdge.y;
                        case 2 -> ptOnOneEdge.z = -ptOnOneEdge.z;
                    }
                }

                if (i == twoAxisIndex) {
                    switch (i) {
                        case 0 -> ptOnTwoEdge.x = 0;
                        case 1 -> ptOnTwoEdge.y = 0;
                        case 2 -> ptOnTwoEdge.z = 0;
                    }
                } else if (orientation2.getColumn(i).dot(axis) < 0) {
                    switch (i) {
                        case 0 -> ptOnTwoEdge.x = -ptOnTwoEdge.x;
                        case 1 -> ptOnTwoEdge.y = -ptOnTwoEdge.y;
                        case 2 -> ptOnTwoEdge.z = -ptOnTwoEdge.z;
                    }
                }
            }

            ptOnOneEdge = rigidBody1.getPointInWorldSpace(ptOnOneEdge);
            ptOnTwoEdge = rigidBody2.getPointInWorldSpace(ptOnTwoEdge);

            double oneSize = switch (oneAxisIndex) {
                case 0 -> collider1.width / 2.0;
                case 1 -> collider1.height / 2.0;
                case 2 -> collider1.depth / 2.0;
                default -> throw new IllegalStateException("Unexpected value: " + oneAxisIndex);
            };

            double twoSize = switch (twoAxisIndex) {
                case 0 -> collider2.width / 2.0;
                case 1 -> collider2.height / 2.0;
                case 2 -> collider2.depth / 2.0;
                default -> throw new IllegalStateException("Unexpected value: " + twoAxisIndex);
            };

            Vec3 vertex = contactPoint(ptOnOneEdge, oneAxis, oneSize, ptOnTwoEdge, twoAxis, twoSize, bestSingleAxis > 2);

            data.set(rigidBody1, rigidBody2, axis, vertex, penetration.value, RigidBodyCollision.DEFAULT_RESTITUTION);
        }
    }

    private static class Pointer<T> {
        public T value;

        public Pointer(T value) {
            this.value = value;
        }
    }
}

