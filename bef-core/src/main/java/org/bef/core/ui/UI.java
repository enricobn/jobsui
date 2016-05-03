package org.bef.core.ui;

/**
 * Created by enrico on 11/2/15.
 */
public interface UI<T> {

//    <T> T get(String title, T[] values);

    void showMessage(String message);

    UIWindow<T> createWindow(String title);

    void log(String message);

    void log(String message, Throwable th);

}
