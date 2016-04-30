package org.bef.core.ui;

/**
 * Created by enrico on 4/29/16.
 */
public class StringConverterString implements StringConverter<String> {
    @Override
    public String toString(String value) {
        return value;
    }

    @Override
    public String fromString(String value) {
        return value;
    }
}
