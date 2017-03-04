package org.jobsui.core.ui.javafx;

import javafx.scene.Node;
import javafx.scene.control.Button;
import org.jobsui.core.SerializableVoid;
import org.jobsui.core.ui.UIButton;
import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 5/31/16.
 */
public class JavaFXUIButton implements UIButton<Node> {
    private final Button component = new Button();
    private final Observable<SerializableVoid> observable;
    private final List<Subscriber<? super SerializableVoid>> subscribers = new ArrayList<>();

    public JavaFXUIButton() {
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            subscribers.add(subscriber);
        });
        component.setOnAction(event -> notifySubscribers());
    }

    @Override
    public Observable<SerializableVoid> getObservable() {
        return observable;
    }

    @Override
    public Node getComponent() {
        return component;
    }

    @Override
    public SerializableVoid getValue() {
        return null;
    }

    @Override
    public void notifySubscribers() {
        for (Subscriber<? super SerializableVoid> subscriber : subscribers) {
            subscriber.onNext(getValue());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        component.setVisible(visible);
    }

    @Override
    public void setValue(SerializableVoid value) {
    }

    @Override
    public void setTitle(String label) {
        component.setText(label);
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setDisable(!enable);
    }
}
