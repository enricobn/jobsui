package org.bef.core.ui;

/**
 * Created by enrico on 5/8/16.
 */
public class FakeUIWindow<T> implements UIWindow<T> {
    private static final int SLEEP = 50;
    private boolean exit = false;
    private boolean valid = false;
    private boolean started = false;

    @Override
    public boolean show() {
        started = true;
        while (!exit) {
            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
        return valid;
    }

    public void exit() {
        this.exit = true;
    }

    public void waitUntilStarted() {
        while (!started) {
            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
