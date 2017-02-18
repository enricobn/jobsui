package org.jobsui.core.ui;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by enrico on 2/24/16.
 */
public interface UIList<T extends Serializable,C> extends UIComponent<ArrayList<T>,C> {

//    void setItems(List<T> items);

    void addItem(T item);

    void setAllowRemove(boolean allowRemove);

}
