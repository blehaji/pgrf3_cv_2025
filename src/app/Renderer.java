package app;

import app.solid.Grid;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLTexture;
import lwjglutils.OGLTexture2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import transforms.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.DoubleBuffer;
import java.util.*;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART;

public class Renderer extends AbstractRenderer {

    private final static String TEXTURE_PATH = "textures/";

    private enum PolygonMode {
        LINE(GL_LINE),
        POINT(GL_POINT),
        FILL(GL_FILL);

        private final int value;
        PolygonMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private Camera camera;
    private Mat4 projectionMatrix;
    private final Map<String, OGLTexture> textures = new HashMap<>();
    private final List<String> textureNames = new ArrayList<>();
    private int textureIndex;
    private final List<Grid> grids = new ArrayList<>();
    private Grid grid, light;
    private PolygonMode polygonMode = PolygonMode.FILL;
    private boolean isPerspectiveProjection = true;
    private boolean isMousePressed = false;
    private final double[] mouseOrigin = new double[2];
    private Vec3D lightPosition = new Vec3D(1.5, 0, 1.5);

    @Override
    public void init() {
        textRenderer = new OGLTextRenderer(width, height);

        loadTextures();

        Grid floor = new Grid();
        floor.scale(new Vec3D(4));
        grid = new Grid(100, 100, GL_TRIANGLES, Grid.FuncType.SPHERE);
        grid.setColor(1, 1, 0);
        grid.translate(new Vec3D(0, 0, 1));
        setTexture(grid);
        grid.setColorMode(Grid.ColorMode.TEXTURE);

        grids.add(floor);
        grids.add(grid);

        light = new Grid(50, 50, GL_TRIANGLES, Grid.FuncType.SPHERE);
        light.setColor(1, 1, 1);
        light.setEnableLighting(false);
        light.scale(new Vec3D(0.1));
        light.translate(lightPosition);

        camera = new Camera()
                .withPosition(new Vec3D(0, -3, 1))
                .withAzimuth(Math.toRadians(90))
                .withZenith(Math.toRadians(-15))
                .withFirstPerson(true);
        updateProjectionMatrix();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_PRIMITIVE_RESTART);
    }

