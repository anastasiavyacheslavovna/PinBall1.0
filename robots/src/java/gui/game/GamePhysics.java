package gui.game;

import log.Logger;

public class GamePhysics {
    public static final double GRAVITY = 0.1;
    public static final double FRICTION = 0.99;
    public static final double BOUNCE_DAMPING = 0.8;
    public static final double BALL_RADIUS = 6;

    private final GameVisualizer visualizer;

    public GamePhysics(GameVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    public double getBallRadius() {
        return BALL_RADIUS;
    }

    public void updatePhysics() {
        double ballX = visualizer.getBallPositionX();
        double ballY = visualizer.getBallPositionY();
        double velocityX = visualizer.getBallVelocityX();
        double velocityY = visualizer.getBallVelocityY();

        ballX += velocityX;
        ballY += velocityY;
        velocityY += GRAVITY;
        velocityX *= FRICTION;
        velocityY *= FRICTION;

        visualizer.setBallPositionX(ballX);
        visualizer.setBallPositionY(ballY);
        visualizer.setBallVelocityX(velocityX);
        visualizer.setBallVelocityY(velocityY);
    }


    public void checkCollisions() {
        if (visualizer.getBallPositionY() < visualizer.getBorderMargin() + BALL_RADIUS) {
            visualizer.setBallVelocityY(Math.abs(visualizer.getBallVelocityY()));
        }

        checkTargetsCollisions();
        checkBumpersCollisions();
        checkFunnelCollisions();
        checkBallLoss();
        checkFlipperCollisions();
    }


    private void checkFunnelCollisions() {
        double funnelLeftX = visualizer.getFunnelLeftX();
        double funnelLeftY = visualizer.getFunnelLeftY();
        double topLeftX = visualizer.getTopLeftX();
        double topLeftY = visualizer.getTopLeftY();
        double topRightX = visualizer.getTopRightX();
        double topRightY = visualizer.getTopRightY();
        double funnelRightX = visualizer.getFunnelRightX();
        double funnelRightY = visualizer.getFunnelRightY();


        if (checkLineCollision(funnelLeftX, funnelLeftY, topLeftX, topLeftY, true)) {
            return;
        }


        if (checkLineCollision(funnelRightX, funnelRightY, topRightX, topRightY, false)) {
            return;
        }


        if (visualizer.getBallPositionY() >= funnelLeftY + visualizer.getFlipperLength() - BALL_RADIUS) {
            visualizer.setBallPositionY(funnelLeftY - BALL_RADIUS);
            visualizer.setBallVelocityY(-Math.abs(visualizer.getBallVelocityY()));
        }
    }


    private boolean checkLineCollision(double x1, double y1, double x2, double y2, boolean isLeft) {
        double distance = pointToLineDistance(visualizer.getBallPositionX(), visualizer.getBallPositionY(),
                x1, y1, x2, y2);

        if (distance < BALL_RADIUS) {
            double closestX = closestPointOnLine(visualizer.getBallPositionX(), x1, x2);
            double closestY = closestPointOnLine(visualizer.getBallPositionY(), y1, y2);

            double dx = visualizer.getBallPositionX() - closestX;
            double dy = visualizer.getBallPositionY() - closestY;
            double distanceToClosest = Math.sqrt(dx * dx + dy * dy);

            if (distanceToClosest < BALL_RADIUS) {
                Logger.debug("шарик столкнулся со стенкой");

                if (isLeft) {
                    visualizer.setBallVelocityX(Math.abs(visualizer.getBallVelocityY()));
                } else {
                    visualizer.setBallVelocityX(-Math.abs(visualizer.getBallVelocityY()));
                }

                if (visualizer.getBallVelocityY() < 0) {
                    visualizer.setBallVelocityY(Math.abs(visualizer.getBallVelocityX()) + 1);
                } else {
                    visualizer.setBallVelocityY(-Math.abs(visualizer.getBallVelocityX()) - 1);
                }
                return true;
            }
        }
        return false;
    }


    private void checkBallLoss() {
        if (visualizer.getBallPositionY() > visualizer.getFunnelLeftY() + visualizer.getFlipperLength() / 2) {
            visualizer.setBallLost(true);
            visualizer.setLives(visualizer.getLives() - 1);
            Logger.debug("Шарик потерян! Осталось жизней: " + visualizer.getLives());

            if (visualizer.getLives() <= 0) {
                Logger.debug("ИГРА ОКОНЧЕНА! Финальный счет: " + visualizer.getScore());
            }
        }
    }


    private void checkFlipperCollisions() {
        checkFlipperCollision(visualizer.getLeftFlipperPivotX(), visualizer.getLeftFlipperPivotY(),
                visualizer.getLeftFlipperAngle(), visualizer.isLeftFlipperActive(), true);
        checkFlipperCollision(visualizer.getRightFlipperPivotX(), visualizer.getRightFlipperPivotY(),
                visualizer.getRightFlipperAngle(), visualizer.isRightFlipperActive(), false);
    }


    private void checkFlipperCollision(double pivotX, double pivotY, double angle,
                                       boolean isActive, boolean isLeftFlipper) {

        double[] endPoint = visualizer.getFlipperController().calculateFlipperEndPoint(
                pivotX, pivotY, angle, isLeftFlipper);
        double flipperEndX = endPoint[0];
        double flipperEndY = endPoint[1];

        double distanceToLine = pointToLineDistance(visualizer.getBallPositionX(), visualizer.getBallPositionY(),
                pivotX, pivotY, flipperEndX, flipperEndY);

        if (distanceToLine < BALL_RADIUS + visualizer.getFlipperWidth() / 2) {
            if (isLeftFlipper) {
                visualizer.setBallVelocityX(Math.abs(visualizer.getBallVelocityX()) * BOUNCE_DAMPING + 2.0);
            } else {
                visualizer.setBallVelocityX(-Math.abs(visualizer.getBallVelocityX()) * BOUNCE_DAMPING - 2.0);
            }

            visualizer.setBallVelocityY(-Math.abs(visualizer.getBallVelocityY()) * BOUNCE_DAMPING - 3.0);

            if (isActive) {
                if (isLeftFlipper) {
                    visualizer.setBallVelocityX(visualizer.getBallVelocityX() + 3.0);
                } else {
                    visualizer.setBallVelocityX(visualizer.getBallVelocityX() - 3.0);
                }
                visualizer.setBallVelocityY(visualizer.getBallVelocityY() - 4.0);
            }

            visualizer.setScore(visualizer.getScore() + 10);
            Logger.debug((isLeftFlipper ? "Левый" : "Правый") + " флиппер: отскок!");
        }
    }


    private void checkTargetsCollisions() {
        for (Target target : visualizer.getTargets()) {
            if (target.checkCollisionTarget(visualizer.getBallPositionX(), visualizer.getBallPositionY(), BALL_RADIUS)) {
                int points = target.hit();
                visualizer.setScore(visualizer.getScore() + points);

                double[] bounce = target.getBounceDirection(visualizer.getBallPositionX(), visualizer.getBallPositionY());
                visualizer.setBallVelocityX(visualizer.getBallVelocityX() + bounce[0]);
                visualizer.setBallVelocityY(visualizer.getBallVelocityY() + bounce[1]);

                Logger.debug("Попадание в мишень! +" + points + " очков");
            }
        }
    }


    private void checkBumpersCollisions() {
        for (Bumper bumper : visualizer.getBumpers()) {
            if (bumper.checkCollisionBumper(visualizer.getBallPositionX(), visualizer.getBallPositionY(), BALL_RADIUS)) {
                int points = bumper.hit();
                visualizer.setScore(visualizer.getScore() + points);

                double[] bounce = bumper.getBounceDirection(visualizer.getBallPositionX(), visualizer.getBallPositionY());
                visualizer.setBallVelocityX(visualizer.getBallVelocityX() + bounce[0]);
                visualizer.setBallVelocityY(visualizer.getBallVelocityY() + bounce[1]);

                Logger.debug("Столкновение с бампером! +" + points + " очков");
            }
        }
    }


    private double pointToLineDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = (len_sq != 0) ? dot / len_sq : -1;

        double xx, yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double closestPointOnLine(double p, double a, double b) {
        double t = Math.max(0, Math.min(1, (p - a) / (b - a)));
        return a + t * (b - a);
    }
}