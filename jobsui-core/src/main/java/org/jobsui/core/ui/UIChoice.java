package org.jobsui.core.ui;

import java.io.Serializable;
import java.util.List;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIChoice<C> extends UIComponent<C> {

    void setEnabled(boolean enable);

    void setItems(List<Serializable> items);

}
