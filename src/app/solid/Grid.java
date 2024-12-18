package app.solid;

import lwjglutils.OGLBuffers;
import lwjglutils.OGLTexture;
import lwjglutils.ShaderUtils;
import transforms.Mat4;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.glPrimitiveRestartIndex;

public class Grid extends Solid {

    public enum FuncType {
        GRID,
        WAVE,
        SPHERE,
        CYLINDER,
        HOURGLASS,
        SPHERICAL_HOURGLASS,
        TENT
    }

    public enum ColorMode {
        COLOR,
        TEXTURE,
        VIEW_NORMAL,
        UV,
        DEPTH,
        FRAG_POS,
        LIGHT_DIST
    }

    private static final int GL_PRIMITIVE_RESTART_INDEX = 65535;
    private static final Set<String> SHADER_UNIFORM_NAMES = Set.of(
            "uModelMat", "uViewMat", "uProjMat", "uColor", "uFuncType", "uColorMode", "uTime", "uEnableLighting",
            "uLightPosition", "uLightVPMat", "uEnableShadows"
    );
    private static final Map<String, Integer> shaderUniforms = new HashMap<>();
    private static int shaderProgram;
    private static boolean shaderLoaded = false;

    private FuncType funcType;
    private ColorMode colorMode;
    private OGLTexture texture;
    private final long start;
    private boolean enableLighting = true;
    private float[] lightPosition = new float[3];
    private OGLTexture shadowMap;
    private Mat4 lightVPMat;
    private boolean enableShadows = false;

    public Grid() {
        this(50, 50);
    }

    public Grid(int width, int height) {
        this(width, height, GL_TRIANGLE_STRIP, FuncType.GRID);
    }

    public Grid(int width, int height, int topology, FuncType funcType) {
        // convert number of edges into number of vertices
        width += 1;
        height += 1;

        if (topology != GL_TRIANGLES && topology != GL_TRIANGLE_STRIP) {
            throw new IllegalArgumentException("Topology must be either GL_TRIANGLES or GL_TRIANGLE_STRIP");
        }

        this.topology = topology;
        this.funcType = funcType;
        this.colorMode = ColorMode.COLOR;
        this.start = System.currentTimeMillis();

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
        glUniform1f(shaderUniforms.get("uTime"), (float) (System.currentTimeMillis() - start));
        glUniform3fv(shaderUniforms.get("uColor"), color);
        glUniform1i(shaderUniforms.get("uFuncType"), funcType.ordinal());
        glUniform1i(shaderUniforms.get("uColorMode"), colorMode.ordinal());
        if (texture != null) {
            texture.bind(shaderProgram, "uTexture", texture.getTextureId());
        }
        glUniform1i(shaderUniforms.get("uEnableLighting"), enableLighting ? 1 : 0);
        glUniform3fv(shaderUniforms.get("uLightPosition"), lightPosition);
        if (shadowMap != null) {
            shadowMap.bind(shaderProgram, "uShadowMap", shadowMap.getTextureId());
        }
        if (lightVPMat != null) {
            glUniformMatrix4fv(shaderUniforms.get("uLightVPMat"), false, lightVPMat.floatArray());
        }
        glUniform1i(shaderUniforms.get("uEnableShadows"), enableShadows ? 1 : 0);
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

    public FuncType getFuncType() {
        return funcType;
    }

    public void setFuncType(FuncType funcType) {
        this.funcType = funcType;
    }

    public ColorMode getColorMode() {
        return colorMode;
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    public OGLTexture getTexture() {
        return texture;
    }

    public void setTexture(OGLTexture texture) {
        this.texture = texture;
    }

    public boolean isEnableLighting() {
        return enableLighting;
    }

    public void setEnableLighting(boolean enableLighting) {
        this.enableLighting = enableLighting;
    }

    public float[] getLightPosition() {
        return lightPosition;
    }

    public void setLightPosition(float[] lightPosition) {
        this.lightPosition = lightPosition;
    }

    public OGLTexture getShadowMap() {
        return shadowMap;
    }

    public void setShadowMap(OGLTexture shadowMap) {
        this.shadowMap = shadowMap;
    }

    public Mat4 getLightVPMat() {
        return lightVPMat;
    }

    public void setLightVPMat(Mat4 lightVPMat) {
        this.lightVPMat = lightVPMat;
    }

    public boolean isEnableShadows() {
        return enableShadows;
    }

    public void setEnableShadows(boolean enableShadows) {
        this.enableShadows = enableShadows;
    }
}
