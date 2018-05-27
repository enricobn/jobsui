package org.jobsui.core.ui;

import rx.Observable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Created by enrico on 5/10/16.
 */
public interface UIWidget<C> {

    void setVisible(boolean visible);

    void setDisable(boolean value);

    UIComponent<C> getComponent();

    void setValidationMessages(List<String> messages);

    boolean isEnabled();

    default C getLayoutComponent() {
        return getComponent().getComponent();
    }

    Optional<Observable<Serializable>> getButtonForDefaultObservable();
}
