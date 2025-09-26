package gui;

import java.awt.*;


public class Target {
    private final double m_x;
    private final double m_y;
    private final double m_width;
    private final double m_height;

    private Color m_color;
    private final Color m_hitColor;

    private final int m_pointValue;
    private long m_hitAnimationDuration = 300;

    private double m_bounceStrength;


    public Target(double x, double y, double width, double height, Color baseColor, int pointValue) {
        this.m_x = x;
        this.m_y = y;
        this.m_width = width;
        this.m_height = height;
        this.m_pointValue = pointValue;

        this.m_hitColor = Color.YELLOW;
        this.m_color = baseColor;

        this.m_bounceStrength = 8.0;
    }

    /**
     * Конструктор с дополнительными параметрами
     */
    public Target(double x, double y, double width, double height,
                  Color baseColor, int pointValue, int maxHits, double bounceStrength) {
        this(x, y, width, height, baseColor, pointValue);
        this.m_bounceStrength = bounceStrength;
    }


    public double getX() {
        return m_x;
    }

    public double getY() {
        return m_y;
    }

    public double getWidth() {
        return m_width;
    }

    public double getHeight() {
        return m_height;
    }

    public double getCenterX() {
        return m_x + m_width / 2;
    }

    public double getCenterY() {
        return m_y + m_height / 2;
    }

    public int getPointValue() {
        return m_pointValue;
    }


    public void setBounceStrength(double strength) {
        this.m_bounceStrength = strength;
    }


    public boolean checkCollisionTarget(double ballX, double ballY, double ballRadius) {
        double closestX = Math.max(m_x, Math.min(ballX, m_x + m_width));
        double closestY = Math.max(m_y, Math.min(ballY, m_y + m_height));

        double distanceX = ballX - closestX;
        double distanceY = ballY - closestY;
        double distanceSquared = distanceX * distanceX + distanceY * distanceY;

        return distanceSquared < (ballRadius * ballRadius);
    }


    public int hit() {
        startHitAnimation();

        return m_pointValue;
    }


    private void startHitAnimation() {
        m_color = m_hitColor;
    }


    public void update() {
        if (m_color == m_hitColor) {
            long currentTime = System.currentTimeMillis();
        }
    }


    public void draw(Graphics2D g2d) {
        g2d.setColor(m_color);

        g2d.fillRect((int) m_x, (int) m_y, (int) m_width, (int) m_height);

        g2d.setColor(Color.WHITE);
        g2d.drawRect((int) m_x, (int) m_y, (int) m_width, (int) m_height);


        g2d.setColor(Color.BLACK);
        g2d.fillOval((int) (m_x + m_width / 2 - 2), (int) (m_y + m_height / 2 - 2), 4, 4);

        if (m_width > 30 && m_height > 15) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String pointsText = "+" + m_pointValue;
            int textWidth = g2d.getFontMetrics().stringWidth(pointsText);
            g2d.drawString(pointsText, (int) (m_x + m_width / 2 - (double) textWidth / 2), (int) (m_y + m_height / 2 + 4));
        }
    }


    public double[] getBounceDirection(double ballX, double ballY) {
        double centerX = getCenterX();
        double centerY = getCenterY();

        double dx = ballX - centerX;
        double dy = ballY - centerY;

        double length = Math.sqrt(dx * dx + dy * dy);
        if (length > 0) {
            dx /= length;
            dy /= length;
        }

        return new double[]{dx * m_bounceStrength, dy * m_bounceStrength};
    }


    @Override
    public String toString() {
        return String.format("Target[%.1f,%.1f %dx%d points=%d hits=%d/%d active=%s]",
                m_x, m_y, m_width, m_height, m_pointValue);
    }

    public long getM_hitAnimationDuration() {
        return m_hitAnimationDuration;
    }

    public void setM_hitAnimationDuration(long m_hitAnimationDuration) {
        this.m_hitAnimationDuration = m_hitAnimationDuration;
    }
}
