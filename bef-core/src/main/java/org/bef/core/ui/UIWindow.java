package org.bef.core.ui;

/**
 * Created by enrico on 2/24/16.
 */
public interface UIWindow<T> {

    boolean show();

    void setValid(boolean valid);

    UIContainer<T> addContainer();

}
