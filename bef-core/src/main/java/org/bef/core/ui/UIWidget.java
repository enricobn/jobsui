package org.bef.core.ui;

/**
 * Created by enrico on 5/10/16.
 */
public interface UIWidget<T, C> {

    void setVisible(boolean visible);

    UIComponent<T,C> getComponent();

}
