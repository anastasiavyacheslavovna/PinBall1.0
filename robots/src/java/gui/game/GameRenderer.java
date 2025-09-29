package gui.game;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class GameRenderer {
    private final GameVisualizer visualizer;

    public GameRenderer(GameVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    public void render(Graphics2D g2d) {
        if (visualizer.getBackgroundImage() != null) {
            g2d.drawImage(visualizer.getBackgroundImage(), 0, 0,
                    visualizer.getWidth(), visualizer.getHeight(), visualizer);
        } else {
            g2d.setColor(new Color(20, 20, 73));
            g2d.fillRect(0, 0, visualizer.getWidth(), visualizer.getHeight());
        }

        g2d.setColor(Color.WHITE);
        g2d.drawRect(visualizer.getBorderMargin(), visualizer.getBorderMargin(),
                visualizer.getFieldWidth(), visualizer.getFieldHeight());

        drawPlayingField(g2d);
        drawBall(g2d);
        drawFlippers(g2d);
        drawInfo(g2d);
        drawTargetsAndBumpers(g2d);
    }

    private void drawPlayingField(Graphics2D g2d) {
        g2d.setColor(new Color(40, 40, 80));

        Polygon fieldPolygon = getPolygon();

        g2d.fillPolygon(fieldPolygon);

        g2d.setColor(Color.WHITE);
        g2d.drawLine((int)visualizer.getTopLeftX(), (int)visualizer.getTopLeftY(),
                (int)visualizer.getTopRightX(), (int)visualizer.getTopRightY());
        g2d.drawLine((int)visualizer.getTopLeftX(), (int)visualizer.getTopLeftY(),
                (int)visualizer.getFunnelLeftX(), (int)visualizer.getFunnelLeftY());
        g2d.drawLine((int)visualizer.getTopRightX(), (int)visualizer.getTopRightY(),
                (int)visualizer.getFunnelRightX(), (int)visualizer.getFunnelRightY());
        g2d.drawLine((int)visualizer.getFunnelLeftX(),
                (int)visualizer.getFunnelLeftY() + (int)visualizer.getFlipperLength(),
                (int)visualizer.getFunnelRightX(),
                (int)visualizer.getFunnelRightY() + (int)visualizer.getFlipperLength());

        g2d.setColor(new Color(255, 0, 0, 50));
        g2d.fillRect((int)visualizer.getFunnelLeftX(),
                (int)visualizer.getFunnelLeftY() + (int)visualizer.getFlipperLength(),
                (int)(visualizer.getFunnelRightX() - visualizer.getFunnelLeftX()),
                visualizer.getHeight() - (int)visualizer.getFunnelLeftY() + (int)visualizer.getFlipperLength());
    }

    private Polygon getPolygon() {
        Polygon fieldPolygon = new Polygon();
        fieldPolygon.addPoint((int)visualizer.getTopLeftX(), (int)visualizer.getTopLeftY());
        fieldPolygon.addPoint((int)visualizer.getTopRightX(), (int)visualizer.getTopRightY());
        fieldPolygon.addPoint((int)visualizer.getFunnelRightX(), (int)visualizer.getFunnelRightY());
        fieldPolygon.addPoint((int)visualizer.getFunnelRightX(),
                (int)visualizer.getFunnelRightY() + (int)visualizer.getFlipperLength());
        fieldPolygon.addPoint((int)visualizer.getFunnelLeftX(),
                (int)visualizer.getFunnelLeftY() + (int)visualizer.getFlipperLength());
        fieldPolygon.addPoint((int)visualizer.getFunnelLeftX(), (int)visualizer.getFunnelLeftY());
        return fieldPolygon;
    }

    private void drawBall(Graphics2D g2d) {
        double ballRadius = visualizer.getBallRadius();
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(
                (int)(visualizer.getBallPositionX() - ballRadius),
                (int)(visualizer.getBallPositionY() - ballRadius),
                (int)(ballRadius * 2),
                (int)(ballRadius * 2)
        );

        g2d.setColor(Color.ORANGE);
        g2d.drawOval(
                (int)(visualizer.getBallPositionX() - ballRadius),
                (int)(visualizer.getBallPositionY() - ballRadius),
                (int)(ballRadius * 2),
                (int)(ballRadius * 2)
        );
    }

    private void drawFlippers(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();

        drawFlipper(g2d, visualizer.getLeftFlipperPivotX(), visualizer.getLeftFlipperPivotY(),
                visualizer.getLeftFlipperAngle(), visualizer.isLeftFlipperActive(), true);

        drawFlipper(g2d, visualizer.getRightFlipperPivotX(), visualizer.getRightFlipperPivotY(),
                visualizer.getRightFlipperAngle(), visualizer.isRightFlipperActive(), false);

        g2d.setTransform(oldTransform);
    }

    private void drawFlipper(Graphics2D g2d, double pivotX, double pivotY,
                             double angle, boolean isActive, boolean isLeftFlipper) {

        double flipperWidth = visualizer.getFlipperWidth();

        double[] endPoint = visualizer.getFlipperController().calculateFlipperEndPoint(
                pivotX, pivotY, angle, isLeftFlipper);

        double endX = endPoint[0];
        double endY = endPoint[1];

        g2d.setColor(isActive ? Color.CYAN : Color.GRAY);
        g2d.setStroke(new BasicStroke((float)flipperWidth,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g2d.drawLine((int)pivotX, (int)pivotY, (int)endX, (int)endY);

        g2d.setStroke(new BasicStroke(1.0f));

        g2d.setColor(Color.RED);
        g2d.fillOval((int)pivotX - 3, (int)pivotY - 3, 6, 6);
    }

    private void drawInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.drawString("Управление: ←/A - левый флиппер, →/D - правый флиппер", 10, 20);
        g2d.drawString("Пробел - запуск шарика, R - сброс, F - полноэкранный режим", 10, 40);
        g2d.drawString(String.format("Скорость: X=%.1f Y=%.1f",
                visualizer.getBallVelocityX(), visualizer.getBallVelocityY()), 10, 60);
        g2d.drawString(String.format("Счет: %d", visualizer.getScore()), 10, 80);
        g2d.drawString(String.format("Жизни: %d", visualizer.getLives()), 10, 100);

        if (visualizer.isBallLost()) {
            if (visualizer.getLives() > 0) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("Шарик потерян! Нажмите ПРОБЕЛ для продолжения",
                        visualizer.getWidth() / 2 - 150, visualizer.getHeight() / 2);
            } else {
                g2d.setColor(Color.RED);
                g2d.drawString("ИГРА ОКОНЧЕНА! Финальный счет: " + visualizer.getScore(),
                        visualizer.getWidth() / 2 - 100, visualizer.getHeight() / 2);
            }
        }
    }

    private void drawTargetsAndBumpers(Graphics2D g2d) {
        for (Target target : visualizer.getTargets()) {
            target.draw(g2d);
        }

        for (Bumper bumper : visualizer.getBumpers()) {
            bumper.draw(g2d);
        }
    }
}