package org.jobsui.core.ui;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 5/5/16.
 */
public abstract class FakeUIComponent<T extends Serializable, C> implements UIComponent<T, C> {
    private final Observable<T> observable;
    protected final List<Action1<T>> actions = new ArrayList<>();
    private final List<Subscriber<? super T>> subscribers = new ArrayList<>();
    private boolean visible = true;
    private boolean enabled = true;

    public FakeUIComponent() {
        observable = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                actions.add(new Action1<T>() {
                    @Override
                    public void call(T o) {
                        subscriber.onNext(o);
                    }
                });
                subscribers.add(subscriber);
            }
        });
    }

    @Override
    public Observable<T> getObservable() {
        return observable;
    }

    @Override
    public void notifySubscribers() {
        for (Subscriber<? super T> subscriber : subscribers) {
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
}
