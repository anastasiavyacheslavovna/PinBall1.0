package gui;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class GameWindow extends JInternalFrame
{
    private final GameVisualizer m_visualizer;
    private boolean closingFromMenu = false; //флаг для закрытия окна

    public GameWindow() 
    {
        super("Игровое поле", true, true, true, true);
        m_visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);

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

    // обработка закрытия окна
    private void handleWindowClosing() {
        boolean confirmed = ConfirmationDialog.showCloseConfirmation("Игровое поле");
        if (!confirmed) {
            //отменяем закрытие - восстанавливаем окно
            try {
                setClosed(false); //отменяем закрытие
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
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
                ex.printStackTrace();
                return false;
            }
        } else {
            closingFromMenu = false;
        }
        return false;
    }
}


