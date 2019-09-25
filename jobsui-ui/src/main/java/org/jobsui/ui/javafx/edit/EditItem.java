package org.jobsui.ui.javafx.edit;

import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 4/28/17.
 */
public class EditItem {
    public final ItemType itemType;
    public final Object payload;
    private boolean changed;
    private boolean valid = true;
    private final Observable<EditItem> observable;
    private final List<Subscriber<? super EditItem>> subscribers = new ArrayList<>();

    EditItem(ItemType itemType, Object payload) {
        this.itemType = itemType;
        this.payload = payload;

        if (!itemType.getPayloadType().isAssignableFrom(payload.getClass())) {
            throw new IllegalArgumentException();
        }
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            subscribers.add(subscriber);
        });
    }

    public Observable<EditItem> getObservable() {
        return observable;
    }

    void setChanged(boolean changed) {
        this.changed = changed;
        notifySubscribers();
    }

    @Override
    public String toString() {
        return itemType.getTitleFunction().apply(payload) + (changed ? " *" : "");
    }

    boolean isChanged() {
        return changed;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
        notifySubscribers();
    }

    public boolean isValid() {
        return valid;
    }

    private void notifySubscribers() {
        for (Subscriber<? super EditItem> subscriber : subscribers) {
            subscriber.onNext(this);
        }
    }
}
