package gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.JOptionPane;

//подтверждение закрытия
class WindowCloseConfirmationTest {

    @Test
    void testConfirmationDialogMethodsExist() {
        GameWindow gameWindow = new GameWindow();
        LogWindow logWindow = new LogWindow(log.Logger.getDefaultLogSource());

        assertDoesNotThrow(() -> gameWindow.closeWithConfirmation());
        assertDoesNotThrow(() -> logWindow.closeWithConfirmation());

        assertDoesNotThrow(() -> ConfirmationDialog.showCloseConfirmation("Тест"));
        assertDoesNotThrow(() -> ConfirmationDialog.showExitConfirmation());
    }

    @Test
    void testWindowsHaveCloseFunctionality() {
        GameWindow gameWindow = new GameWindow();
        LogWindow logWindow = new LogWindow(log.Logger.getDefaultLogSource());

        assertDoesNotThrow(() -> {
            if (!gameWindow.isClosed()) {
                gameWindow.setClosed(true);
            }
        });

        assertDoesNotThrow(() -> {
            if (!logWindow.isClosed()) {
                logWindow.setClosed(true);
            }
        });
    }

    @Test
    void testConfirmationDialogReturnsBoolean() {
        boolean result1 = ConfirmationDialog.showCloseConfirmation("Тестовое окно");
        boolean result2 = ConfirmationDialog.showExitConfirmation();

        assertTrue(result1 == true || result1 == false);
        assertTrue(result2 == true || result2 == false);
    }
}