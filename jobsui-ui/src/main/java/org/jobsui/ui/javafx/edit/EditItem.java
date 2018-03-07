package org.jobsui.ui.javafx.edit;

/**
 * Created by enrico on 4/28/17.
 */
class EditItem {
    final ItemType itemType;
    final Object payload;

    EditItem(ItemType itemType, Object payload) {
        this.itemType = itemType;
        this.payload = payload;

        if (!itemType.getPayloadType().isAssignableFrom(payload.getClass())) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return itemType.getTitleFunction().apply(payload);
    }
}
