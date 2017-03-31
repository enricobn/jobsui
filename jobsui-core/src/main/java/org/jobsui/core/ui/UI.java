package org.jobsui.core.ui;

/**
 * Created by enrico on 11/2/15.
 */
public interface UI<T> {

//    <T> T get(String title, T[] values);

    void showMessage(String message);

    UIWindow<T> createWindow(String title);

    void log(String message);

    void log(String message, Throwable th);

    <COMP extends UIComponent> COMP create(Class<COMP> componentType) throws UnsupportedComponentException;

    void showError(String message, Throwable t);

    void start(String[] args);

}
