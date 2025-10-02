package gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import gui.game.GameVisualizer;
import log.Logger;

public class GameWindow extends BaseInternalFrame {
    public GameWindow() {
        super("Игровое поле", "Игровое поле",
                false, true, false, false);

        GameVisualizer visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);

        pack();
    }

    @Override
    protected void onConfirmedClose() {
        Logger.debug("Игровое окно закрыто");
    }

    @Override
    protected boolean isMaximizableSupported() {
        return false;
    }
}
