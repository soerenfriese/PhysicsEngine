# Physics Engine

![Demonstration](demo.gif)

A lightweight Physics Engine in 3D with support for particles and fully rotating rigid bodies, written in Java, and based on [Ian Millington's Cyclone Physics Engine](https://github.com/idmillington/cyclone-physics).
The following Rigid Bodies are supported:
- Cubes
- Spheres
- Planes

This Physics Engine is a plain implementation of physical laws and thus does not produce fully realistic results. (Friction is also neglected)

## Controls

Use mouse and keyboard for simple scene navigation.

### Mouse
- Left Click - Orbit Rotation (Click and hold)
- Right CLick - Drag Movement (Click and hold)
- Mouse Wheel - Move forwards/backwards

### Keys

- W - Move forwards
- A - Move left
- S - Move backward
- D - Move right
- Space - Move up
- Left Shift - Move down
- Ctrl - Fast movement (Increases speed by 8x)
- ESC - Close application

### Object Selection

Select objects with left click to then translate and rotate them both in world and local scope.
Objects can be unselected by clicking into the void or selecting a different object.

Editing shortcuts:
- R - Reset the selected object's orientation
- H - Hold to automatically unselect an object after translation/rotation (Hold while releasing left click)
- Q - Toggle between translation and rotation edit mode
- TAB - Toggle between global and local orientation scope