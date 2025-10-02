package gui;

import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyVetoException;
import log.Logger;

public abstract class BaseInternalFrame extends JInternalFrame {
    private boolean isHandlingClose = false;
    protected final String windowName;
    private Rectangle normalBounds;

    public BaseInternalFrame(String title, String windowName, boolean resizable,
                             boolean closable, boolean maximizable, boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);
        this.windowName = windowName;
        this.normalBounds = new Rectangle(100, 100, 400, 300);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setupWindowClosingHandler();
        setupStateTracking();
    }

    private void setupStateTracking() {
        // Убрал дублированный код - оставил только один PropertyChangeListener
        addPropertyChangeListener(evt -> {
            if ("maximum".equals(evt.getPropertyName())) {
                boolean wasMaximized = (Boolean) evt.getOldValue();
                boolean isMaximized = (Boolean) evt.getNewValue();

                if (wasMaximized && !isMaximized) {
                    if (normalBounds != null) {
                        setBounds(normalBounds);
                    }
                } else if (!wasMaximized && isMaximized) {
                    updateNormalBounds();
                }
            }
        });

        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
                updateNormalBounds();
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                updateNormalBounds();
            }

            @Override
            public void internalFrameIconified(InternalFrameEvent e) {
                updateNormalBounds();
            }

            @Override
            public void internalFrameDeiconified(InternalFrameEvent e) {
                updateNormalBounds();
            }

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                updateNormalBounds();
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateNormalBounds();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                updateNormalBounds();
            }
        });
    }

    private void updateNormalBounds() {
        if (!isMaximum() && !isIcon()) {
            Rectangle currentBounds = getBounds();
            if (currentBounds.width > 50 && currentBounds.height > 50) {
                normalBounds = new Rectangle(currentBounds);
            }
        }
    }

    private void setupWindowClosingHandler() {
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (!isHandlingClose) {
                    SwingUtilities.invokeLater(() -> handleCloseRequest());
                }
            }
        });
    }

    private boolean handleCloseRequest() {
        if (isHandlingClose) return false;

        isHandlingClose = true;
        try {
            boolean confirmed = ConfirmationDialog.showCloseConfirmation(windowName);
            if (confirmed) {
                performClose();
                return true;
            }
            return false;
        } finally {
            isHandlingClose = false;
        }
    }

    public boolean closeWindow() {
        return handleCloseRequest();
    }

    private void performClose() {
        try {
            onConfirmedClose();
            setClosed(true);
        } catch (PropertyVetoException ex) {
            Logger.error("Ошибка при закрытии " + windowName + ": " + ex.getMessage());
        }
    }

    public WindowState getWindowState() {
        boolean isIcon = isIcon();
        boolean isMaximum = isMaximum();

        if (isIcon) {
            isMaximum = false;
        }

        Rectangle boundsToSave;
        if (isMaximum || isIcon) {
            boundsToSave = (normalBounds != null) ? new Rectangle(normalBounds) : new Rectangle(100, 100, 500, 400);
        } else {
            boundsToSave = new Rectangle(getBounds());
        }

        if (!isMaximizableSupported()) {
            isIcon = false;
            isMaximum = false;
        }

        return new WindowState(true, boundsToSave, isIcon, isMaximum);
    }

    public void applyWindowState(WindowState state) {
        if (state == null) return;

        if (state.getBounds() != null && state.getBounds().width > 0 && state.getBounds().height > 0) {
            normalBounds = new Rectangle(state.getBounds());
        }

        try {
            if (isIcon()) setIcon(false);
            if (isMaximum()) setMaximum(false);
        } catch (Exception e) {
            // игнор
        }

        if (normalBounds != null) {
            Rectangle adjustedBounds = adjustBoundsToScreen(normalBounds);
            setBounds(adjustedBounds);
        }

        setVisible(true);

        try {
            if (isMaximizableSupported()) {
                if (state.isMaximum() && !state.isIcon()) {
                    setMaximum(true);
                }
                else if (state.isIcon()) {
                    setIcon(true);
                }
            }
        } catch (Exception e) {
            Logger.error("Ошибка при восстановлении состояния " + windowName + ": " + e.getMessage());
        }
    }

    protected Rectangle adjustBoundsToScreen(Rectangle bounds) {
        if (bounds == null || bounds.width <= 0 || bounds.height <= 0) {
            return new Rectangle(100, 100, 500, 400);
        }

        Rectangle adjusted = new Rectangle(bounds);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (adjusted.x < 0) adjusted.x = 10;
        if (adjusted.y < 0) adjusted.y = 10;
        if (adjusted.width > screenSize.width - 20)
            adjusted.width = screenSize.width - 20;
        if (adjusted.height > screenSize.height - 20)
            adjusted.height = screenSize.height - 20;
        if (adjusted.x + adjusted.width > screenSize.width)
            adjusted.x = screenSize.width - adjusted.width - 10;
        if (adjusted.y + adjusted.height > screenSize.height)
            adjusted.y = screenSize.height - adjusted.height - 10;

        if (adjusted.width < 300) adjusted.width = 300;
        if (adjusted.height < 200) adjusted.height = 200;

        return adjusted;
    }

    protected boolean isMaximizableSupported() {
        return isMaximizable() && isIconifiable();
    }

    protected abstract void onConfirmedClose();
}
