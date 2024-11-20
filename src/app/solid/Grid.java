package app.solid;

import lwjglutils.OGLBuffers;
import lwjglutils.ShaderUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.glPrimitiveRestartIndex;

public class Grid extends Solid {
    public static final int FUNC_TYPE_GRID = 0;
    public static final int FUNC_TYPE_WAVE = 1;
    public static final int FUNC_TYPE_SPHERE = 2;
    public static final int FUNC_TYPE_CYLINDER = 3;
    public static final int FUNC_TYPE_HOURGLASS = 4;

    private static final int GL_PRIMITIVE_RESTART_INDEX = 65535;
    private static final Set<String> SHADER_UNIFORM_NAMES = Set.of(
            "uModelMat", "uViewMat", "uProjMat", "uColor", "uFuncType"
    );
    private static final Map<String, Integer> shaderUniforms = new HashMap<>();
    private static int shaderProgram;
    private static boolean shaderLoaded = false;

    private int funcType;

    public Grid() {
        this(50, 50);
    }

    public Grid(int width, int height) {
        this(width, height, GL_TRIANGLE_STRIP, FUNC_TYPE_GRID);
    }

    public Grid(int width, int height, int topology, int funcType) {
        // convert number of edges into number of vertices
        width += 1;
        height += 1;

        if (topology != GL_TRIANGLES && topology != GL_TRIANGLE_STRIP) {
            throw new IllegalArgumentException("Topology must be either GL_TRIANGLES or GL_TRIANGLE_STRIP");
        }

        this.topology = topology;
        this.funcType = funcType;

        float[] vb = createVertexBuffer(width, height);
        int[] ib = createIndexBuffer(width, height, topology);

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };
        buffers = new OGLBuffers(vb, attributes, ib);
        if (!shaderLoaded) {
            shaderProgram = ShaderUtils.loadProgram("/shaders/grid");
            loadShaderUniforms();
            shaderLoaded = true;
        }
    }

    @Override
    public void draw() {
        glUseProgram(shaderProgram);
        setShaderUniforms();
        glPrimitiveRestartIndex(GL_PRIMITIVE_RESTART_INDEX);
        buffers.draw(topology, shaderProgram);
    }

    private void loadShaderUniforms() {
        for (String name : SHADER_UNIFORM_NAMES) {
             shaderUniforms.put(name, glGetUniformLocation(shaderProgram, name));
        }
    }

    private void setShaderUniforms() {
        glUniformMatrix4fv(shaderUniforms.get("uModelMat"), false, modelMatrix.floatArray());
        glUniformMatrix4fv(shaderUniforms.get("uViewMat"), false, viewMatrix.floatArray());
        glUniformMatrix4fv(shaderUniforms.get("uProjMat"), false, projectionMatrix.floatArray());
        glUniform3fv(shaderUniforms.get("uColor"), color);
        glUniform1i(shaderUniforms.get("uFuncType"), funcType);
    }

    private float[] createVertexBuffer(int width, int height) {
        float[] vb = new float[2 * width * height];

        for (int i = 0, index = 0; i < height; i++) {
            float yOffset = (float) i / (height - 1f);
            for (int j = 0; j < width; j++) {
                float xOffset = (float) j / (width - 1f);
                vb[index++] = xOffset;
                vb[index++] = yOffset;
            }
        }

        return vb;
    }

    private int[] createIndexBuffer(int width, int height, int topology) {
        int size = topology == GL_TRIANGLES ? 3 * 2 * (width - 1) * (height - 1) : 2 * (width + 1) * (height - 1);
        int[] ib = new int[size];

        if (topology == GL_TRIANGLES) {
            for (int i = 0, index = 0; i < height - 1; i++) {
                int offset = i * width;
                for (int j = 0; j < width - 1; j++) {
                    ib[index++] = j + offset;
                    ib[index++] = j + width + offset;
                    ib[index++] = j + 1 + offset;

                    ib[index++] = j + 1 + offset;
                    ib[index++] = j + width + offset;
                    ib[index++] = j + width + 1 + offset;
                }
            }
        } else {
            for (int i = 0, index = 0; i < height - 1; i++) {
                int offset = i * width;
                for (int j = 0; j < width; j++) {
                    ib[index++] = j + offset;
                    ib[index++] = j + width + offset;
                }
                ib[index++] = GL_PRIMITIVE_RESTART_INDEX;
            }
        }

        return ib;
    }

    public int getFuncType() {
        return funcType;
    }

    public void setFuncType(int funcType) {
        this.funcType = funcType;
    }
}
