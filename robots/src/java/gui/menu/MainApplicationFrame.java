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
        setBounds(inset, inset,
                screenSize.width  - inset*2,
                screenSize.height - inset*2);

        setContentPane(desktopPane);

        ApplicationProfile savedProfile = loadProfile();
        if (savedProfile != null && savedProfile.isProfileExists()) {
            boolean restore = showRestoreProfileDialog();
            if (restore) {
                restoreProfile(savedProfile);
            } else {
                //создаем окна по умолчанию
                createDefaultWindows();
                //удаляем файл профиля
                deleteProfileFile();
            }
        } else {
            //если профиля нет, создаем окна по умолчанию
            createDefaultWindows();
        }


        setJMenuBar(createMenuBar());
        //закрытие главного окна
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
        if (profileFile.exists()) {
            if (profileFile.delete()) {
                Logger.debug("Файл профиля удален");
            } else {
                Logger.error("Не удалось удалить файл профиля");
            }
        }
    }

    //создание окон по умолчанию
    private void createDefaultWindows() {
        logWindow = createLogWindow();
        addWindow(logWindow);

        gameWindow = new GameWindow();
        gameWindow.setSize(1180, 620);
        addWindow(gameWindow);
    }

    private boolean showRestoreProfileDialog() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Обнаружен сохраненный профиль приложения. Восстановить предыдущее состояние окон?",
                "Восстановление профиля",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    // Восстановление профиля
    private void restoreProfile(ApplicationProfile profile) {
        // Восстанавливаем окно логов
        if (profile.getLogWindowState() != null && profile.getLogWindowState().isOpen()) {
            logWindow = createLogWindow();
            applyWindowState(logWindow, profile.getLogWindowState());
            addWindow(logWindow);
        }

        // Восстанавливаем игровое окно
        if (profile.getGameWindowState() != null && profile.getGameWindowState().isOpen()) {
            gameWindow = new GameWindow();
            applyWindowState(gameWindow, profile.getGameWindowState());
            addWindow(gameWindow);
        }
    }

    //применение состояния к окну
    private void applyWindowState(JInternalFrame window, WindowState state) {
        if (state.getBounds() != null) {
            //проверяем, чтобы окно не выходило за пределы экрана
            Rectangle bounds = state.getBounds();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            //корректируем положение, если окно выходит за пределы экрана
            if (bounds.x < 0) bounds.x = 10;
            if (bounds.y < 0) bounds.y = 10;
            if (bounds.x + bounds.width > screenSize.width)
                bounds.width = screenSize.width - bounds.x - 50;
            if (bounds.y + bounds.height > screenSize.height)
                bounds.height = screenSize.height - bounds.y - 50;

            window.setBounds(bounds);
        }

        try {
            //восстанавливаем свернутое/развернутое состояние
            if (window instanceof LogWindow) {
                if (state.isIcon()) {
                    window.setIcon(true);
                }
                if (state.isMaximum()) {
                    window.setMaximum(true);
                }
            }
        } catch (Exception e) {
            Logger.error("Ошибка при восстановлении состояния окна: " + e.getMessage());
        }
    }

    //сохранение профиля при выходе
    private void saveProfile() {
        ApplicationProfile profile = new ApplicationProfile();
        profile.setProfileExists(true);

        //сохраняем состояние окна логов
        if (logWindow != null && !logWindow.isClosed()) {
            WindowState logState = captureWindowState(logWindow);
            profile.setLogWindowState(logState);
        } else {
            profile.setLogWindowState(new WindowState(false, null, false, false));
        }

        //сохраняем состояние игрового окна
        if (gameWindow != null && !gameWindow.isClosed()) {
            WindowState gameState = captureWindowState(gameWindow);
            profile.setGameWindowState(gameState);
        } else {
            profile.setGameWindowState(new WindowState(false, null, false, false));
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(PROFILE_FILE)))) {
            oos.writeObject(profile);
            Logger.debug("Профиль приложения сохранен");
        } catch (IOException e) {
            Logger.error("Ошибка при сохранении профиля: " + e.getMessage());
        }
    }

    //захват состояния окна
    private WindowState captureWindowState(JInternalFrame window) {
        Rectangle bounds = window.getBounds();
        boolean isIcon = window.isIcon();
        boolean isMaximum = window.isMaximum();

        if (window instanceof GameWindow) {
            isIcon = false;
            isMaximum = false;
        }

        return new WindowState(true, bounds, isIcon, isMaximum);
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

    //выход из приложения
    public void exitApplication() {
        boolean confirmed = ConfirmationDialog.showExitConfirmation();
        if (confirmed) {
            saveProfile();
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
            gameWindow.setSize(1180, 620);
            addWindow(gameWindow);
        } else {
            //если окно уже открыто, активируем его
            try {
                gameWindow.setSelected(true);
            } catch (Exception ex) {
                Logger.debug("Ошибка при активации окна: " + ex.getMessage());
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
                Logger.debug("Ошибка при активации окна: " + ex.getMessage());
            }
        }
    }
}
