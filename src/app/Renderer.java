package app;

import app.solid.Grid;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import transforms.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART;

public class Renderer extends AbstractRenderer {

    private Camera camera;
    private Mat4 projectionMatrix;
    private final List<Grid> grids = new ArrayList<>();
    private int polygonMode = GL_FILL;
    private boolean isPerspectiveProjection = true;

    @Override
    public void init() {
        Grid grid = new Grid();
        Grid ball = new Grid(50, 50, GL_TRIANGLES, Grid.FUNC_TYPE_WAVE);
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

    private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW.GLFW_PRESS) {
                onKeyPress(key, mods);
            }
        }
    };

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
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
