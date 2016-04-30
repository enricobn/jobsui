package org.bef.core.ui;

import java.util.List;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIContainer {

    <T> UIChoice<T> addChoice(String title, T[] items);

    UIButton addButton(String title);

    <T> UIList<T> addList(List<T> items, boolean allowRemove);

    <T> UIValue<T> add(String title, StringConverter<T> converter, T defaultValue);
}
