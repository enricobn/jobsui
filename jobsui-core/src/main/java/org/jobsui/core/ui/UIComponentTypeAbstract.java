package org.jobsui.core.ui;

public abstract class UIComponentTypeAbstract implements UIComponentType {
    private final String name;

    public UIComponentTypeAbstract(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
