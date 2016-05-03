package org.bef.core.ui;

import java.util.List;

/**
 * Created by enrico on 2/24/16.
 */
public interface UIList<T,C> extends UIComponent<List<T>,C> {

    void setItems(List<T> items);

    void addItem(T item);

    List<T> getItems();
}
