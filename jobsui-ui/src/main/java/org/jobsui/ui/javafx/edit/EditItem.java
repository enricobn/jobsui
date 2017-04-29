package org.jobsui.ui.javafx.edit;

import java.util.function.Supplier;

/**
 * Created by enrico on 4/28/17.
 */
class EditItem {
    private final Supplier<String> title;
    final EditProject.ItemType itemType;
    final Object payload;

    EditItem(EditProject.ItemType itemType, Supplier<String> title, Object payload) {
        this.itemType = itemType;
        this.title = title;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return title.get();
    }
}
