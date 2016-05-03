package org.bef.core.ui;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIContainer<C> {

    <T> void add(String title, UIComponent<T,C> component);

    <T> void add(UIComponent<T,C> component);

    void add(UIContainer<C> container);

    C getComponent();

}
