package org.jobsui.core.ui;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIContainer<C> {

    <T> UIWidget<T,C> add(String title, UIComponent<T,C> component);

    <T> UIWidget<T,C> add(UIComponent<T,C> component);

    void add(UIContainer<C> container);

    C getComponent();

}