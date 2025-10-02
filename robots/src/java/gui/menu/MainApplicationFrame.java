package gui.menu;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.*;

import gui.*;
import log.Logger;


public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private LogWindow logWindow;
    private GameWindow gameWindow;
    private static final String PROFILE_FILE = "application_profile.dat";

    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset*2, screenSize.height - inset*2);

        setContentPane(desktopPane);
        initializeApplication();
        setupWindowListeners();
    }


    private void initializeApplication() {
        ApplicationProfile savedProfile = loadProfile();
        if (savedProfile != null && savedProfile.isProfileExists()) {
            boolean restore = ConfirmationDialog.showRestoreProfileDialog();
            if (restore) {
                restoreProfile(savedProfile);
            } else {
                createDefaultWindows();
                deleteProfileFile();
            }
        } else {
            createDefaultWindows();
        }

        setJMenuBar(createMenuBar());
    }


    private void setupWindowListeners() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }

    //удаление профиля
    private void deleteProfileFile() {
        File profileFile = new File(PROFILE_FILE);
        if (profileFile.exists() && profileFile.delete()) {
            Logger.debug("Файл профиля удален");
        } else if (profileFile.exists()) {
                Logger.error("Не удалось удалить файл профиля");
        }
    }

    //создание окон по умолчанию
    private void createDefaultWindows() {
        gameWindow = new GameWindow();
        gameWindow.setSize(1180, 620);
        addWindow(gameWindow);

        logWindow = createLogWindow();
        addWindow(logWindow);

        if (logWindow != null && gameWindow != null) {
            try {
                desktopPane.setComponentZOrder(gameWindow, 0);
                desktopPane.setComponentZOrder(logWindow, 1);
            } catch (Exception e) {
                Logger.error("Ошибка при установке порядка окон по умолчанию: " + e.getMessage());
            }
        }
    }


    private void restoreProfile(ApplicationProfile profile) {
        if (profile.getLogWindowState() != null && profile.getLogWindowState().isOpen()) {
            logWindow = createLogWindow();
            addWindow(logWindow);

            if (profile.getLogWindowState() != null) {
                logWindow.setVisible(true);

                WindowState logState = profile.getLogWindowState();

                logWindow.applyWindowState(logState);

                if (logState.isIcon() && !logWindow.isIcon()) {
                    try {
                        logWindow.setIcon(true);
                    } catch (Exception e) {
                        Logger.error("Не удалось свернуть окно: " + e.getMessage());
                    }
                }
            }
        }

        if (profile.getGameWindowState() != null && profile.getGameWindowState().isOpen()) {
            gameWindow = new GameWindow();
            addWindow(gameWindow);

            if (profile.getGameWindowState() != null) {
                gameWindow.setVisible(true);
                gameWindow.applyWindowState(profile.getGameWindowState());
            }
        }

        if (logWindow != null && gameWindow != null) {
            try {
                desktopPane.setComponentZOrder(gameWindow, 0);
                desktopPane.setComponentZOrder(logWindow, 1);
            } catch (Exception e) {
                Logger.error("Ошибка при установке порядка окон: " + e.getMessage());
            }
        }
    }

    //сохранение профиля при выходе
    private void saveProfile() {
        ApplicationProfile profile = new ApplicationProfile();
        profile.setProfileExists(true);

        WindowState logState = getWindowState(logWindow);
        WindowState gameState = getWindowState(gameWindow);

        profile.setLogWindowState(logState);
        profile.setGameWindowState(gameState);

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(PROFILE_FILE)))) {
            oos.writeObject(profile);
            Logger.debug("Профиль приложения сохранен");
        } catch (IOException e) {
            Logger.error("Ошибка при сохранении профиля: " + e.getMessage());
        }
    }


    private WindowState getWindowState(BaseInternalFrame window) {
        if (window != null && !window.isClosed()) {
            return window.getWindowState();
        }
        return new WindowState(false, null, false, false);
    }

    //загрузка профиля
    private ApplicationProfile loadProfile() {
        File profileFile = new File(PROFILE_FILE);
        if (!profileFile.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(Paths.get(PROFILE_FILE)))) {
            ApplicationProfile profile = (ApplicationProfile) ois.readObject();
            profile.setProfileExists(true);
            Logger.debug("Профиль приложения загружен");
            return profile;
        } catch (IOException | ClassNotFoundException e) {
            Logger.error("Ошибка при загрузке профиля: " + e.getMessage());
            return null;
        }
    }

    //закрытие игрового окна через меню
    public void closeGameWindow() {
        if (gameWindow != null && !gameWindow.isClosed() && gameWindow.closeWindow()) {
            gameWindow = null;
        }
    }

    //закрытие окна логов через меню
    public void closeLogWindow() {
        if (logWindow != null && !logWindow.isClosed() && logWindow.closeWindow()) {
            logWindow = null;
        }
    }

    //выход из приложения
    public void exitApplication() {
        boolean confirmed = ConfirmationDialog.showExitConfirmation();
        if (confirmed) {
            saveProfile();
            Logger.debug("Приложение завершает работу");

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


    private void activateWindow(JInternalFrame window) {
        if (window != null) {
            try {
                window.setSelected(true);
                window.toFront();
            } catch (Exception ex) {
                Logger.debug("Ошибка при активации окна: " + ex.getMessage());
            }
        }
    }

    //методы для повторного открытия окон
    public void openGameWindow() {
        if (gameWindow == null || gameWindow.isClosed()) {
            gameWindow = new GameWindow();
            gameWindow.setSize(1180, 620);
            addWindow(gameWindow);
        } else {
            activateWindow(gameWindow);
        }
    }

    public void openLogWindow() {
        if (logWindow == null || logWindow.isClosed()) {
            logWindow = createLogWindow();
            addWindow(logWindow);
        } else {
            activateWindow(logWindow);
        }
    }
}
