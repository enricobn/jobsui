package org.jobsui.core.ui;

import java.io.Serializable;

/**
 * Created by enrico on 2/24/16.
 */
public interface UIList<C> extends UIComponent<C> {

//    void setItems(List<T> items);

    void addItem(Serializable item);

    void setAllowRemove(boolean allowRemove);

}
