package gui.menu;

import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import log.Logger;

public class ApplicationMenuBar extends JMenuBar { //выделила создание меню в отдельный класс
    private final MainApplicationFrame mainFrame;

    public ApplicationMenuBar(MainApplicationFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeMenu();
    }

    private void initializeMenu() {
        add(createLookAndFeelMenu());
        add(createTestMenu());
        add(createWindowMenu()); //меню управления окнами
    }

    private JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("Режим отображения");
        menu.setMnemonic(KeyEvent.VK_V);
        menu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        menu.add(createLookAndFeelMenuItem("Системная схема", KeyEvent.VK_S,
                UIManager.getSystemLookAndFeelClassName()));

        menu.add(createLookAndFeelMenuItem("Универсальная схема", KeyEvent.VK_U,
                UIManager.getCrossPlatformLookAndFeelClassName()));

        return menu;
    }


    private JMenuItem createLookAndFeelMenuItem(String text, int mnemonic, String lookAndFeelClassName) {
        JMenuItem menuItem = new JMenuItem(text, mnemonic);
        menuItem.addActionListener(event -> {
            mainFrame.setLookAndFeel(lookAndFeelClassName);
            mainFrame.invalidate(); // обновляем интерфейс
        });
        return menuItem;
    }


    private JMenu createTestMenu() {
        JMenu menu = new JMenu("Тесты");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription("Тестовые команды");

        menu.add(createTestMenuItem("Сообщение в лог", KeyEvent.VK_S));

        return menu;
    }


    private JMenuItem createTestMenuItem(String text, int mnemonic) {
        JMenuItem menuItem = new JMenuItem(text, mnemonic);
        menuItem.addActionListener(event -> {
            Logger.debug("Новая строка");
        });
        return menuItem;
    }


    //меню для управления окнами
    private JMenu createWindowMenu() {
        JMenu menu = new JMenu("Окна");
        menu.setMnemonic(KeyEvent.VK_O);
        menu.getAccessibleContext().setAccessibleDescription("Управление окнами приложения");

        //пункты для открытия окон
        JMenuItem openGameWindowItem = new JMenuItem("Открыть игровое окно", KeyEvent.VK_I);
        openGameWindowItem.addActionListener(event -> {
            mainFrame.openGameWindow();
        });
        menu.add(openGameWindowItem);

        JMenuItem openLogWindowItem = new JMenuItem("Открыть окно логов", KeyEvent.VK_P);
        openLogWindowItem.addActionListener(event -> {
            mainFrame.openLogWindow();
        });
        menu.add(openLogWindowItem);

        menu.addSeparator(); // Разделитель

        //пункт для закрытия игрового окна
        JMenuItem closeGameWindowItem = new JMenuItem("Закрыть игровое окно", KeyEvent.VK_G);
        closeGameWindowItem.addActionListener(event -> {
            mainFrame.closeGameWindow();
        });
        menu.add(closeGameWindowItem);

        //пункт для закрытия окна логов
        JMenuItem closeLogWindowItem = new JMenuItem("Закрыть окно логов", KeyEvent.VK_L);
        closeLogWindowItem.addActionListener(event -> {
            mainFrame.closeLogWindow();
        });
        menu.add(closeLogWindowItem);

        //пункт для выхода из приложения
        JMenuItem exitItem = new JMenuItem("Выход", KeyEvent.VK_Q);
        exitItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_Q, java.awt.Event.CTRL_MASK));
        exitItem.addActionListener(event -> {
            mainFrame.exitApplication();
        });
        menu.add(exitItem);

        return menu;
    }
}
