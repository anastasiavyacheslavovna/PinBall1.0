package gui;

import gui.game.GamePhysics;
import gui.game.GameVisualizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static gui.game.GamePhysics.BALL_RADIUS;
import static org.junit.jupiter.api.Assertions.*;

public class GameVisualizerTest {
    private GameVisualizer gameVisualizer;
    private GamePhysics physics;

    @BeforeEach
    public void setUp() {
        gameVisualizer = new GameVisualizer();
        gameVisualizer.setBounds(0, 0, 800, 600);

        try {
            Field physicsField = GameVisualizer.class.getDeclaredField("m_physics");
            physicsField.setAccessible(true);
            physics = (GamePhysics) physicsField.get(gameVisualizer);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGameEndsAfterThreeBallLosses() {
        gameVisualizer.setLives(3);
        gameVisualizer.setBallLost(false);

        for (int i = 0; i < 3; i++) {
            gameVisualizer.setBallPositionY(gameVisualizer.getFunnelLeftY() + gameVisualizer.getFlipperLength() + 50);
            invokeCheckBallLoss();

            assertEquals(2 - i, gameVisualizer.getLives());
        }

        assertEquals(0, gameVisualizer.getLives());
        assertTrue(gameVisualizer.isBallLost());
    }

    @Test
    public void testBallBouncesOffTopBorder() {
        gameVisualizer.setBallPositionY(gameVisualizer.getBorderMargin() + 3);
        gameVisualizer.setBallVelocityY(-5.0);

        invokeCheckCollisions();

        double newVelocity = gameVisualizer.getBallVelocityY();

        assertTrue(newVelocity > 0);
    }


    @ParameterizedTest
    @MethodSource("wallCollisionProvider")
    void testWallCollisionIsolated(boolean isLeftWall, double initialVelocityX, double initialVelocityY) {
        double wallXFirst = isLeftWall ? gameVisualizer.getFunnelLeftX() : gameVisualizer.getFunnelRightX();
        double wallXSecond = isLeftWall ? gameVisualizer.getTopLeftX() : gameVisualizer.getTopRightX();
        double wallYFirst = gameVisualizer.getFunnelLeftY();
        double wallYSecond = gameVisualizer.getTopLeftY();

        double ballX = isLeftWall ? (wallXFirst + wallXSecond) / 2 + BALL_RADIUS - 0.1
                : (wallXFirst + wallXSecond) / 2 - BALL_RADIUS + 0.1;
        double ballY = wallYFirst + (wallYSecond - wallYFirst) / 2;

        gameVisualizer.setBallPositionX(ballX);
        gameVisualizer.setBallPositionY(ballY);
        gameVisualizer.setBallVelocityX(initialVelocityX);
        gameVisualizer.setBallVelocityY(initialVelocityY);

        try {
            Method checkLineCollisionMethod = GamePhysics.class.getDeclaredMethod(
                    "checkLineCollision", double.class, double.class, double.class, double.class, boolean.class);
            checkLineCollisionMethod.setAccessible(true);

            boolean collision = (boolean) checkLineCollisionMethod.invoke(physics,
                    wallXFirst, wallYFirst, wallXSecond, wallYSecond, isLeftWall);

            assertTrue(collision);

            double finalVelocityX = gameVisualizer.getBallVelocityX();
            double finalVelocityY = gameVisualizer.getBallVelocityY();

            double expectedVelocityX = isLeftWall ?
                    Math.abs(initialVelocityY) :
                    -Math.abs(initialVelocityY);
            double expectedVelocityY = -Math.abs(initialVelocityX) - 1;

            assertEquals(expectedVelocityX, finalVelocityX, 0.001);
            assertEquals(expectedVelocityY, finalVelocityY, 0.001);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    private static Stream<Arguments> wallCollisionProvider() {
        return Stream.of(
                // левая стенка
                Arguments.of(true, 0.0, 5.0),     // падает сверху вниз
                Arguments.of(true, -3.0, 0.0),    // влетает сбоку
                Arguments.of(true, -2.0, 4.0),    // влетает сверху-справа
                Arguments.of(true, -3.0, -2.0),   // влетает снизу-справа
                Arguments.of(true, 1.0, 5.0),     // влетает сверху-слева

                // правая стенка
                Arguments.of(false, 0.0, 5.0),    // падает сверху вниз
                Arguments.of(false, 3.0, 0.0),    // влетает сбоку
                Arguments.of(false, 2.0, 4.0),    // слева-сверху
                Arguments.of(false, 3.0, -2.0),   // слева-снизу
                Arguments.of(false, -1.0, 5.0)    // влетает сверху-справа
        );
    }

    @Test
    public void testScoreIncreasesWhenHittingFlipper() {
        gameVisualizer.setScore(0);

        gameVisualizer.setBallPositionX(gameVisualizer.getLeftFlipperPivotX() + 10);
        gameVisualizer.setBallPositionY(gameVisualizer.getLeftFlipperPivotY() + 5);
        gameVisualizer.setBallVelocityX(2.0);
        gameVisualizer.setBallVelocityY(2.0);

        int initialScore = gameVisualizer.getScore();

        invokeCheckFlipperCollisions();

        int newScore = gameVisualizer.getScore();

        assertTrue(newScore > initialScore);
    }

    @Test
    public void testBallPhysicsGravity() {
        gameVisualizer.setBallPositionX(100);
        gameVisualizer.setBallPositionY(100);
        gameVisualizer.setBallVelocityX(0);
        gameVisualizer.setBallVelocityY(0);

        double initialVelocityY = gameVisualizer.getBallVelocityY();

        physics.updatePhysics();

        double newVelocityY = gameVisualizer.getBallVelocityY();

        assertTrue(newVelocityY > initialVelocityY);
    }


    private void invokeCheckBallLoss() {
        try {
            Method method = GamePhysics.class.getDeclaredMethod("checkBallLoss");
            method.setAccessible(true);
            method.invoke(physics);
        } catch (Exception e) {
            fail("Не удалось вызвать checkBallLoss: " + e.getMessage());
        }
    }

    private void invokeCheckCollisions() {
        try {
            Method method = GamePhysics.class.getDeclaredMethod("checkCollisions");
            method.setAccessible(true);
            method.invoke(physics);
        } catch (Exception e) {
            fail("Не удалось вызвать checkCollisions: " + e.getMessage());
        }
    }

    private void invokeCheckFlipperCollisions() {
        try {
            Method method = GamePhysics.class.getDeclaredMethod("checkFlipperCollisions");
            method.setAccessible(true);
            method.invoke(physics);
        } catch (Exception e) {
            fail("Не удалось вызвать checkFlipperCollisions: " + e.getMessage());
        }
    }
}