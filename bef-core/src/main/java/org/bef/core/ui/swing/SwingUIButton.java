package org.bef.core.ui.swing;

import org.bef.core.ui.UIButton;
import org.bef.core.ui.UIComponentAbstract;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;

/**
 * Created by enrico on 2/24/16.
 */
public class SwingUIButton implements UIButton {
    private final JButton component = new JButton();
    private final Observable<Void> observable;

    public SwingUIButton() {
        observable = Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                subscriber.onStart();
                component.addActionListener(new ActionListener() {
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

    @Override
    public void setEnabled(boolean enabled) {
        component.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return component.isEnabled();
    }

    public JComponent getComponent() {
        return component;
    }

    public void setText(String title) {
        component.setText(title);
    }

    public String getText() {
        return component.getText();
    }
}
