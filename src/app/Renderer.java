package app;

import app.solid.Grid;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import transforms.*;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART;

public class Renderer extends AbstractRenderer {

    private Camera camera;
    private Mat4 projectionMatrix;
    private final List<Grid> grids = new ArrayList<>();
    private int polygonMode = GL_FILL;
    private boolean isPerspectiveProjection = true;
    private boolean isMousePressed = false;
    private double[] mouseOrigin = new double[2];

    @Override
    public void init() {
        Grid grid = new Grid();
        Grid ball = new Grid(50, 50, GL_TRIANGLES, Grid.FuncType.WAVE);
        ball.setColor(1, 0, 1);
        ball.scale(new Vec3D(0.5));

        grids.add(grid);
        grids.add(ball);

        camera = new Camera()
                .withPosition(new Vec3D(0, -2, 1))
                .withAzimuth(Math.toRadians(90))
                .withZenith(Math.toRadians(-15))
                .withFirstPerson(true);
        updateProjectionMatrix();

        updateGrids();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_PRIMITIVE_RESTART);
    }

    private void updateGrids() {
        for (Grid grid : grids) {
            grid.setProjectionMatrix(projectionMatrix);
            grid.setViewMatrix(camera.getViewMatrix());
        }
    }

    @Override
    public void display() {
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glPolygonMode(GL_FRONT_AND_BACK, polygonMode);

        for (Grid grid : grids) {
            grid.draw();
        }
    }

    private void changePolygonMode() {
        switch (polygonMode) {
            case GL_FILL -> polygonMode = GL_LINE;
            case GL_LINE -> polygonMode = GL_POINT;
            case GL_POINT -> polygonMode = GL_FILL;
        }
    }

    private void updateProjectionMatrix() {
        projectionMatrix = isPerspectiveProjection
                ? new Mat4PerspRH(Math.toRadians(70), (double) height / (double) width, 0.01, 100)
                : new Mat4OrthoRH(5 * ((double) width / height), 5, 0.01, 100);
        updateGrids();
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

            case GLFW.GLFW_KEY_UP:
                grids.get(1).translate(new Vec3D(0, 0, 0.5));
                break;
            case GLFW.GLFW_KEY_DOWN:
                grids.get(1).translate(new Vec3D(0, 0, -0.5));
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
        }
    };

    @Override
    public GLFWWindowSizeCallback getWsCallback() {
        return wsCallback;
    }
}
