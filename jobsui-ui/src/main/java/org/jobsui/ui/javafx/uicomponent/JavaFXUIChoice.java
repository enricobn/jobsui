package org.jobsui.ui.javafx.uicomponent;

import com.jfoenix.controls.JFXComboBox;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import org.jobsui.core.ui.JobsUITheme;
import org.jobsui.core.ui.UIChoice;
import org.jobsui.ui.javafx.AutocompleteComboBox;
import org.jobsui.ui.javafx.JavaFXUI;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by enrico on 10/7/16.
 */
public class JavaFXUIChoice implements UIChoice<Node> {
//    private final FlowPane component = new FlowPane();
    private final Node component;
    private final ComboBox<Serializable> combo;
//    private final Button button = new Button("...");
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();
    private final Observable<Serializable> observable;
    private final JavaFXUI ui;
    //    private String title;
    private boolean disableListener = false;

    public JavaFXUIChoice(JavaFXUI ui) {
        this.ui = ui;
        combo = createComboBox();
        component = combo;
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            subscribers.add(subscriber);
        });
        initialize();
    }

    @Override
    public Observable<Serializable> getObservable() {
        return observable;
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setDisable(!enable);
    }

    @Override
    public Serializable getValue() {
        return AutocompleteComboBox.getComboBoxValue(combo);
    }

    @Override
    public void setItems(final List<? extends Serializable> items) {
        final Serializable selectedItem = combo.getSelectionModel().getSelectedItem();

        disableListener = true;
        combo.getSelectionModel().clearSelection();
        combo.getItems().clear();
        disableListener = false;

//        if (items.size() > 1) {
//            combo.getItems().add(null);
//        }

        boolean found = false;
        for (Serializable v : items) {
            combo.getItems().add(v);
            if (Objects.equals(selectedItem, v)) {
                found = true;
            }
        }

        if (found) {
            combo.getSelectionModel().select(selectedItem);
            combo.setDisable(false);
//            button.setDisable(false);
        } else {
            if (items.size() == 1) {
                combo.getSelectionModel().select(items.get(0));
                combo.setDisable(true);
//                button.setDisable(true);
            } else {
                // when no items are set, I want to be sure that subscribers are notified,
                // even if the value was already null, but I want to do it only once
                // so I disable listener
                disableListener = true;
                combo.getSelectionModel().select(null);
                disableListener = false;
                notifySubscribers();
                combo.setDisable(false);
//                button.setDisable(false);
            }
        }
    }

    @Override
    public Node getComponent() {
        return component;
    }

    private void initialize() {
//        component.setHgap(5);

        combo.setOnAction(new EventHandler<ActionEvent>() {
            private Serializable selectedItem = null;

            @Override
            public void handle(ActionEvent event) {
                if (!disableListener && !Objects.equals(selectedItem, getValue())) {
                    selectedItem = getValue();
                    notifySubscribers();
                }
            }
        });

//        component.getChildren().add(combo);

//        button.setOnAction(event -> {
//            final SwingFilteredList<Serializable> filteredList = new SwingFilteredList<>(title, combo.getItems(),
//                    combo.getSelectionModel().getSelectedItem());
//            filteredList.show();
//            if (filteredList.isOk()) {
//                setValue(filteredList.getSelectedItem());
//            }
//        });
//
//        component.getChildren().add(button);
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
        if (value != null) {
            if (!combo.getItems().contains(value)) {
                throw new RuntimeException("Cannot find item " + value);
            }
        }

        if (value == null) {
            combo.getSelectionModel().clearSelection();
        } else {
            combo.getSelectionModel().select(value);
        }

        // if getScene() == null (the component has not been added to ui, for example in wizard)
        // then the change is not automatically notified
        if (!combo.isVisible() || combo.getScene() == null) {
            notifySubscribers();
        }
    }

    @Override
    public void setTitle(String label) {
//        this.title = label;
    }

    private <T> ComboBox<T> createComboBox() {
        ComboBox<T> result;
        if (ui.getPreferences().getTheme() == JobsUITheme.Material) {
            result = new JFXComboBox<>();
        } else {
            result = new ComboBox<>();
        }

        AutocompleteComboBox.autoCompleteComboBoxPlus(result, JavaFXUIChoice::compareLowerCase);

        return result;
    }

    private static <T> boolean compareLowerCase(String typedText, T itemToCompare) {
        return itemToCompare != null && itemToCompare.toString().toLowerCase().contains(typedText.toLowerCase());
    }

}