    private void loadTextures() {
        URL url = getClass().getClassLoader().getResource(TEXTURE_PATH);
        assert url != null;

        File textureDirectory = new File(url.getFile());
        for (File file : Objects.requireNonNull(textureDirectory.listFiles())) {
            String path = TEXTURE_PATH + file.getName();
            System.out.println("Loading texture: " + path);
            try {
                OGLTexture texture = new OGLTexture2D(path);
                textures.put(file.getName(), texture);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            textureNames.add(file.getName());
        }
        textureIndex = textureNames.size() - 1;
    }

    private void setTexture(Grid grid) {
        grid.setTexture(textures.get(textureNames.get(textureIndex)));
    }

    private void updateGrids() {
        if (light != null) {
            light.setProjectionMatrix(projectionMatrix);
            light.setViewMatrix(camera.getViewMatrix());
        }

        for (Grid grid : grids) {
            grid.setProjectionMatrix(projectionMatrix);
            grid.setViewMatrix(camera.getViewMatrix());
        }
    }

    @Override
    public void display() {
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glPolygonMode(GL_FRONT_AND_BACK, polygonMode.getValue());

        light.draw();
        float[] lightPositionF = new float[] {
                (float) lightPosition.getX(),
                (float) lightPosition.getY(),
                (float) lightPosition.getZ()
        };

        for (Grid grid : grids) {
            grid.setLightPosition(lightPositionF);
            grid.draw();
        }

        drawText();
    }

    private void drawText() {
        textRenderer.addStr2D(5, 25, String.format("[TAB] Projection type: %s", isPerspectiveProjection ? "perspective" : "orthogonal"));
        textRenderer.addStr2D(5, 45, String.format("[F] Function type: %s", grid.getFuncType()));
        textRenderer.addStr2D(5, 65, String.format("[C] Color mode: %s", grid.getColorMode()));
        textRenderer.addStr2D(5, 85, String.format("[P] Polygon mode: %s", polygonMode));
        textRenderer.addStr2D(5, 105, String.format("[T] Texture: %s", textureNames.get(textureIndex)));
    }

    private void changePolygonMode() {
        PolygonMode[] polygonModes = PolygonMode.values();
        polygonMode = polygonModes[(polygonMode.ordinal() + 1) % polygonModes.length];
    }

    private void updateProjectionMatrix() {
        projectionMatrix = isPerspectiveProjection
                ? new Mat4PerspRH(Math.toRadians(70), (double) height / (double) width, 0.01, 100)
                : new Mat4OrthoRH(5 * ((double) width / height), 5, 0.01, 100);
        updateGrids();
    }

    private void changeColorMode() {
        Grid.ColorMode[] colorModes = Grid.ColorMode.values();
        Grid.ColorMode colorMode = grid.getColorMode();
        grid.setColorMode(colorModes[(colorMode.ordinal() + 1) % colorModes.length]);
    }

    private void changeFunctionType() {
        Grid.FuncType[] functionTypes = Grid.FuncType.values();
        Grid.FuncType functionType = grid.getFuncType();
        grid.setFuncType(functionTypes[(functionType.ordinal() + 1) % functionTypes.length]);
    }

    private void onKeyPress(int key, int mods) {
        switch (key) {
            case GLFW.GLFW_KEY_P:
                changePolygonMode();
                break;
            case GLFW.GLFW_KEY_TAB:
                isPerspectiveProjection = !isPerspectiveProjection;
                updateProjectionMatrix();
                break;

            case GLFW.GLFW_KEY_C:
                changeColorMode();
                break;
            case GLFW.GLFW_KEY_F:
                changeFunctionType();
                break;
            case GLFW.GLFW_KEY_T:
                textureIndex = (textureIndex + 1) % textures.size();
                setTexture(grid);
                break;
        }
    }

    private void moveCamera(int key) {
        double speed = 0.02;
        switch (key) {
            case GLFW.GLFW_KEY_W -> camera = camera.forward(speed);
            case GLFW.GLFW_KEY_S -> camera = camera.backward(speed);
            case GLFW.GLFW_KEY_A -> camera = camera.left(speed);
            case GLFW.GLFW_KEY_D -> camera = camera.right(speed);
            case GLFW.GLFW_KEY_SPACE -> camera = camera.up(speed);
            case GLFW.GLFW_KEY_X -> camera = camera.down(speed);
        }
        updateGrids();
    }

    private void moveLight(int key) {
        double speed = 0.02;
        final Vec3D direction;
        switch (key) {
            case GLFW.GLFW_KEY_RIGHT -> direction = new Vec3D(speed, 0, 0);
            case GLFW.GLFW_KEY_LEFT -> direction = new Vec3D(-speed, 0, 0);
            case GLFW.GLFW_KEY_UP -> direction = new Vec3D(0, speed, 0);
            case GLFW.GLFW_KEY_DOWN -> direction = new Vec3D(0, -speed, 0);
            default -> direction  = new Vec3D(0, 0, 0);
        }
        light.translate(direction);
        lightPosition = lightPosition.add(direction);
    }

    private void rotateCamera(double x, double y) {
        camera = camera.addAzimuth(Math.PI * (mouseOrigin[0] - x) / width)
                .addZenith(Math.PI * (mouseOrigin[1] - y) / height);
        mouseOrigin[0] = x;
        mouseOrigin[1] = y;
        updateGrids();
    }

    private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW.GLFW_PRESS) {
                onKeyPress(key, mods);
            }

            if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                moveCamera(key);
                moveLight(key);
            }
        }
    };

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    private final GLFWMouseButtonCallback mbCallback = new GLFWMouseButtonCallback() {

        @Override
        public void invoke(long window, int button, int action, int mods) {
            DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
            glfwGetCursorPos(window, xBuffer, yBuffer);
            double x = xBuffer.get(0);
            double y = yBuffer.get(0);

            if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
                if (action == GLFW.GLFW_PRESS) {
                    isMousePressed = true;
                    mouseOrigin[0] = x;
                    mouseOrigin[1] = y;
                } else if (action == GLFW.GLFW_RELEASE) {
                    isMousePressed = false;
                    rotateCamera(x, y);
                }
            }
        }
    };

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mbCallback;
    }

    private final GLFWCursorPosCallback cpCallbacknew = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (isMousePressed) rotateCamera(x, y);
        }
    };

    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cpCallbacknew;
    }

    protected GLFWWindowSizeCallback wsCallback = new GLFWWindowSizeCallback() {
        @Override
        public void invoke(long window, int w, int h) {
            width = w;
            height = h;
            updateProjectionMatrix();
            if (textRenderer != null) {
                textRenderer.resize(width, height);
            }
        }
    };

    @Override
    public GLFWWindowSizeCallback getWsCallback() {
        return wsCallback;
    }
}
