package gui.menu;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import gui.ConfirmationDialog;
import gui.GameWindow;
import gui.LogWindow;
import log.Logger;


public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private LogWindow logWindow;
    private GameWindow gameWindow;

    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width  - inset*2,
                screenSize.height - inset*2);

        setContentPane(desktopPane);


        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow();
        gameWindow.setSize(400,  400);
        addWindow(gameWindow);

        setJMenuBar(createMenuBar());
        //закрытие главного окна
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleApplicationClosing();
            }
        });
    }

    //закрытие приложения
    private void handleApplicationClosing() {
        boolean confirmed = ConfirmationDialog.showExitConfirmation();
        if (confirmed) {
            // закрываем все внутренние окна
            if (logWindow != null && !logWindow.isClosed()) {
                try {
                    logWindow.setClosed(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (gameWindow != null && !gameWindow.isClosed()) {
                try {
                    gameWindow.setClosed(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // завершаем приложение
            dispose();
            System.exit(0);
        }
    }

    //закрытие игрового окна через меню
    public void closeGameWindow() {
        if (gameWindow != null && !gameWindow.isClosed()) {
            boolean closed = gameWindow.closeWithConfirmation();
            if (closed) {
                gameWindow = null;
            }
        }
    }

    //закрытие окна логов через меню
    public void closeLogWindow() {
        if (logWindow != null && !logWindow.isClosed()) {
            boolean closed = logWindow.closeWithConfirmation();
            if (closed) {
                logWindow = null;
            }
        }
    }

    //метод для выхода из приложения
    public void exitApplication() {
        boolean confirmed = ConfirmationDialog.showExitConfirmation();
        if (confirmed) {
            //логируем выход
            Logger.debug("Приложение завершает работу");

            //закрываем приложение
            dispose();
            System.exit(0);
        }
    }

    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
    }


    public JMenuBar createMenuBar() { //упростила создание
        return new ApplicationMenuBar(this);
    }

    public void setLookAndFeel(String className) //now public
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }


    //методы для повторного открытия окон
    public void openGameWindow() {
        if (gameWindow == null || gameWindow.isClosed()) {
            gameWindow = new GameWindow();
            gameWindow.setSize(400, 400);
            addWindow(gameWindow);
        } else {
            //если окно уже открыто, активируем его
            try {
                gameWindow.setSelected(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void openLogWindow() {
        if (logWindow == null || logWindow.isClosed()) {
            logWindow = createLogWindow();
            addWindow(logWindow);
        } else {
            //если окно уже открыто, активируем его
            try {
                logWindow.setSelected(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}