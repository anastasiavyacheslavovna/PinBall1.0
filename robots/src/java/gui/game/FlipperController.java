package gui.game;

import java.awt.event.KeyEvent;

public class FlipperController {
    //константы флипперов
    public static final double FLIPPER_LENGTH = 60;
    public static final double FLIPPER_WIDTH = 10;
    public static final double FLIPPER_REST_ANGLE = Math.PI / 6;
    public static final double FLIPPER_ACTIVE_ANGLE = -Math.PI / 6;
    public static final double FLIPPER_ROTATION_SPEED = 0.4;

    private final GameVisualizer visualizer;

    public FlipperController(GameVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    public void updateFlippers() {
        double newAngle;

        if (visualizer.isLeftFlipperActive()) {
            newAngle = Math.max(visualizer.getLeftFlipperAngle() - FLIPPER_ROTATION_SPEED,
                    FLIPPER_ACTIVE_ANGLE);
        } else {
            newAngle = Math.min(visualizer.getLeftFlipperAngle() + FLIPPER_ROTATION_SPEED,
                    FLIPPER_REST_ANGLE);
        }
        visualizer.setLeftFlipperAngle(newAngle);

        if (visualizer.isRightFlipperActive()) {
            newAngle = Math.min(visualizer.getRightFlipperAngle() + FLIPPER_ROTATION_SPEED,
                    -FLIPPER_ACTIVE_ANGLE);
            visualizer.setRightFlipperAngle(newAngle);
        } else {
            newAngle = Math.max(visualizer.getRightFlipperAngle() - FLIPPER_ROTATION_SPEED,
                    -FLIPPER_REST_ANGLE);
            visualizer.setRightFlipperAngle(newAngle);
        }
    }


    public void handleKeyPress(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                visualizer.setLeftFlipperActive(pressed);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                visualizer.setRightFlipperActive(pressed);
                break;
        }
    }


    public double[] calculateFlipperEndPoint(double pivotX, double pivotY,
                                             double angle, boolean isLeftFlipper) {
        double endX, endY;

        if (isLeftFlipper) {
            endX = pivotX + Math.cos(angle) * FLIPPER_LENGTH;
            endY = pivotY + Math.sin(angle) * FLIPPER_LENGTH;
        } else {
            endX = pivotX + Math.cos(angle + Math.PI) * FLIPPER_LENGTH;
            endY = pivotY + Math.sin(angle + Math.PI) * FLIPPER_LENGTH;
        }

        return new double[]{endX, endY};
    }


    public double getFlipperLength() { return FLIPPER_LENGTH; }
    public double getFlipperWidth() { return FLIPPER_WIDTH; }
}