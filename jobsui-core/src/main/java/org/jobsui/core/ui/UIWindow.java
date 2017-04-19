package org.jobsui.core.ui;

import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.job.Project;
import org.jobsui.core.job.Job;

import java.util.function.Consumer;

/**
 * Created by enrico on 2/24/16.
 */
public interface UIWindow<T> extends UIContainer<T> {

    /**
     * Shows the main window and waits for user interaction. It must return when the user has confirmed or
     * has cancelled the operation or has closed the window.
     * @param project
     * @param job
     * @param callback the creation of components is done in the callback, so implementations must call it
     */
    void show(Project project, Job job, Runnable callback);

//    void setValid(boolean valid);

    void showValidationMessage(String message);

    void addButton(UIButton<T> button);

    void setOnOpenBookmark(Consumer<Bookmark> onOpenBookmark);

//    UIContainer<T> addContainer();

}
