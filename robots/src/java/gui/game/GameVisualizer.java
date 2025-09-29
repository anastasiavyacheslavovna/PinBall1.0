package gui.game;

import log.Logger;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    private volatile GameState m_gameState = GameState.READY;

    //флиперов
    private volatile double m_leftFlipperAngle = Math.PI / 6;
    private volatile double m_rightFlipperAngle = -Math.PI / 6;
    //точки вращения
    private double m_leftFlipperPivotX;
    private double m_leftFlipperPivotY;
    private double m_rightFlipperPivotX;
    private double m_rightFlipperPivotY;

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

    private double m_topLeftX, m_topLeftY;
    private double m_topRightX, m_topRightY;
    private double m_funnelLeftX, m_funnelLeftY;
    private double m_funnelRightX, m_funnelRightY;

    private final java.util.List<Target> m_targets = new java.util.ArrayList<>();
    private final java.util.List<Bumper> m_bumpers = new java.util.ArrayList<>();

    private Image m_backgroundImage;

    private final gui.game.GamePhysics m_physics;
    private final gui.game.FlipperController m_flipperController;
    private final gui.game.GameRenderer m_renderer;


    public GameVisualizer()
    {
        m_physics = new gui.game.GamePhysics(this);
        m_flipperController = new FlipperController(this);
        m_renderer = new GameRenderer(this);

        setBackground(Color.BLACK);
        updateFieldDimensions();

        loadImages();

        Timer m_timer = initTimer();
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

        //воронка
        double m_funnelWidth = m_flipperController.getFlipperLength() * 3.2;

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
        m_flipperController.handleKeyPress(keyCode, pressed);

        switch (keyCode) {
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
            m_physics.updatePhysics();
            m_physics.checkCollisions();
        }
        m_flipperController.updateFlippers();
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        m_renderer.render(g2d);
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
                35, 15, Color.BLUE, 500, 12.0
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

            if (m_backgroundImage.getWidth(null) == -1) {
                Logger.error("Не удалось загрузить фоновое изображение");
            }
        } catch (Exception e) {
            Logger.error("Ошибка загрузки изображений: " + e.getMessage());
        }
    }

    public double getLeftFlipperAngle() { return m_leftFlipperAngle; }
    public void setLeftFlipperAngle(double angle) { m_leftFlipperAngle = angle; }

    public double getRightFlipperAngle() { return m_rightFlipperAngle; }
    public void setRightFlipperAngle(double angle) { m_rightFlipperAngle = angle; }

    public boolean isLeftFlipperActive() { return m_leftFlipperActive; }
    public void setLeftFlipperActive(boolean active) { m_leftFlipperActive = active; }

    public boolean isRightFlipperActive() { return m_rightFlipperActive; }
    public void setRightFlipperActive(boolean active) { m_rightFlipperActive = active; }

    public double getBallPositionX() { return m_ballPositionX; }
    public void setBallPositionX(double x) { m_ballPositionX = x; }

    public double getBallPositionY() { return m_ballPositionY; }
    public void setBallPositionY(double y) { m_ballPositionY = y; }

    public double getBallVelocityX() { return m_ballVelocityX; }
    public void setBallVelocityX(double vx) { m_ballVelocityX = vx; }

    public double getBallVelocityY() { return m_ballVelocityY; }
    public void setBallVelocityY(double vy) { m_ballVelocityY = vy; }

    public double getBallRadius() { return m_physics.getBallRadius(); }

    public int getBorderMargin() { return BORDER_MARGIN; }

    public double getFunnelLeftX() { return m_funnelLeftX; }
    public double getFunnelLeftY() { return m_funnelLeftY; }
    public double getTopLeftX() { return m_topLeftX; }
    public double getTopLeftY() { return m_topLeftY; }
    public double getTopRightX() { return m_topRightX; }
    public double getTopRightY() { return m_topRightY; }
    public double getFunnelRightX() { return m_funnelRightX; }
    public double getFunnelRightY() { return m_funnelRightY; }

    public double getFlipperLength() { return m_flipperController.getFlipperLength(); }
    public double getFlipperWidth() { return m_flipperController.getFlipperWidth(); }

    public double getLeftFlipperPivotX() { return m_leftFlipperPivotX; }
    public double getLeftFlipperPivotY() { return m_leftFlipperPivotY; }
    public double getRightFlipperPivotX() { return m_rightFlipperPivotX; }
    public double getRightFlipperPivotY() { return m_rightFlipperPivotY; }

    public int getScore() { return m_score; }
    public void setScore(int score) { m_score = score; }

    public int getLives() { return m_lives; }
    public void setLives(int lives) { m_lives = lives; }

    public boolean isBallLost() { return m_ballLost; }
    public void setBallLost(boolean lost) { m_ballLost = lost; }

    public java.util.List<Target> getTargets() { return m_targets; }
    public java.util.List<Bumper> getBumpers() { return m_bumpers; }

    public int getFieldWidth() { return m_fieldWidth; }
    public int getFieldHeight() { return m_fieldHeight; }
    public Image getBackgroundImage() { return m_backgroundImage; }

    public FlipperController getFlipperController() { return m_flipperController; }
}
