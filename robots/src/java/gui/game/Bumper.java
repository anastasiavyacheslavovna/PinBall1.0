package gui.game;

import java.awt.Color;
import java.awt.Graphics2D;


public class Bumper {
    private final double m_x;
    private final double m_y;
    private final double m_radius;

    private final Color m_hitColor;
    private Color m_currentColor;

    private final int m_pointValue;
    private final double m_bounceStrength;


    public Bumper(double x, double y, double radius, Color color, int pointValue, double bounceStrength) {
        this.m_x = x;
        this.m_y = y;
        this.m_radius = radius;
        this.m_hitColor = Color.WHITE;
        this.m_currentColor = color;
        this.m_pointValue = pointValue;
        this.m_bounceStrength = bounceStrength;
    }

    public boolean checkCollisionBumper(double ballX, double ballY, double ballRadius) {
        double dx = ballX - m_x;
        double dy = ballY - m_y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < ballRadius + m_radius;
    }


    public int hit() {
        m_currentColor = m_hitColor;
        return m_pointValue;
    }


    public double[] getBounceDirection(double ballX, double ballY) {
        double dx = ballX - m_x;
        double dy = ballY - m_y;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            dx /= length;
            dy /= length;
        }

        return new double[]{dx * m_bounceStrength, dy * m_bounceStrength};
    }


    public void draw(Graphics2D g2d) {
        g2d.setColor(m_currentColor);
        g2d.fillOval((int)(m_x - m_radius), (int)(m_y - m_radius),
                (int)(m_radius * 2), (int)(m_radius * 2));


        g2d.setColor(Color.WHITE);
        g2d.drawOval((int)(m_x - m_radius), (int)(m_y - m_radius),
                (int)(m_radius * 2), (int)(m_radius * 2));


        g2d.setColor(Color.BLACK);
        g2d.fillOval((int)(m_x - 2), (int)(m_y - 2), 4, 4);
    }
}