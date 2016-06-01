package org.jobsui.core.ui;

import java.util.List;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIChoice<T,C> extends UIComponent<T,C> {

    void setEnabled(boolean enable);

    void setItems(List<T> items);

}
