package org.jobsui.core.ui.javafx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import org.jobsui.core.ui.UIChoice;
import org.jobsui.core.ui.swing.SwingFilteredList;
import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by enrico on 10/7/16.
 */
public class JavaFXUIChoice<T> implements UIChoice<T, Node> {
    private FlowPane component = null;
    private ComboBox<T> combo = null;
    private Button button = null;
    private final List<Subscriber<? super T>> subscribers = new ArrayList<>();
    private Observable<T> observable;
    private List<T> items = new ArrayList<>();
    private String title;
    private boolean disableListener = false;
    private AtomicBoolean initialized = new AtomicBoolean(false);
    private boolean visible = true;

    public JavaFXUIChoice() {
        observable = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                subscribers.add(subscriber);
            }
        });
    }

    @Override
    public Observable<T> getObservable() {
        return observable;
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setDisable(!enable);
    }

    @Override
    public T getValue() {
        if (!initialized.get()) {
            if (items.size() == 1) {
                return items.get(0);
            }
            return null;
        }
        return combo.getSelectionModel().getSelectedItem();
    }

    @Override
    public void setItems(final List<T> items) {
//        System.out.println("SwingUIChoice.setItems " + items);
//        if (items.equals(this.items)) {
//            return;
//        }

        this.items = items;

        if (initialized.get()) {
            updateItems();
        }
    }

    private void updateItems() {
        final T selectedItem = combo.getSelectionModel().getSelectedItem();

        disableListener = true;
        combo.getSelectionModel().clearSelection();
        combo.getItems().clear();
        disableListener = false;

        if (items.size() > 1) {
            combo.getItems().add(null);
        }

        boolean found = false;
        for (T v : items) {
            combo.getItems().add(v);
            if (Objects.equals(selectedItem, v)) {
                found = true;
            }
        }

        if (found) {
            combo.getSelectionModel().select(selectedItem);
        } else {
            if (items.size() == 1) {
                combo.getSelectionModel().select(items.get(0));
            } else {
                // when no items are set, I want to be sure that subscribers are notified,
                // even if the value was already null, but I want to do it only once
                // so I disable listener
                disableListener = true;
                combo.getSelectionModel().select(null);
                disableListener = false;
                notifySubscribers();
            }
        }
    }

    @Override
    public Node getComponent() {
        initialize();
        return component;
    }

    private void initialize() {
        if (initialized.compareAndSet(false, true)) {
            component = new FlowPane();
            component.setHgap(5);
            component.setVisible(visible);

            combo = new ComboBox<>();

            combo.setOnAction(new EventHandler<ActionEvent>() {
                private T selectedItem = null;

                @Override
                public void handle(ActionEvent event) {
                    if (!disableListener && !Objects.equals(selectedItem, getValue())) {
                        selectedItem = getValue();
                        notifySubscribers();
                    }
                }
            });

            component.getChildren().add(combo);

            button = new Button("...");
//            button.setPadding(new Insets(2, 2, 2, 2));
            button.setOnAction(event -> {
                final SwingFilteredList<T> filteredList = new SwingFilteredList<>(title, items,
                        combo.getSelectionModel().getSelectedItem());
                filteredList.show();
                if (filteredList.isOk()) {
                    setValue(filteredList.getSelectedItem());
                }
            });

            component.getChildren().add(button);

            subscribers.stream().forEach(Subscriber::onStart);

            updateItems();
        }
    }

    @Override
    public void notifySubscribers() {
        for (Subscriber<? super T> subscriber : subscribers) {
            subscriber.onNext(getValue());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (initialized.get()) {
            component.setVisible(visible);
        } else {
            this.visible = visible;
        }
    }

    @Override
    public void setValue(T value) {
        if (value != null) {
//            System.out.println("SwingUIChoice.setValue " + System.identityHashCode(value.getClass()));
//            boolean found = false;
//            for (int i = 0; i < component.getItemCount(); i++) {
//                final T item = component.getItemAt(i);
////                if (item != null) {
////                    System.out.println("SwingUIChoice.setValue item " + System.identityHashCode(item.getClass()));
////                }
//                if (Objects.equals(item, value)) {
//                    found = true;
//                    break;
//                }
//            }

            if (!items.contains(value)) {
                throw new RuntimeException("Cannot find item " + value);
            }
        }
        combo.getSelectionModel().select(value);
        if (!combo.isVisible()) {
            notifySubscribers();
        }
    }

    @Override
    public void setTitle(String label) {
        this.title = label;
    }

}
