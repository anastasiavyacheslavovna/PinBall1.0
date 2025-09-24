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
            mainFrame.invalidate(); // Обновляем интерфейс
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
}
