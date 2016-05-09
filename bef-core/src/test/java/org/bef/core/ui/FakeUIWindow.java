package org.bef.core.ui;

/**
 * Created by enrico on 5/8/16.
 */
public class FakeUIWindow<T> implements UIWindow<T> {
    private static final int TIMEOUT = 5000;
    private static final int SLEEP = 50;
    private volatile boolean exit = false;
    private volatile boolean started = false;
    private volatile boolean valid = false;

    @Override
    public boolean show() {
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
        return valid;
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public <T1> void add(String title, UIComponent<T1, T> component) {

    }

    @Override
    public <T1> void add(UIComponent<T1, T> component) {

    }

    @Override
    public void add(UIContainer<T> container) {

    }

    @Override
    public T getComponent() {
        return null;
    }

    public boolean isValid() {
        if (!started) {
            throw new IllegalStateException();
        }
        return valid;
    }

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
}
