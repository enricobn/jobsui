package org.jobsui.core.ui;

/**
 * Created by enrico on 2/24/16.
 */
public interface UIWindow<T> extends UIContainer<T> {

    boolean show();

    void setValid(boolean valid);

    void showValidationMessage(String message);

//    UIContainer<T> addContainer();

}
