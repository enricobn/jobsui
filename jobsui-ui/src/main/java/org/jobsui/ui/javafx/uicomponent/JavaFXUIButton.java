package org.jobsui.ui.javafx.uicomponent;

import com.jfoenix.controls.JFXButton;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.jobsui.core.ui.JobsUITheme;
import org.jobsui.core.ui.UIButton;
import org.jobsui.ui.javafx.JavaFXUI;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 5/31/16.
 */
public class JavaFXUIButton implements UIButton<Node> {
    private final Button component;
    private final Observable<Serializable> observable;
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();
    private final JavaFXUI ui;

    public JavaFXUIButton(JavaFXUI ui) {
        this.ui = ui;
        component = createButton();
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
    public Serializable getValue() {
        return null;
    }

    @Override
    public void notifySubscribers() {
        for (Subscriber<? super Serializable> subscriber : subscribers) {
            subscriber.onNext(getValue());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        component.setVisible(visible);
    }

    @Override
    public void setValue(Serializable value) {
    }

    @Override
    public void setTitle(String label) {
        component.setText(label);
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setDisable(!enable);
    }

    private Button createButton() {
        Button result;
        if (ui.getPreferences().getTheme() == JobsUITheme.Material) {
            result = new JFXButton();
            result.getStyleClass().add("button-raised");
        } else {
            result = new Button();
        }
        return result;
    }
}
