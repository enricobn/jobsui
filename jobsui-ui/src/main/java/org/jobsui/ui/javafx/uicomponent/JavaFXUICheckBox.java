package org.jobsui.ui.javafx.uicomponent;

import com.jfoenix.controls.JFXCheckBox;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import org.jobsui.core.ui.JobsUITheme;
import org.jobsui.core.ui.UICheckBox;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 5/31/16.
 */
public class JavaFXUICheckBox implements UICheckBox<Node> {
    private final CheckBox component;
    private final Observable<Serializable> observable;
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();
    private final JavaFXUI ui;

    public JavaFXUICheckBox(JavaFXUI ui) {
        this.ui = ui;
        component = createCheckBox();
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            subscribers.add(subscriber);
        });
        component.setOnAction(event -> notifySubscribers());
    }

    @Override
    public Observable<Serializable> getObservable() {
        return observable;
    }

    @Override
    public Node getComponent() {
        return component;
    }

    @Override
    public Boolean getValue() {
        return component.isSelected();
    }

    @Override
    public void notifySubscribers() {
        for (Subscriber<? super Boolean> subscriber : subscribers) {
            subscriber.onNext(getValue());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        component.setVisible(visible);
    }

    @Override
    public void setValue(Serializable value) {
        component.setSelected((Boolean) value);

        // if getScene() == null then the change is not automatically notified
        if (!component.isVisible() || component.getScene() == null) {
            notifySubscribers();
        }
    }

    @Override
    public void setTitle(String label) {
//        component.setText(label);
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setDisable(!enable);
    }

    private CheckBox createCheckBox() {
        CheckBox result;
        if (ui.getPreferences().getTheme() == JobsUITheme.Material) {
            result = new JFXCheckBox();
        } else {
            result = new CheckBox();
        }
        return result;
    }
}
