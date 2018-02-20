package org.jobsui.core.ui;

import org.jobsui.core.CommandLineArguments;
import org.jobsui.core.JobsUIPreferences;

import java.util.Optional;

/**
 * Created by enrico on 11/2/15.
 */
public interface UI<T> {

//    <T> T get(String title, T[] values);

    void showMessage(String message);

    UIWindow<T> createWindow(String title);

    void log(String message);

    void log(String message, Throwable th);

    UIButton<T> createButton();

    UICheckBox<T> createCheckBox();

    UIChoice<T> createChoice();

    UIList<T> createList();

    UIPassword<T> createPassword();

    UIValue<T> createValue();

    UIFileChooser<T> createFileChooser();

    void showError(String message, Throwable t);

    void start(CommandLineArguments arguments);

    Optional<String> askString(String message);

    boolean askOKCancel(String message);

    JobsUIPreferences getPreferences();

    UIWidget<T> createWidget(String title, UIComponent<T> component);

}
