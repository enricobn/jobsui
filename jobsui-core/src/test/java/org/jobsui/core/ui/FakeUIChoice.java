package org.jobsui.core.ui;

import rx.functions.Action1;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertTrue;

/**
 * Created by enrico on 5/5/16.
 */
public class FakeUIChoice<T, C> extends FakeUIComponent<T, C> implements UIChoice<T, C> {
    private List<T> items;
    private T selectedItem;

    @Override
    public C getComponent() {
        return null;
    }

    @Override
    public void setEnabled(boolean enable) {

    }

    @Override
    public T getValue() {
        return selectedItem;
    }

    @Override
    public void setItems(List<T> items) {
        this.items = items;
        if (items.size() == 0) {
            setValue(null);
        } else if (items.size() == 1) {
            setValue(items.get(0));
        }
    }

    @Override
    public void setValue(T item) {
        boolean found = false;
        for (T t : items) {
            if (Objects.equals(t, item)) {
                found = true;
                break;
            }
        }

        assertTrue(Objects.toString(item), found);

        this.selectedItem = item;
        for (Action1<T> action1 : actions) {
            action1.call(item);
        }
    }

    public void setSelectedItemByToString(String s) {
        for (T item : items) {
            if (Objects.equals(s, item == null ? null : item.toString())) {
                setValue(item);
                return;
            }
        }
        throw new IllegalArgumentException("Cannot find item do \"" + s + "\".");
    }

    public List<T> getItems() {
        return items;
    }
}
