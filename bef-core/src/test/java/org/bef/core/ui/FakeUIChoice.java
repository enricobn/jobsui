package org.bef.core.ui;

import rx.functions.Action1;

/**
 * Created by enrico on 5/5/16.
 */
public class FakeUIChoice<T, C> extends FakeUIComponent<T, C> implements UIChoice<T, C> {
    private T[] items;
    private T selectedItem;

    @Override
    public C getComponent() {
        return null;
    }

    @Override
    public void setEnabled(boolean enable) {

    }

    @Override
    public T getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void setItems(T[] items) {
        this.items = items;
        if (items.length == 0) {
            setSelectedItem(null);
        } else if (items.length == 1) {
            setSelectedItem(items[0]);
        }
    }

    public void setSelectedItem(T item) {
        this.selectedItem = item;
        for (Action1 action1 : actions) {
            action1.call(item);
        }
    }
}
