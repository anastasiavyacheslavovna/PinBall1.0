package gui.menu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;

//проверка "выйти"
class ExitMenuTest {

    private MainApplicationFrame frame;
    private ApplicationMenuBar menuBar;

    @BeforeEach
    void setUp() {
        frame = new MainApplicationFrame();
        menuBar = new ApplicationMenuBar(frame);
    }

    @Test
    void testExitMenuItemTriggersAction() {
        javax.swing.JMenu fileMenu = menuBar.getMenu(0);
        JMenuItem exitItem = (JMenuItem) fileMenu.getMenuComponent(0);

        assertTrue(exitItem.getActionListeners().length > 0,
                "Пункт 'Выход' должен иметь обработчик события");

        assertDoesNotThrow(() -> {
            for (java.awt.event.ActionListener listener : exitItem.getActionListeners()) {
                listener.actionPerformed(
                        new ActionEvent(exitItem, ActionEvent.ACTION_PERFORMED, "")
                );
            }
        });
    }
}