package app.solid;

import lwjglutils.OGLBuffers;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

public class Grid extends Solid {
    public Grid(int width, int height) {
        // convert number of edges into number of vertices
        width += 1;
        height += 1;

        float[] vb = new float[2 * width * height];
//        int[] ib = new int[3 * 2 * (width - 1) * (height - 1)];
        int[] ib = new int[2 * (width + 1) * (height - 1)];

        for (int i = 0, index = 0; i < height; i++) {
            float yOffset = (float) i / (height - 1f);
            for (int j = 0; j < width; j++) {
                float xOffset = (float) j / (width - 1f);
                vb[index++] = xOffset;
                vb[index++] = yOffset;
            }
        }

//        for (int i = 0, index = 0; i < height - 1; i++) {
//            int offset = i * width;
//            for (int j = 0; j < width - 1; j++) {
//                ib[index++] = j + offset;
//                ib[index++] = j + width + offset;
//                ib[index++] = j + 1 + offset;
//
//                ib[index++] = j + 1 + offset;
//                ib[index++] = j + width + offset;
//                ib[index++] = j + width + 1 + offset;
//            }
//        }

        for (int i = 0, index = 0; i < height - 1; i++) {
            int offset = i * width;
            for (int j = 0; j < width; j++) {
                ib[index++] = j + offset;
                ib[index++] = j + width + offset;
            }
            ib[index++] = 65535;
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };
        buffers = new OGLBuffers(vb, attributes, ib);
//        topology = GL_TRIANGLES;
        topology = GL_TRIANGLE_STRIP;
    }
}
