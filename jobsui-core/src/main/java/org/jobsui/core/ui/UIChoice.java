package org.jobsui.core.ui;

import java.io.Serializable;
import java.util.List;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIChoice<T extends Serializable,C> extends UIComponent<T,C> {

    void setEnabled(boolean enable);

    void setItems(List<T> items);

}
