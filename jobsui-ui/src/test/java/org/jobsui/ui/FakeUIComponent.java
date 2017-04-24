package org.jobsui.ui;

import org.jobsui.core.ui.UIComponent;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 5/5/16.
 */
public abstract class FakeUIComponent implements UIComponent<FakeComponent> {
    private final Observable<Serializable> observable;
    protected final List<Action1<Serializable>> actions = new ArrayList<>();
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();
    private boolean visible = true;
    private boolean enabled = true;

    public FakeUIComponent() {
        observable = Observable.create(subscriber -> {
            actions.add(subscriber::onNext);
            subscribers.add(subscriber);
        });
    }

    @Override
    public Observable<Serializable> getObservable() {
        return observable;
    }

    @Override
    public void notifySubscribers() {
        for (Subscriber<? super Serializable> subscriber : subscribers) {
            subscriber.onNext(getValue());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }


    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setTitle(String label) {
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public final FakeComponent getComponent() {
        return null;
    }

}
