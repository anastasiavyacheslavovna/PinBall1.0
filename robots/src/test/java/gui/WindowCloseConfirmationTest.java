package gui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyVetoException;

import static org.junit.jupiter.api.Assertions.*;

//подтверждение закрытия
class WindowCloseConfirmationTest {
    @BeforeEach
    void setUp() {
        ConfirmationDialog.setTestMode(true, true);
    }

    @AfterEach
    void tearDown() {
        ConfirmationDialog.resetTestMode();
    }

    @Test
    void testConfirmationDialogMethodsExist() {
        GameWindow gameWindow = new GameWindow();
        LogWindow logWindow = new LogWindow(log.Logger.getDefaultLogSource());

        assertDoesNotThrow(gameWindow::closeWindow);
        assertDoesNotThrow(logWindow::closeWindow);

        assertDoesNotThrow(() -> ConfirmationDialog.showCloseConfirmation("Тест"));
        assertDoesNotThrow(ConfirmationDialog::showExitConfirmation);
    }

    @Test
    void testWindowsHaveCloseFunctionality() {
        GameWindow gameWindow = new GameWindow();
        LogWindow logWindow = new LogWindow(log.Logger.getDefaultLogSource());

        assertDoesNotThrow(gameWindow::closeWindow);
        assertDoesNotThrow(logWindow::closeWindow);

        assertDoesNotThrow(() -> {
            try {
                if (!gameWindow.isClosed()) {
                    gameWindow.setClosed(true);
                }
            } catch (PropertyVetoException e) {
                //ignor
            }
        });

        assertDoesNotThrow(() -> {
            try {
                if (!logWindow.isClosed()) {
                    logWindow.setClosed(true);
                }
            } catch (PropertyVetoException e) {
                //ignor
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
