package org.jobsui.ui.javafx.edit;

/**
 * Created by enrico on 4/28/17.
 */
public class EditItem {
    public final ItemType itemType;
    public final Object payload;
    private boolean changed;

    EditItem(ItemType itemType, Object payload) {
        this.itemType = itemType;
        this.payload = payload;

        if (!itemType.getPayloadType().isAssignableFrom(payload.getClass())) {
            throw new IllegalArgumentException();
        }
    }

    void setChanged(boolean changed) {
        this.changed = changed;
    }

    @Override
    public String toString() {
        return itemType.getTitleFunction().apply(payload) + (changed ? " *" : "");
    }

    boolean isChanged() {
        return changed;
    }
}
