package app.solid;

import lwjglutils.OGLBuffers;

public abstract class Solid {
    protected OGLBuffers buffers;
    protected int topology;

    public OGLBuffers getBuffers() {
        return buffers;
    }

    public int getTopology() {
        return topology;
    }
}
