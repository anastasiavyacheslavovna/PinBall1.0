package gui;

import gui.menu.MainApplicationFrame;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class WindowStateTest {
    private MainApplicationFrame mainFrame;
    private File profileFile;
    private static final String PROFILE_FILE = "application_profile.dat";

    @Before
    public void setUp() {
        ConfirmationDialog.setTestMode(true, true);
        profileFile = new File(PROFILE_FILE);
        deleteProfileFile();

        mainFrame = new MainApplicationFrame();
    }

    @After
    public void tearDown() {
        deleteProfileFile();
        if (mainFrame != null) {
            mainFrame.dispose();
        }
        ConfirmationDialog.resetTestMode();
    }


    private void deleteProfileFile() {
        if (profileFile.exists()) {
            boolean deleted = profileFile.delete();
            if (deleted) {
                System.out.println("Файл профиля удален: " + profileFile.getName());
            } else {
                System.err.println("Не удалось удалить файл профиля: " + profileFile.getName());
            }
        }
    }


    @Test
    public void testProfileContainsWindowStates() {
        try {
            saveProfile();
            ApplicationProfile profile = loadProfile();

            assertNotNull(profile);
            assertTrue(profile.isProfileExists());

            assertNotNull(profile.getLogWindowState());
            assertNotNull(profile.getGameWindowState());

            assertTrue(profile.getLogWindowState().isOpen());
            assertTrue(profile.getGameWindowState().isOpen());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testWindowPositionsAreSaved() {
        try {
            WindowPair window = getWindows();

            window.logWindow.setLocation(100, 100);
            window.logWindow.setSize(400, 500);

            window.gameWindow.setLocation(200, 150);
            window.gameWindow.setSize(600, 400);

            saveProfile();
            ApplicationProfile profile = loadProfile();

            Rectangle savedLogBounds = profile.getLogWindowState().getBounds();
            Rectangle savedGameBounds = profile.getGameWindowState().getBounds();

            assertNotNull(savedLogBounds);
            assertNotNull(savedGameBounds);

            assertEquals(100, savedLogBounds.x);
            assertEquals(100, savedLogBounds.y);
            assertEquals(400, savedLogBounds.width);
            assertEquals(500, savedLogBounds.height);

            assertEquals(200, savedGameBounds.x);
            assertEquals(150, savedGameBounds.y);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testProfileIsLoadedOnStartup() {
        try {
            ApplicationProfile testProfile = new ApplicationProfile();
            testProfile.setProfileExists(true);

            WindowState logState = new WindowState(true, new Rectangle(50, 50, 350, 450), false, false);
            WindowState gameState = new WindowState(true, new Rectangle(100, 100, 800, 600), false, false);
            testProfile.setLogWindowState(logState);
            testProfile.setGameWindowState(gameState);

            try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
                    Files.newOutputStream(Paths.get(PROFILE_FILE)))) {
                oos.writeObject(testProfile);
            }

            MainApplicationFrame newFrame = new MainApplicationFrame();

            Method loadProfileMethod = MainApplicationFrame.class.getDeclaredMethod("loadProfile");
            loadProfileMethod.setAccessible(true);
            ApplicationProfile loadedProfile = (ApplicationProfile) loadProfileMethod.invoke(newFrame);

            assertNotNull(loadedProfile);
            assertTrue(loadedProfile.isProfileExists());

            newFrame.dispose();

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRestoreProfileDialogShown() {
        try {
            ApplicationProfile testProfile = createTestProfile();
            saveTestProfile(testProfile);

            ConfirmationDialog.setTestMode(true, false);
            boolean resultNo = ConfirmationDialog.showRestoreProfileDialog();
            assertFalse(resultNo);

            ConfirmationDialog.setTestMode(true, true);
            boolean resultYes = ConfirmationDialog.showRestoreProfileDialog();
            assertTrue(resultYes);

            assertNotNull(resultNo);
            assertNotNull(resultYes);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDefaultWindowsCreatedWhenNoProfile() {
        try {
            assertFalse(profileFile.exists());

            saveProfile();
            WindowPair window = getWindows();

            assertNotNull(window.logWindow);
            assertNotNull(window.gameWindow);

            assertTrue(window.logWindow.isVisible());
            assertTrue(window.gameWindow.isVisible());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testProfileFileDeletedOnReject() {
        try {
            ApplicationProfile testProfile = createTestProfile();
            boolean saved = saveTestProfile(testProfile);

            assertTrue(saved);
            assertTrue(profileFile.exists());

            Method deleteProfileMethod = MainApplicationFrame.class.getDeclaredMethod("deleteProfileFile");
            deleteProfileMethod.setAccessible(true);
            deleteProfileMethod.invoke(mainFrame);

            assertFalse(profileFile.exists());


        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void testLogWindowStatePersistence() { //сохраняется положение маленького окна
        try {
            WindowPair windows = getWindows();
            LogWindow logWindow = windows.logWindow;

            logWindow.setLocation(150, 150);
            logWindow.setSize(450, 550);
            logWindow.setMaximum(true);

            saveProfile();
            ApplicationProfile profile = loadProfile();

            WindowState logState = profile.getLogWindowState();
            assertNotNull(logState);
            assertTrue(logState.isOpen());
            assertTrue(logState.isMaximum());

            Rectangle savedBounds = logState.getBounds();
            assertNotNull(savedBounds);
            assertTrue(savedBounds.width > 0);
            assertTrue(savedBounds.height > 0);

        } catch (Exception e) {
            fail("Тест провалился: " + e.getMessage());
        }
    }


    @Test
    public void testLogWindowRestoreFromMaximized() { //развернутое после сохранения
        try {
            ApplicationProfile testProfile = new ApplicationProfile();
            testProfile.setProfileExists(true);

            testProfile.setLogWindowState(new WindowState(true, new Rectangle(100, 100, 500, 400), false, true));
            testProfile.setGameWindowState(new WindowState(true, new Rectangle(50, 50, 800, 600), false, false));

            saveTestProfile(testProfile);

            MainApplicationFrame newFrame = new MainApplicationFrame();

            Field logWindowField = MainApplicationFrame.class.getDeclaredField("logWindow");
            logWindowField.setAccessible(true);
            LogWindow restoredLogWindow = (LogWindow) logWindowField.get(newFrame);

            assertNotNull(restoredLogWindow);
            assertTrue(restoredLogWindow.isMaximum());
            assertTrue(restoredLogWindow.isVisible());

            newFrame.dispose();

        } catch (Exception e) {
            fail("Тест провалился: " + e.getMessage());
        }
    }

    @Test
    public void testLogWindowRestoreFromIconified() { //свернутость при сохранении
        try {
            ApplicationProfile testProfile = new ApplicationProfile();
            testProfile.setProfileExists(true);

            testProfile.setLogWindowState(new WindowState(true, new Rectangle(100, 100, 500, 400), true, false));
            testProfile.setGameWindowState(new WindowState(true, new Rectangle(50, 50, 800, 600), false, false));

            saveTestProfile(testProfile);

            MainApplicationFrame newFrame = new MainApplicationFrame();

            Field logWindowField = MainApplicationFrame.class.getDeclaredField("logWindow");
            logWindowField.setAccessible(true);
            LogWindow restoredLogWindow = (LogWindow) logWindowField.get(newFrame);

            assertNotNull(restoredLogWindow);
            assertTrue(restoredLogWindow.isIcon());

            newFrame.dispose();

        } catch (Exception e) {
            fail("Тест провалился: " + e.getMessage());
        }
    }

    @Test
    public void testLogWindowCloseConfirmation() { //закрытие
        try {
            WindowPair windows = getWindows();
            LogWindow logWindow = windows.logWindow;

            boolean closeResult = logWindow.closeWindow();

            assertTrue(closeResult);

        } catch (Exception e) {
            fail("Тест провалился: " + e.getMessage());
        }
    }


    private void saveProfile() throws Exception {
        Method saveProfileMethod = MainApplicationFrame.class.getDeclaredMethod("saveProfile");
        saveProfileMethod.setAccessible(true);
        saveProfileMethod.invoke(mainFrame);
    }


    private ApplicationProfile loadProfile() throws Exception {
        Method loadProfileMethod = MainApplicationFrame.class.getDeclaredMethod("loadProfile");
        loadProfileMethod.setAccessible(true);
        return (ApplicationProfile) loadProfileMethod.invoke(mainFrame);
    }


    private WindowPair getWindows() throws Exception {
        Field logWindowField = MainApplicationFrame.class.getDeclaredField("logWindow");
        Field gameWindowField = MainApplicationFrame.class.getDeclaredField("gameWindow");
        logWindowField.setAccessible(true);
        gameWindowField.setAccessible(true);

        LogWindow logWindow = (LogWindow) logWindowField.get(mainFrame);
        GameWindow gameWindow = (GameWindow) gameWindowField.get(mainFrame);

        assertNotNull(logWindow);
        assertNotNull(gameWindow);

        return new WindowPair(logWindow, gameWindow);
    }


    private static class WindowPair {
        final LogWindow logWindow;
        final GameWindow gameWindow;

        WindowPair(LogWindow logWindow, GameWindow gameWindow) {
            this.logWindow = logWindow;
            this.gameWindow = gameWindow;
        }
    }


    private ApplicationProfile createTestProfile() {
        ApplicationProfile testProfile = new ApplicationProfile();
        testProfile.setProfileExists(true);
        testProfile.setLogWindowState(new WindowState(true, new Rectangle(50, 50, 300, 400), false, false));
        testProfile.setGameWindowState(new WindowState(true, new Rectangle(100, 100, 800, 600), false, false));
        return testProfile;
    }


    private boolean saveTestProfile(ApplicationProfile profile) {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
                Files.newOutputStream(Paths.get(PROFILE_FILE)))) {
            oos.writeObject(profile);
            return true;
        } catch (IOException e) {
            fail(e.getMessage());
            return false;
        }
    }
}