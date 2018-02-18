package org.jobsui.core.ui;

/**
 * Created by enrico on 4/2/17.
 */
public interface UIComponentType {

    String getName();

    <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException;
}
