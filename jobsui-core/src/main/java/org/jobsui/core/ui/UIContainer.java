package org.jobsui.core.ui;

import java.io.Serializable;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIContainer<C> {

    <T extends Serializable> UIWidget<T,C> add(String title, UIComponent<T,C> component);

    <T extends Serializable> UIWidget<T,C> add(UIComponent<T,C> component);

    void add(UIContainer<C> container);

    C getComponent();

}
