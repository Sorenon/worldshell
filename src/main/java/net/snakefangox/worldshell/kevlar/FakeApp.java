package net.snakefangox.worldshell.kevlar;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.Clipboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FakeApp implements Application {
    @Override
    public ApplicationListener getApplicationListener() {
        return null;
    }

    @Override
    public Graphics getGraphics() {
        return null;
    }

    @Override
    public Audio getAudio() {
        return null;
    }

    @Override
    public Input getInput() {
        return null;
    }

    @Override
    public Files getFiles() {
        return null;
    }

    @Override
    public Net getNet() {
        return null;
    }

    private static final Logger LOGGER = LogManager.getLogger("GrappleShip");
    
    @Override
    public void log(String tag, String message) {
        LOGGER.info(message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        LOGGER.info(message);
        exception.printStackTrace();
    }

    @Override
    public void error(String tag, String message) {
        LOGGER.error(message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        LOGGER.error(message);
        exception.printStackTrace();
    }

    @Override
    public void debug(String tag, String message) {
        LOGGER.info(message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        LOGGER.info(message);
        exception.printStackTrace();
    }

    @Override
    public void setLogLevel(int logLevel) {

    }

    @Override
    public int getLogLevel() {
        return 0;
    }

    @Override
    public void setApplicationLogger(ApplicationLogger applicationLogger) {

    }

    @Override
    public ApplicationLogger getApplicationLogger() {
        return null;
    }

    @Override
    public ApplicationType getType() {
        return null;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public long getJavaHeap() {
        return 0;
    }

    @Override
    public long getNativeHeap() {
        return 0;
    }

    @Override
    public Preferences getPreferences(String name) {
        return null;
    }

    @Override
    public Clipboard getClipboard() {
        return null;
    }

    @Override
    public void postRunnable(Runnable runnable) {

    }

    @Override
    public void exit() {

    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {

    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {

    }
}
