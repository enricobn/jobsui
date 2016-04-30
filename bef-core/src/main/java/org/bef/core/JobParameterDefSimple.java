package org.bef.core;

import com.sun.javafx.scene.layout.region.Margins;
import org.bef.core.ui.StringConverter;
import org.bef.core.ui.UIContainer;
import org.bef.core.ui.UIValue;
import rx.Observable;

import java.util.Collections;
import java.util.List;

/**
 * Created by enrico on 4/30/16.
 */
public class JobParameterDefSimple<T> implements JobParameterDef<T> {
    private final String key;
    private final String name;
    private final Class<T> type;
    private final StringConverter<T> converter;
    private final ParameterValidator<T> validator;

    public JobParameterDefSimple(String key, String name, Class<T> type, StringConverter<T> converter, ParameterValidator<T> validator) {
        this.key = key;
        this.name = name;
        this.type = type;
        this.converter = converter;
        this.validator = validator;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public T getDefaultValue() {
        return null;
    }

    @Override
    public Observable<T> addToUI(UIContainer container) {
        final UIValue<T> value = container.add(getName(), converter, getDefaultValue());
        return value.getObservable();
    }

    @Override
    public List<JobParameterDef<?>> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public List<String> validate(T value) {
        return validator.validate(value);
    }
}
