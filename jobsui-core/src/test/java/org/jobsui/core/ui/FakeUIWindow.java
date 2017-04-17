package org.jobsui.core.ui;

import org.jobsui.core.Project;
import org.jobsui.core.job.Job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by enrico on 5/8/16.
 */
public class FakeUIWindow<T extends Serializable> implements UIWindow<T> {
    private static final int TIMEOUT = 5_000;
    private static final int SLEEP = 50;
    private volatile boolean exit = false;
    private volatile boolean started = false;
//    private volatile boolean valid = false;
    private volatile AtomicInteger unnamed = new AtomicInteger(0);
    private Map<String, UIWidget> widgets = new HashMap<>();
    private List<String> validationMessages = new ArrayList<>();

    @Override
    public void show(Project project, Job job, Runnable callback) {
        callback.run();

        long start = System.currentTimeMillis();
        started = true;
        while (!exit) {
            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (System.currentTimeMillis() - start > TIMEOUT) {
                throw new IllegalStateException("Timeout on show.");
            }
        }
//        return valid;
    }

//    @Override
//    public void setValid(boolean valid) {
//        this.valid = valid;
//    }

    @Override
    // TODO I don't like it here, it must know nothing about validation
    public void showValidationMessage(String message) {
        validationMessages.add(message);
    }

    @Override
    public UIWidget<T> add(String title, final UIComponent<T> component) {
        final UIWidget widget = mock(UIWidget.class);
        when(widget.getComponent()).thenReturn(component);
        widgets.put(title, widget);
        return widget;
    }

    @Override
    public UIWidget<T> add(UIComponent<T> component) {
        return add(Integer.toString(unnamed.addAndGet(1)), component);
    }

    @Override
    public void add(UIContainer<T> container) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T getComponent() {
        return null;
    }

//    public boolean isValid() {
//        if (!started) {
//            throw new IllegalStateException();
//        }
//        return valid;
//    }

    public void exit() {
        this.exit = true;
    }

    public void waitUntilStarted() {
        long start = System.currentTimeMillis();
        while (!started) {
            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (System.currentTimeMillis() - start > TIMEOUT) {
                throw new IllegalStateException("Timeout on waitUntilStarted.");
            }
        }
    }

    public UIWidget getWidget(String title) {
        return widgets.get(title);
    }

    // TODO I don't like it here, it must know nothing about validation
    public List<String> getValidationMessages() {
        return validationMessages;
    }
}
