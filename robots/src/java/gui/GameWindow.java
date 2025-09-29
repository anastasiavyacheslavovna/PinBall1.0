package gui;

import gui.game.GameVisualizer;
import log.Logger;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class GameWindow extends JInternalFrame
{
    private boolean closingFromMenu = false; //флаг для закрытия окна

    public GameWindow() 
    {
        super("Игровое поле", true, true, true, true);
        GameVisualizer m_visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        setResizable(false);
        setMaximizable(false);
        setIconifiable(false);

        //обработка закрытия окна
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (!closingFromMenu) {
                    handleWindowClosing();
                }
            }
        });

        pack();
    }

    //обработка закрытия окна
    private void handleWindowClosing() {
        boolean confirmed = ConfirmationDialog.showCloseConfirmation("Игровое поле");
        if (!confirmed) {
            //отменяем закрытие - восстанавливаем окно
            try {
                setClosed(false); //отменяем закрытие
            } catch (PropertyVetoException ex) {
                Logger.error("Ошибка при отмене закрытия" + ex.getMessage());
            }
            return;
        }

        //окно закроется автоматически
        System.out.println("Игровое окно закрыто");
    }

    //программное закрытие с подтверждением
    public boolean closeWithConfirmation() {
        closingFromMenu = true;

        boolean confirmed = ConfirmationDialog.showCloseConfirmation("Игровое поле");
        if (confirmed) {
            try {
                setClosed(true); //закрываем окно
                return true;
            } catch (PropertyVetoException ex) {
                Logger.error("Ошибка при закрытие" + ex.getMessage());
                return false;
            }
        } else {
            closingFromMenu = false;
        }
        return false;
    }
}


