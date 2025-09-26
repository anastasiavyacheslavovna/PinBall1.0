package gui;

import log.Logger;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class GameVisualizer extends JPanel
{
    private static Timer initTimer()
    {
        return new Timer("events generator", true);
    }

    //парамерты шарика
    private volatile double m_ballPositionX = 300;
    private volatile double m_ballPositionY = 300;
    private volatile double m_ballVelocityX = 0;
    private volatile double m_ballVelocityY = 0;
    private static final double BALL_RADIUS = 6;

    private enum GameState {
        READY,
        PLAYING,
        GAME_OVER,
        PAUSED
    }

    private volatile GameState m_gameState = GameState.READY;

    //флиперов
    private volatile double m_leftFlipperAngle = Math.PI / 6;
    private volatile double m_rightFlipperAngle = -Math.PI / 6;
    private static final double FLIPPER_LENGTH = 60;
    private static final double FLIPPER_WIDTH = 10;
    //углы
    private static final double FLIPPER_REST_ANGLE = Math.PI / 6;
    private static final double FLIPPER_ACTIVE_ANGLE = -Math.PI / 6;
    private static final double FLIPPER_ROTATION_SPEED = 0.4;
    //точки вращения
    private double m_leftFlipperPivotX;
    private double m_leftFlipperPivotY;
    private double m_rightFlipperPivotX;
    private double m_rightFlipperPivotY;

    //физика
    private static final double GRAVITY = 0.1;
    private static final double FRICTION = 0.99;
    private static final double BOUNCE_DAMPING = 0.8;

    //границы поля
    private static final int BORDER_MARGIN = 20;
    private int m_fieldWidth = 500;
    private int m_fieldHeight = 400;

    //управление
    private boolean m_leftFlipperActive = false;
    private boolean m_rightFlipperActive = false;
    private int m_score = 0;
    private int m_lives = 3;
    private boolean m_ballLost = false;

    //воронка
    private double m_funnelWidth;
    private double m_topLeftX, m_topLeftY;
    private double m_topRightX, m_topRightY;
    private double m_funnelLeftX, m_funnelLeftY;
    private double m_funnelRightX, m_funnelRightY;

    private Timer m_timer;

    private java.util.List<Target> m_targets = new java.util.ArrayList<>();
    private java.util.List<Bumper> m_bumpers = new java.util.ArrayList<>();

    private Image m_backgroundImage;
    private Image m_funnelImage;
    private Image m_fieldImage;


    public GameVisualizer()
    {
        setBackground(Color.BLACK);
        updateFieldDimensions();

        loadImages();

        m_timer = initTimer();
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onRedrawEvent();
            }
        }, 0, 50); // 20 FPS

        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onModelUpdateEvent();
            }
        }, 0, 16); // 60 FPS для физики

        //обработка флиперов
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyPress(e.getKeyCode(), false);
            }
        });

        //обработка старта шарика
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                launchBall();
            }
        });

        setDoubleBuffered(true);
        initializeTargets();
        initializeBumpers();
    }

    // обновление размеров поля
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        updateFieldDimensions();
    }

    //обновление игрового поля
    private void updateFieldDimensions() {
        m_fieldWidth = getWidth() - BORDER_MARGIN * 2;
        m_fieldHeight = getHeight() - BORDER_MARGIN * 2;

        //воронка
        m_topLeftX = BORDER_MARGIN;
        m_topLeftY = BORDER_MARGIN;
        m_topRightX = BORDER_MARGIN + m_fieldWidth;
        m_topRightY = BORDER_MARGIN;

        m_funnelWidth = FLIPPER_LENGTH * 3.2;

        double funnelHeight = m_fieldHeight * 0.25;

        m_funnelLeftX = BORDER_MARGIN + (m_fieldWidth - m_funnelWidth) / 2;
        m_funnelLeftY = BORDER_MARGIN + m_fieldHeight - funnelHeight;
        m_funnelRightX = BORDER_MARGIN + (m_fieldWidth + m_funnelWidth) / 2;
        m_funnelRightY = BORDER_MARGIN + m_fieldHeight - funnelHeight;

        //флиперы
        double centerX = BORDER_MARGIN + m_fieldWidth / 2.0;
        m_leftFlipperPivotX = centerX - m_funnelWidth / 2;
        m_leftFlipperPivotY = m_funnelLeftY;
        m_rightFlipperPivotX = centerX + m_funnelWidth / 2;
        m_rightFlipperPivotY = m_funnelLeftY;

        initializeTargets();
        initializeBumpers();

        resetBallPosition();
    }

    // изменение позиции шарика
    private void resetBallPosition() {
        m_ballPositionX = BORDER_MARGIN + m_fieldWidth / 2.0;
        m_ballPositionY = BORDER_MARGIN + m_fieldHeight / 3.0;
        m_ballVelocityX = 0;
        m_ballVelocityY = 0;
        m_ballLost = false;
    }

    //запуск
    private void startGame() {
        if (m_gameState == GameState.READY || m_gameState == GameState.GAME_OVER) {
            //new
            m_score = 0;
            m_lives = 3;
            resetBallPosition();
            m_gameState = GameState.PLAYING;
            launchBall();
            Logger.debug("Игра началась!");
        }
        else if (m_gameState == GameState.PLAYING && m_ballLost) {
            //old
            if (m_lives > 0) {
                resetBallPosition();
                m_ballLost = false;
                launchBall();
                Logger.debug("Продолжение игры. Осталось жизней: " + m_lives);
            }
        }
    }


    private void launchBall() {
        if (m_gameState == GameState.PLAYING && !m_ballLost) {
            m_ballVelocityX = (Math.random() - 0.5) * 8;
            m_ballVelocityY = -Math.random() * 6 - 3;
            Logger.debug("Шарик запущен!");
        }
    }

    private void handleKeyPress(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                m_leftFlipperActive = pressed;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                m_rightFlipperActive = pressed;
                break;
            case KeyEvent.VK_SPACE:
                if (pressed) {
                    if (m_gameState == GameState.READY || m_gameState == GameState.GAME_OVER) {
                        startGame();
                    }
                    else if (m_gameState == GameState.PLAYING) {
                        if (m_ballLost) {
                            startGame();
                        }
                    }
                }
                break;
            case KeyEvent.VK_R:
                resetGame();
                break;
        }
    }

    private void resetGame() {
        m_gameState = GameState.READY;
        m_score = 0;
        m_lives = 3;
        resetBallPosition();
        Logger.debug("Игра сброшена");
    }
    
    protected void onRedrawEvent()
    {
        EventQueue.invokeLater(this::repaint);
    }


    protected void onModelUpdateEvent() {
        if (m_gameState == GameState.PLAYING && !m_ballLost){
            updatePhysics();
            checkCollisions();
        }
        updateFlippers();
    }

    private synchronized void updatePhysics() {
        m_ballPositionX += m_ballVelocityX;
        m_ballPositionY += m_ballVelocityY;

        m_ballVelocityY += GRAVITY;

        m_ballVelocityX *= FRICTION;
        m_ballVelocityY *= FRICTION;
    }

    private synchronized void updateFlippers() {
        if (m_leftFlipperActive) {
            m_leftFlipperAngle = Math.max(m_leftFlipperAngle - FLIPPER_ROTATION_SPEED,
                    FLIPPER_ACTIVE_ANGLE);
        } else {
            m_leftFlipperAngle = Math.min(m_leftFlipperAngle + FLIPPER_ROTATION_SPEED,
                    FLIPPER_REST_ANGLE);
        }

        if (m_rightFlipperActive) {
            m_rightFlipperAngle = Math.min(m_rightFlipperAngle + FLIPPER_ROTATION_SPEED,
                    -FLIPPER_ACTIVE_ANGLE);
        } else {
            m_rightFlipperAngle = Math.max(m_rightFlipperAngle - FLIPPER_ROTATION_SPEED, -FLIPPER_REST_ANGLE);
        }
    }

    private synchronized void checkCollisions() {
        //верх
        if (m_ballPositionY < BORDER_MARGIN + BALL_RADIUS) {
            m_ballVelocityY = Math.abs(m_ballVelocityY);
        }

        checkTargetsCollisions();
        checkBumpersCollisions();
        checkFunnelCollisions();
        checkBallLoss();
        checkFlipperCollisions();
    }


    private void checkFunnelCollisions() {
        //левая
        if (checkLineCollision(m_funnelLeftX, m_funnelLeftY, m_topLeftX, m_topLeftY, true)) {
            return;
        }

        //правая
        if (checkLineCollision(m_funnelRightX, m_funnelRightY, m_topRightX, m_topRightY, false)) {
            return;
        }

        //нижняя
        if (m_ballPositionY >= m_funnelLeftY + FLIPPER_LENGTH - BALL_RADIUS) {

            m_ballPositionY = m_funnelLeftY - BALL_RADIUS;
            m_ballVelocityY = -Math.abs(m_ballVelocityY);
        }
    }

    //столкновение с боковыми
    private boolean checkLineCollision(double x1, double y1, double x2, double y2, boolean isLeft) {
        double distance = pointToLineDistance(m_ballPositionX, m_ballPositionY, x1, y1, x2, y2);

        if (distance < BALL_RADIUS) {
            double closestX = closestPointOnLine(m_ballPositionX, x1, x2);
            double closestY = closestPointOnLine(m_ballPositionY, y1, y2);

            double dx = m_ballPositionX - closestX;
            double dy = m_ballPositionY - closestY;
            double distanceToClosest = Math.sqrt(dx * dx + dy * dy);

            if (distanceToClosest < BALL_RADIUS) {
                Logger.debug("шарик столкнулся со стенкой");

                if (isLeft) {
                    m_ballVelocityX = Math.abs(m_ballVelocityY);
                } else {
                    m_ballVelocityX = -Math.abs(m_ballVelocityY);
                }
                if (m_ballVelocityY < 0) {
                    m_ballVelocityY = Math.abs(m_ballVelocityX) + 1;
                } else {
                    m_ballVelocityY = -Math.abs(m_ballVelocityX) - 1;
                }
                return true;
            }
        }
        return false;
    }


    private void checkBallLoss() {
        if (m_ballPositionY > m_funnelLeftY + FLIPPER_LENGTH / 2) {
            m_ballLost = true;
            m_lives--;
            Logger.debug("Шарик потерян! Осталось жизней: " + m_lives);

            if (m_lives <= 0) {
                Logger.debug("ИГРА ОКОНЧЕНА! Финальный счет: " + m_score);
            }
        }
    }


    private synchronized void checkFlipperCollisions() {
        checkFlipperCollision(m_leftFlipperPivotX, m_leftFlipperPivotY,
                m_leftFlipperAngle, m_leftFlipperActive, true);
        checkFlipperCollision(m_rightFlipperPivotX, m_rightFlipperPivotY,
                m_rightFlipperAngle, m_rightFlipperActive, false);
    }


    private synchronized void checkFlipperCollision(double pivotX, double pivotY, double angle,
                                                    boolean isActive, boolean isLeftFlipper) {
        double flipperEndX, flipperEndY;

        if (isLeftFlipper) {
            flipperEndX = pivotX + Math.cos(angle) * FLIPPER_LENGTH;
            flipperEndY = pivotY + Math.sin(angle) * FLIPPER_LENGTH;
        } else {
            flipperEndX = pivotX + Math.cos(angle + Math.PI) * FLIPPER_LENGTH;
            flipperEndY = pivotY + Math.sin(angle + Math.PI) * FLIPPER_LENGTH;
        }

        double distanceToLine = pointToLineDistance(m_ballPositionX, m_ballPositionY,
                pivotX, pivotY, flipperEndX, flipperEndY);

        if (distanceToLine < BALL_RADIUS + FLIPPER_WIDTH / 2) {
            if (isLeftFlipper) {
                m_ballVelocityX = Math.abs(m_ballVelocityX) * BOUNCE_DAMPING + 2.0;
            } else {
                m_ballVelocityX = -Math.abs(m_ballVelocityX) * BOUNCE_DAMPING - 2.0;
            }

            m_ballVelocityY = -Math.abs(m_ballVelocityY) * BOUNCE_DAMPING - 3.0;

            if (isActive) {
                if (isLeftFlipper) {
                    m_ballVelocityX += 3.0;
                } else {
                    m_ballVelocityX -= 3.0;
                }
                m_ballVelocityY -= 4.0;
            }

            m_score += 10;
            Logger.debug((isLeftFlipper ? "Левый" : "Правый") + " флиппер: отскок!");
        }
    }


    private synchronized void checkTargetsCollisions() {
        for (Target target : m_targets) {
            if (target.checkCollisionTarget(m_ballPositionX, m_ballPositionY, BALL_RADIUS)) {
                int points = target.hit();
                m_score += points;

                double[] bounce = target.getBounceDirection(m_ballPositionX, m_ballPositionY);
                m_ballVelocityX += bounce[0];
                m_ballVelocityY += bounce[1];

                Logger.debug("Попадание в мишень! +" + points + " очков");
            }
        }
    }


    private synchronized void checkBumpersCollisions() {
        for (Bumper bumper : m_bumpers) {
            if (bumper.checkCollisionBumper(m_ballPositionX, m_ballPositionY, BALL_RADIUS)) {
                int points = bumper.hit();
                m_score += points;

                double[] bounce = bumper.getBounceDirection(m_ballPositionX, m_ballPositionY);
                m_ballVelocityX += bounce[0];
                m_ballVelocityY += bounce[1];

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

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        if (m_backgroundImage != null) {
            g2d.drawImage(m_backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(new Color(20, 20, 73));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        //границы
        g2d.setColor(Color.WHITE);
        g2d.drawRect(BORDER_MARGIN, BORDER_MARGIN, m_fieldWidth, m_fieldHeight);

        drawPlayingField(g2d);
        drawBall(g2d);
        drawFlippers(g2d);
        drawInfo(g2d);
        drawTargetsAndBumpers(g2d);
    }

    //оттрисовка поля
    private void drawPlayingField(Graphics2D g2d) {
        g2d.setColor(new Color(40, 40, 80));

        Polygon fieldPolygon = new Polygon();
        fieldPolygon.addPoint((int)m_topLeftX, (int)m_topLeftY);
        fieldPolygon.addPoint((int)m_topRightX, (int)m_topRightY);
        fieldPolygon.addPoint((int)m_funnelRightX, (int)m_funnelRightY);
        fieldPolygon.addPoint((int)m_funnelRightX, (int)m_funnelRightY + (int)FLIPPER_LENGTH);
        fieldPolygon.addPoint((int)m_funnelLeftX, (int)m_funnelLeftY + (int)FLIPPER_LENGTH);
        fieldPolygon.addPoint((int)m_funnelLeftX, (int)m_funnelLeftY);

        g2d.fillPolygon(fieldPolygon);

        g2d.setColor(Color.WHITE);
        g2d.drawLine((int)m_topLeftX, (int)m_topLeftY, (int)m_topRightX, (int)m_topRightY);
        g2d.drawLine((int)m_topLeftX, (int)m_topLeftY, (int)m_funnelLeftX, (int)m_funnelLeftY);
        g2d.drawLine((int)m_topRightX, (int)m_topRightY, (int)m_funnelRightX, (int)m_funnelRightY);
        g2d.drawLine((int)m_funnelLeftX, (int)m_funnelLeftY + (int)FLIPPER_LENGTH,
                (int)m_funnelRightX, (int)m_funnelRightY + (int)FLIPPER_LENGTH);

        g2d.setColor(new Color(255, 0, 0, 50));
        g2d.fillRect((int)m_funnelLeftX, (int)m_funnelLeftY + (int)FLIPPER_LENGTH,
                (int)(m_funnelRightX - m_funnelLeftX),
                getHeight() - (int)m_funnelLeftY + (int)FLIPPER_LENGTH);
    }

    //отрисовка шарика
    private void drawBall(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(
                (int)(m_ballPositionX - BALL_RADIUS),
                (int)(m_ballPositionY - BALL_RADIUS),
                (int)(BALL_RADIUS * 2),
                (int)(BALL_RADIUS * 2)
        );

        g2d.setColor(Color.ORANGE);
        g2d.drawOval(
                (int)(m_ballPositionX - BALL_RADIUS),
                (int)(m_ballPositionY - BALL_RADIUS),
                (int)(BALL_RADIUS * 2),
                (int)(BALL_RADIUS * 2)
        );
    }

    //флиперов
    private void drawFlippers(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();

        drawFlipper(g2d, m_leftFlipperPivotX, m_leftFlipperPivotY,
                m_leftFlipperAngle, m_leftFlipperActive, true);

        drawFlipper(g2d, m_rightFlipperPivotX, m_rightFlipperPivotY,
                m_rightFlipperAngle, m_rightFlipperActive, false);

        g2d.setTransform(oldTransform);
    }


    private void drawFlipper(Graphics2D g2d, double pivotX, double pivotY,
                             double angle, boolean isActive, boolean isLeftFlipper) {

        double endX, endY;

        if (isLeftFlipper) {
            endX = pivotX + Math.cos(angle) * FLIPPER_LENGTH;
            endY = pivotY + Math.sin(angle) * FLIPPER_LENGTH;
        } else {
            endX = pivotX + Math.cos(angle + Math.PI) * FLIPPER_LENGTH;
            endY = pivotY + Math.sin(angle + Math.PI) * FLIPPER_LENGTH;
        }

        g2d.setColor(isActive ? Color.CYAN : Color.GRAY);
        g2d.setStroke(new java.awt.BasicStroke((float)FLIPPER_WIDTH,
                java.awt.BasicStroke.CAP_ROUND,
                java.awt.BasicStroke.JOIN_ROUND));
        g2d.drawLine((int)pivotX, (int)pivotY, (int)endX, (int)endY);

        g2d.setStroke(new java.awt.BasicStroke(1.0f));

        g2d.setColor(Color.RED);
        g2d.fillOval((int)pivotX - 3, (int)pivotY - 3, 6, 6);
    }


    //информации
    private void drawInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.drawString("Управление: ←/A - левый флиппер, →/D - правый флиппер", 10, 20);
        g2d.drawString("Пробел - запуск шарика, R - сброс, F - полноэкранный режим", 10, 40);
        g2d.drawString(String.format("Скорость: X=%.1f Y=%.1f", m_ballVelocityX, m_ballVelocityY), 10, 60);
        g2d.drawString(String.format("Счет: %d", m_score), 10, 80);
        g2d.drawString(String.format("Жизни: %d", m_lives), 10, 100);

        if (m_ballLost) {
            if (m_lives > 0) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("Шарик потерян! Нажмите ПРОБЕЛ для продолжения",
                        getWidth() / 2 - 150, getHeight() / 2);
            } else {
                g2d.setColor(Color.RED);
                g2d.drawString("ИГРА ОКОНЧЕНА! Финальный счет: " + m_score,
                        getWidth() / 2 - 100, getHeight() / 2);
            }
        }
    }


    private void drawTargetsAndBumpers(Graphics2D g2d) {
        for (Target target : m_targets) {
            target.draw(g2d);
        }

        for (Bumper bumper : m_bumpers) {
            bumper.draw(g2d);
        }
    }


    private void initializeTargets() {
        m_targets.clear();

        m_targets.add(new Target(
                BORDER_MARGIN + m_fieldWidth * 0.2,
                BORDER_MARGIN + m_fieldHeight * 0.1,
                25, 12, Color.RED, 100
        ));


        m_targets.add(new Target(
                BORDER_MARGIN + m_fieldWidth * 0.7,
                BORDER_MARGIN + m_fieldHeight * 0.1,
                25, 12, Color.RED, 100
        ));


        m_targets.add(new Target(
                BORDER_MARGIN + m_fieldWidth * 0.45,
                BORDER_MARGIN + m_fieldHeight * 0.2,
                35, 15, Color.BLUE, 500, 3, 12.0
        ));
    }

    private void initializeBumpers() {
        m_bumpers.clear();

        m_bumpers.add(new Bumper(
                BORDER_MARGIN + m_fieldWidth * 0.3,
                BORDER_MARGIN + m_fieldHeight * 0.4,
                15, Color.GREEN, 50, 8.0
        ));

        m_bumpers.add(new Bumper(
                BORDER_MARGIN + m_fieldWidth * 0.7,
                BORDER_MARGIN + m_fieldHeight * 0.4,
                15, Color.GREEN, 50, 8.0
        ));

        m_bumpers.add(new Bumper(
                BORDER_MARGIN + m_fieldWidth * 0.5,
                BORDER_MARGIN + m_fieldHeight * 0.5,
                18, Color.MAGENTA, 100, 12.0
        ));
    }

    private void loadImages() {
        try {
            m_backgroundImage = Toolkit.getDefaultToolkit().getImage("C:\\Users\\afoni\\IdeaProjects" +
                    "\\PinBall1.0\\robots\\images\\backGroundImage.jpg");
            m_funnelImage = Toolkit.getDefaultToolkit().getImage("images/funnel.png");
            m_fieldImage = Toolkit.getDefaultToolkit().getImage("C:\\Users\\afoni\\IdeaProjects" +
                    "\\PinBall1.0\\robots\\images\\fieldImage.jpg");

            if (m_backgroundImage.getWidth(null) == -1) {
                Logger.error("Не удалось загрузить фоновое изображение");
            }
        } catch (Exception e) {
            Logger.error("Ошибка загрузки изображений: " + e.getMessage());
        }
    }
}
