package org.jobsui.core.ui;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIContainer<C> {

    UIWidget<C> add(String title, UIComponent<C> component);

    UIWidget<C> add(UIComponent<C> component);

    void add(UIContainer<C> container);

    C getComponent();

}
