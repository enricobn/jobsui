package org.jobsui.core.ui;

import rx.functions.Action1;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertTrue;

/**
 * Created by enrico on 5/5/16.
 */
public class FakeUIChoice extends FakeUIComponent implements UIChoice<FakeComponent> {
    private List<Serializable> items = Collections.emptyList();
    private Serializable selectedItem;

    @Override
    public void setEnabled(boolean enable) {

    }

    @Override
    public Serializable getValue() {
        return selectedItem;
    }

    @Override
    public void setItems(List<Serializable> items) {
        this.items = items;
        if (items.size() == 0) {
            setValue(null);
        } else if (items.size() == 1) {
            setValue(items.get(0));
        }
    }

    @Override
    public void setValue(Serializable item) {
        boolean found = false;
        for (Serializable t : items) {
            if (Objects.equals(t, item)) {
                found = true;
                break;
            }
        }

        if (item != null) {
            assertTrue(Objects.toString(item), found);
        }

        this.selectedItem = item;
        for (Action1<Serializable> action1 : actions) {
            action1.call(item);
        }
    }

    public void setSelectedItemByToString(String s) {
        for (Serializable item : items) {
            if (Objects.equals(s, item == null ? null : item.toString())) {
                setValue(item);
                return;
            }
        }
        throw new IllegalArgumentException("Cannot find item do \"" + s + "\".");
    }

    public List<Serializable> getItems() {
        return items;
    }
}
