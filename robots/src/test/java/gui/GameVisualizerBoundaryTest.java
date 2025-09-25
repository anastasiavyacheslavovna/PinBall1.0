package gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

//робот остается в пределах границ игрового поля
class GameVisualizerBoundaryTest {

    private GameVisualizer visualizer;

    @BeforeEach
    void setUp() {
        visualizer = new GameVisualizer();
        visualizer.setBounds(0, 0, 600, 400);
    }

    @Test
    void testRobotStaysWithinBounds() throws Exception {

        setPrivateField("m_targetPositionX", 1000);
        setPrivateField("m_targetPositionY", 1000);

        for (int i = 0; i < 100; i++) {
            callPrivateMethod("onModelUpdateEvent");
        }

        double endX = getPrivateField("m_robotPositionX");
        double endY = getPrivateField("m_robotPositionY");

        assertTrue(endX >= 35, "Робот не должен выходить за левую границу");
        assertTrue(endX <= 565, "Робот не должен выходить за правую границу");
        assertTrue(endY >= 35, "Робот не должен выходить за верхнюю границу");
        assertTrue(endY <= 365, "Робот не должен выходить за нижнюю границу");
    }

    @Test
    void testBoundaryCollisionDetection() throws Exception {
        setPrivateField("m_robotPositionX", 1000);
        setPrivateField("m_robotPositionY", 200);

        callPrivateMethod("checkBoundaryCollision");

        double x = getPrivateField("m_robotPositionX");
        assertTrue(x <= 565, "Робот должен быть возвращен в границы после столкновения");
    }

    private double getPrivateField(String fieldName) throws Exception {
        java.lang.reflect.Field field = GameVisualizer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getDouble(visualizer);
    }

    private void setPrivateField(String fieldName, int value) throws Exception {
        java.lang.reflect.Field field = GameVisualizer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setInt(visualizer, value);
    }

    private void callPrivateMethod(String methodName, Object... params) throws Exception {
        Method method;
        if (params.length > 0 && params[0] instanceof java.awt.Point) {
            method = GameVisualizer.class.getDeclaredMethod(methodName, java.awt.Point.class);
        } else {
            method = GameVisualizer.class.getDeclaredMethod(methodName);
        }
        method.setAccessible(true);
        if (params.length > 0) {
            method.invoke(visualizer, params);
        } else {
            method.invoke(visualizer);
        }
    }
}