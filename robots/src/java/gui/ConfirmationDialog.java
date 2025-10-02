package gui;

import javax.swing.JOptionPane;

public class ConfirmationDialog {
    private static boolean testMode = false;
    private static Boolean testResponse = null;


    public static void setTestMode(boolean testMode, Boolean predefinedResponse) {
        ConfirmationDialog.testMode = testMode;
        ConfirmationDialog.testResponse = predefinedResponse;
    }


    public static void resetTestMode() {
        testMode = false;
        testResponse = null;
    }

    //диалог подтверждения закрытия окна
    public static boolean showCloseConfirmation(String windowName) {
        if (testMode && testResponse != null) {
            return testResponse;
        }

        int result = JOptionPane.showConfirmDialog(
                null,
                "Вы уверены, что хотите закрыть окно \"" + windowName + "\"?",
                "Подтверждение закрытия",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        return result == JOptionPane.YES_OPTION;
    }

    //диалог подтверждения закрытия приложения
    public static boolean showExitConfirmation() {
        if (testMode && testResponse != null) {
            return testResponse;
        }

        int result = JOptionPane.showConfirmDialog(
                null,
                "Вы уверены, что хотите выйти из приложения?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        return result == JOptionPane.YES_OPTION;
    }

    public static boolean showRestoreProfileDialog() {
        if (testMode && testResponse != null) {
            return testResponse;
        }

        int result = JOptionPane.showConfirmDialog(
                null,
                "Обнаружен сохраненный профиль приложения. Восстановить предыдущее состояние окон?",
                "Восстановление профиля",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
}
