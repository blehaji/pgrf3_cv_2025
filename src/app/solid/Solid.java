package app.solid;

import lwjglutils.OGLBuffers;
import transforms.*;

public abstract class Solid {
    protected OGLBuffers buffers;
    protected int topology;
    protected Mat4 modelMatrix = new Mat4Identity();
    protected Mat4 viewMatrix = new Mat4Identity();
    protected Mat4 projectionMatrix = new Mat4Identity();
    protected float[] color = new float[3];

    public OGLBuffers getBuffers() {
        return buffers;
    }

    public int getTopology() {
        return topology;
    }

    public Mat4 getModelMatrix() {
        return modelMatrix;
    }

    public void setModelMatrix(Mat4 modelMatrix) {
        this.modelMatrix = modelMatrix;
    }

    public Mat4 getViewMatrix() {
        return viewMatrix;
    }

    public void setViewMatrix(Mat4 viewMatrix) {
        this.viewMatrix = viewMatrix;
    }

    public Mat4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public void setProjectionMatrix(Mat4 projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    public void translate(Vec3D translation) {
        this.modelMatrix = modelMatrix.mul(new Mat4Transl(translation));
    }

    public void rotate(double alpha, Vec3D axis) {
        this.modelMatrix = modelMatrix.mul(new Mat4Rot(alpha, axis));
    }

    public void scale(Vec3D scale) {
        this.modelMatrix = modelMatrix.mul(new Mat4Scale(scale));
    }

    public abstract void draw();
}
