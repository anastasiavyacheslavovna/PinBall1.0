package gui;

import gui.game.GamePhysics;
import gui.game.GameVisualizer;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class GameVisualizerTest {
    private GameVisualizer gameVisualizer;
    private GamePhysics physics;

    @Before
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

    @Test
    public void testBallBouncesOffLine() {
        double ballRadius = 6.0;
        double wallX = gameVisualizer.getFunnelRightX();

        gameVisualizer.setBallPositionX(wallX + ballRadius - 2);
        gameVisualizer.setBallVelocityX(2.0);
        gameVisualizer.setBallPositionY(wallX + 50);
        gameVisualizer.setBallVelocityY(0);

        for (int i = 0; i < 3; i++) {
            physics.updatePhysics();
            invokeCheckCollisions();
        }

        double newVelocityX = gameVisualizer.getBallVelocityX();

        assertTrue(newVelocityX < 0);
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