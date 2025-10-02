package gui;

import java.io.Serializable;


public class ApplicationProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private WindowState logWindowState;
    private WindowState gameWindowState;
    private boolean profileExists;

    public ApplicationProfile() {
        this.profileExists = false;
    }


    public WindowState getLogWindowState() { return logWindowState; }
    public void setLogWindowState(WindowState logWindowState) { this.logWindowState = logWindowState; }

    public WindowState getGameWindowState() { return gameWindowState; }
    public void setGameWindowState(WindowState gameWindowState) { this.gameWindowState = gameWindowState; }

    public boolean isProfileExists() { return profileExists; }
    public void setProfileExists(boolean profileExists) { this.profileExists = profileExists; }
}
