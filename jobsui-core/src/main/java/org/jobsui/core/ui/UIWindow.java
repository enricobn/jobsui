package org.jobsui.core.ui;

/**
 * Created by enrico on 2/24/16.
 */
public interface UIWindow<T> extends UIContainer<T> {

    /**
     * Shows the main window and waits for user interaction. It must return when the user has confirmed or
     * has cancelled the operation or has closed the window.
     * @param callback the creation of components is done in the callback, so implementations must call it
     *                 after the UI toolkit has been initialized.
     */
    void show(Runnable callback);

//    void setValid(boolean valid);

    void showValidationMessage(String message);

//    UIContainer<T> addContainer();

}
