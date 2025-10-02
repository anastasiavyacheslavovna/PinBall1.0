package gui.menu;

import gui.ConfirmationDialog;
import log.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;

//проверка "выйти"
class ExitMenuTest {

    private MainApplicationFrame frame;
    private ApplicationMenuBar menuBar;

    @BeforeEach
    void setUp() {
        ConfirmationDialog.setTestMode(true, true);
        frame = new MainApplicationFrame();
        menuBar = new ApplicationMenuBar(frame);
    }

    @AfterEach
    void tearDown() {
        ConfirmationDialog.resetTestMode();
    }


    @Test
    void testExitMenuItemTriggersAction() {
        javax.swing.JMenu fileMenu = menuBar.getMenu(0);
        JMenuItem exitItem = (JMenuItem) fileMenu.getMenuComponent(0);

        assertTrue(exitItem.getActionListeners().length > 0);

        assertDoesNotThrow(() -> {
            for (java.awt.event.ActionListener listener : exitItem.getActionListeners()) {
                listener.actionPerformed(
                        new ActionEvent(exitItem, ActionEvent.ACTION_PERFORMED, "")
                );
            }
        });
    }
}