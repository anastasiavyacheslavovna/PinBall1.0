package gui;

import log.Logger;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class GameVisualizer extends JPanel
{
    private final Timer m_timer = initTimer();
    
    private static Timer initTimer() 
    {
        Timer timer = new Timer("events generator", true);
        return timer;
    }
    
    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100; 
    private volatile double m_robotDirection = 0;

    // добавила константы для границ поля
    private static final int BORDER_MARGIN = 20; // отступ
    private static final double ROBOT_RADIUS = 15; // радиус робота для столкновений
    private int m_fieldWidth = 500; // ширина
    private int m_fieldHeight = 400; // высота

    private volatile int m_targetPositionX = 150;
    private volatile int m_targetPositionY = 100;
    
    private static final double maxVelocity = 0.1; 
    private static final double maxAngularVelocity = 0.001; 
    
    public GameVisualizer() 
    {
        updateFieldDimensions();

        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onRedrawEvent();
            }
        }, 0, 50);
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onModelUpdateEvent();
            }
        }, 0, 10);
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                setTargetPosition(e.getPoint());
                repaint();
            }
        });
        setDoubleBuffered(true);
    }

    // обновление размеров поля
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        updateFieldDimensions();
    }

    // обновление размеров игрового поля
    private void updateFieldDimensions() {
        m_fieldWidth = getWidth() - BORDER_MARGIN * 2;
        m_fieldHeight = getHeight() - BORDER_MARGIN * 2;

        // изменяем позицию робота, если он вне границ после изменения размера
        correctRobotPosition();
        // изменяем позицию цели
        correctTargetPosition();
    }

    // изменение позиции робота
    private void correctRobotPosition() {
        double minX = BORDER_MARGIN + ROBOT_RADIUS;
        double maxX = BORDER_MARGIN + m_fieldWidth - ROBOT_RADIUS;
        double minY = BORDER_MARGIN + ROBOT_RADIUS;
        double maxY = BORDER_MARGIN + m_fieldHeight - ROBOT_RADIUS;

        m_robotPositionX = Math.max(minX, Math.min(maxX, m_robotPositionX));
        m_robotPositionY = Math.max(minY, Math.min(maxY, m_robotPositionY));
    }

    // изменение позиции цели
    private void correctTargetPosition() {
        double minX = BORDER_MARGIN;
        double maxX = BORDER_MARGIN + m_fieldWidth;
        double minY = BORDER_MARGIN;
        double maxY = BORDER_MARGIN + m_fieldHeight;

        m_targetPositionX = (int) Math.max(minX, Math.min(maxX, m_targetPositionX));
        m_targetPositionY = (int) Math.max(minY, Math.min(maxY, m_targetPositionY));
    }

    protected void setTargetPosition(Point p)
    {
        //ограничние на цель
        int x = (int) Math.max(BORDER_MARGIN, Math.min(BORDER_MARGIN + m_fieldWidth, p.x));
        int y = (int) Math.max(BORDER_MARGIN, Math.min(BORDER_MARGIN + m_fieldHeight, p.y));

        m_targetPositionX = p.x;
        m_targetPositionY = p.y;

        //лог на новую цель
        Logger.debug("Установлена новая цель: X=" + x + ", Y=" + y);
    }
    
    protected void onRedrawEvent()
    {
        EventQueue.invokeLater(this::repaint);
    }

    private static double distance(double x1, double y1, double x2, double y2)
    {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }
    
    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        double diffX = toX - fromX;
        double diffY = toY - fromY;
        
        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }
    
    protected void onModelUpdateEvent()
    {
        double distance = distance(m_targetPositionX, m_targetPositionY, 
            m_robotPositionX, m_robotPositionY);
        if (distance < 0.5)
        {
            return;
        }
        double velocity = maxVelocity;
        double angleToTarget = angleTo(m_robotPositionX, m_robotPositionY, m_targetPositionX, m_targetPositionY);
        double angularVelocity = 0;
        if (angleToTarget > m_robotDirection)
        {
            angularVelocity = maxAngularVelocity;
        }
        if (angleToTarget < m_robotDirection)
        {
            angularVelocity = -maxAngularVelocity;
        }
        
        moveRobot(velocity, angularVelocity, 10);

        checkBoundaryCollision(); //проверка столкновения
    }


    private void checkBoundaryCollision() {
        if (m_fieldWidth <= 0 || m_fieldHeight <= 0) {
            return; // размеры еще не знаем
        }

        boolean collision = false;
        double bounceIntensity = 0.3; // сила отскока

        // проверка левой границы
        if (m_robotPositionX - ROBOT_RADIUS < BORDER_MARGIN) {
            m_robotPositionX = BORDER_MARGIN + ROBOT_RADIUS;
            m_robotDirection = asNormalizedRadians(Math.PI - m_robotDirection);
            collision = true;
        }

        // проверка правой границы
        if (m_robotPositionX + ROBOT_RADIUS > BORDER_MARGIN + m_fieldWidth) {
            m_robotPositionX = BORDER_MARGIN + m_fieldWidth - ROBOT_RADIUS;
            m_robotDirection = asNormalizedRadians(Math.PI - m_robotDirection);
            collision = true;
        }

        // проверка верхней границы
        if (m_robotPositionY - ROBOT_RADIUS < BORDER_MARGIN) {
            m_robotPositionY = BORDER_MARGIN + ROBOT_RADIUS;
            m_robotDirection = asNormalizedRadians(-m_robotDirection);
            collision = true;
        }

        // проверка нижней границы
        if (m_robotPositionY + ROBOT_RADIUS > BORDER_MARGIN + m_fieldHeight) {
            m_robotPositionY = BORDER_MARGIN + m_fieldHeight - ROBOT_RADIUS;
            m_robotDirection = asNormalizedRadians(-m_robotDirection);
            collision = true;
        }

        // добавляем случайное отклонение
        if (collision) {
            double randomAngle = (Math.random() - 0.5) * 0.2;
            m_robotDirection = asNormalizedRadians(m_robotDirection + randomAngle);

            // логируем столкновение
            Logger.debug("Робот столкнулся с границей. Новое направление: " +
                    String.format("%.2f", m_robotDirection));
        }
    }

    
    private static double applyLimits(double value, double min, double max)
    {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }
    
    private void moveRobot(double velocity, double angularVelocity, double duration)
    {
        velocity = applyLimits(velocity, 0, maxVelocity);
        angularVelocity = applyLimits(angularVelocity, -maxAngularVelocity, maxAngularVelocity);
        double newX = m_robotPositionX + velocity / angularVelocity * 
            (Math.sin(m_robotDirection  + angularVelocity * duration) -
                Math.sin(m_robotDirection));
        if (!Double.isFinite(newX))
        {
            newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);
        }
        double newY = m_robotPositionY - velocity / angularVelocity * 
            (Math.cos(m_robotDirection  + angularVelocity * duration) -
                Math.cos(m_robotDirection));
        if (!Double.isFinite(newY))
        {
            newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        }
        m_robotPositionX = newX;
        m_robotPositionY = newY;
        double newDirection = asNormalizedRadians(m_robotDirection + angularVelocity * duration); 
        m_robotDirection = newDirection;
    }

    private static double asNormalizedRadians(double angle)
    {
        while (angle < 0)
        {
            angle += 2*Math.PI;
        }
        while (angle >= 2*Math.PI)
        {
            angle -= 2*Math.PI;
        }
        return angle;
    }
    
    private static int round(double value)
    {
        return (int)(value + 0.5);
    }
    
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        drawFieldBounds(g2d); //отрисовка границ
        drawRobot(g2d, round(m_robotPositionX), round(m_robotPositionY), m_robotDirection);
        drawTarget(g2d, m_targetPositionX, m_targetPositionY);
    }

    //отрисовка границ
    private void drawFieldBounds(Graphics2D g2d) {
        g2d.setColor(new Color(255, 0, 0, 50));
        g2d.drawRect(BORDER_MARGIN, BORDER_MARGIN, m_fieldWidth, m_fieldHeight);

        g2d.setColor(Color.RED);
        g2d.drawString("Границы поля", BORDER_MARGIN + 5, BORDER_MARGIN + 15);
    }
    
    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }
    
    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }
    
    private void drawRobot(Graphics2D g, int x, int y, double direction)
    {
        int robotCenterX = round(m_robotPositionX); 
        int robotCenterY = round(m_robotPositionY);
        AffineTransform t = AffineTransform.getRotateInstance(direction, robotCenterX, robotCenterY); 
        g.setTransform(t);
        g.setColor(Color.MAGENTA);
        fillOval(g, robotCenterX, robotCenterY, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX, robotCenterY, 30, 10);
        g.setColor(Color.WHITE);
        fillOval(g, robotCenterX  + 10, robotCenterY, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX  + 10, robotCenterY, 5, 5);

        g.setTransform(new AffineTransform()); //сбросили
    }
    
    private void drawTarget(Graphics2D g, int x, int y)
    {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0); 
        g.setTransform(t);
        g.setColor(Color.GREEN);
        fillOval(g, x, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 5, 5);
    }
}
