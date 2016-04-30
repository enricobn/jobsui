package org.bef.core.ui;

/**
 * Created by enrico on 2/24/16.
 */
public interface UIWindow {

    boolean show();

    void setValid(boolean valid);

    UIContainer addContainer();

}
