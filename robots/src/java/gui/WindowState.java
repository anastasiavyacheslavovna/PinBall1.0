package gui;

import java.awt.*;
import java.io.Serializable;


public class WindowState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean isOpen;
    private final Rectangle bounds;
    private final boolean isIcon;
    private final boolean isMaximum;

    public WindowState(boolean isOpen, Rectangle bounds, boolean isIcon, boolean isMaximum) {
        this.isOpen = isOpen;
        this.bounds = bounds;
        this.isIcon = isIcon;
        this.isMaximum = isMaximum;
    }

    public boolean isOpen() { return isOpen; }
    public Rectangle getBounds() { return bounds; }
    public boolean isIcon() { return isIcon; }
    public boolean isMaximum() { return isMaximum; }
}
