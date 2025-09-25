package gui;

import javax.swing.JOptionPane;

public class ConfirmationDialog {

    //диалог подтверждения закрытия окна
    public static boolean showCloseConfirmation(String windowName) {
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
        int result = JOptionPane.showConfirmDialog(
                null,
                "Вы уверены, что хотите выйти из приложения?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        return result == JOptionPane.YES_OPTION;
    }
}
