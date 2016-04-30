package org.bef.core.ui.swing;

import org.bef.core.ui.UIButton;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by enrico on 2/24/16.
 */
public class SwingUIButton extends JButton implements UIButton {
    private final Observable<Void> observable;

    public SwingUIButton() {
        observable = Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                subscriber.onStart();
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        subscriber.onNext(null);
                    }
                });
            }
        });
    }

    @Override
    public Observable<Void> getObservable() {
        return observable;
    }

}
