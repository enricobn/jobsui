package org.bef.core.ui;

import java.util.List;

/**
 * Created by enrico on 2/14/16.
 */
public interface UIContainer<C> {

    <T> UIChoice<T,C> addChoice(String title, T[] items);

    UIButton<C> addButton(String title);

    <T> UIList<T,C> addList(List<T> items, boolean allowRemove);

    <T> UIValue<T,C> add(String title, StringConverter<T> converter, T defaultValue);

    <T> void add(String title, UIComponent<T,C> component);

}
