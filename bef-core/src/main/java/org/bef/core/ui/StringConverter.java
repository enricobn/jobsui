package org.bef.core.ui;

/**
 * Created by enrico on 4/29/16.
 */
public interface StringConverter<T> {

    String toString(T value);

    T fromString(String value);

}
