package org.jobsui.core.ui;

import java.io.Serializable;

/**
 * Created by enrico on 4/2/17.
 */
public interface UIComponentType extends Serializable {

    String getName();

    <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException;
}
