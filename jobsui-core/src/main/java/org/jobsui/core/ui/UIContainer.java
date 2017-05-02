package org.jobsui.core.ui;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIContainer<C> {

    void add(UIWidget<C> widget);

    void add(UIContainer<C> container);

    C getComponent();

    void clear();
}
