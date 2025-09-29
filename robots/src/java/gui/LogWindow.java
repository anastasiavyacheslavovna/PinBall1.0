package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.TextArea;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;
import log.Logger;

public class LogWindow extends JInternalFrame implements LogChangeListener
{
    private final LogWindowSource m_logSource;
    private final TextArea m_logContent;
    private boolean closingFromMenu = false; //флаг на закрытие окна

    public LogWindow(LogWindowSource logSource) 
    {
        super("Протокол работы", true, true, true, true);
        m_logSource = logSource;
        m_logSource.registerListener(this);
        m_logContent = new TextArea("");
        m_logContent.setSize(200, 500);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_logContent, BorderLayout.CENTER);
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
        updateLogContent();
    }


    // обработки закрытия окна
    private void handleWindowClosing() {
        closingFromMenu = true;

        boolean confirmed = ConfirmationDialog.showCloseConfirmation("Протокол работы");
        if (!confirmed) {
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            //отменяем закрытие
            return;
        }

        //закрываем
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        //отписываемся от источника логов
        m_logSource.unregisterListener(this);

        //логируем закрытие окна
        m_logSource.append(log.LogLevel.Debug, "Окно протокола закрыто");
    }

    //программное закрытия с подтверждением
    public boolean closeWithConfirmation() {
        closingFromMenu = true;

        boolean confirmed = ConfirmationDialog.showCloseConfirmation("Протокол работы");
        if (confirmed) {
            m_logSource.unregisterListener(this);
            try {
                setClosed(true);
                return true;
            } catch (PropertyVetoException ex) {
                Logger.error("Ошибка при закрытии окна" + ex.getMessage());
                return false;
            }
        } else {
            closingFromMenu = false;
        }
        return false;
    }


    private void updateLogContent()
    {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : m_logSource.all())
        {
            content.append(entry.getMessage()).append("\n");
        }
        m_logContent.setText(content.toString());
        m_logContent.invalidate();
    }
    
    @Override
    public void onLogChanged()
    {
        EventQueue.invokeLater(this::updateLogContent);
    }
}
