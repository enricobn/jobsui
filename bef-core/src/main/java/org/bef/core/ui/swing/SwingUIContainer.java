package org.bef.core.ui.swing;

import org.bef.core.ui.*;
import org.bef.core.utils.Tuple2;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.*;
import java.util.List;

/**
 * Created by enrico on 2/14/16.
 */
public class SwingUIContainer extends JPanel implements UIContainer {
    private final Map<JComboBox, Boolean> choices = new HashMap<>();
    private int rows = 0;

    public static void main(String[] args) {
        SwingUIWindow window = new SwingUIWindow();

        UIContainer container = window.addContainer();

        final UIChoice<String> version = container.addChoice("Version", new String[]{"1.0", "2.0"});

        final UIChoice<String> db = container.addChoice("DB", new String[]{});

        final UIChoice<String> user = container.addChoice("User", new String[]{});

        UIValue<String> name = container.add("Name", new StringConverterString(), null);

        version.getObservable().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println("Version " + s);
                if (s == null) {
                    db.setItems(new String[0]);
                } else if (s.equals("1.0")) {
                    db.setItems(new String[]{"Dev-1.0", "Cons-1.0", "Dev"});
                } else {
                    db.setItems(new String[]{"Dev-2.0", "Cons-2.0", "Dev"});
                }
            }
        });

        db.getObservable().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println("DB " + s);
            }
        });

        user.getObservable().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println("User " + s);
            }
        });

        Observable.combineLatest(version.getObservable(), db.getObservable(), new Func2<String, String, Tuple2<String, String>>() {
            @Override
            public Tuple2<String,String> call(String version, String db) {
                return new Tuple2<>(version, db);
            }
        }).subscribe(new Action1<Tuple2<String, String>>() {
            @Override
            public void call(Tuple2<String, String> versionDB) {
                if (versionDB.first == null || versionDB.second == null) {
                    user.setItems(new String[0]);
                } else {
                    user.setItems(new String[]{versionDB.toString()});
                }
            }
        });


        UIContainer listContainer = window.addContainer();
        final UIList<String> list = listContainer.addList(Arrays.asList("First", "Second"), true);

        UIContainer buttonsContainer = window.addContainer();

        buttonsContainer.addButton("Add").getObservable().subscribe(new Action1<Void>() {
            @Override
            public void call(Void o) {
                list.addItem("Other");
            }
        });

        System.out.println("OK = " + window.show());
        System.out.println("items = " + list.getItems());
//        System.exit(0);
    }

    public SwingUIContainer() {
        setLayout(new GridBagLayout());
    }

    @Override
    public <T> UIChoice<T> addChoice(String title, final T[] items) {
        final JComboBox<T> choice = new JComboBox<>();
        choices.put(choice, true);

        setItems(choice, items);

        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = rows;
            gbc.insets.right = 5;
            gbc.insets.top = 5;
            gbc.anchor = GridBagConstraints.EAST;
            add(new JLabel(title), gbc);
        }

        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets.top = 5;
            gbc.gridy = rows;
            gbc.anchor = GridBagConstraints.WEST;
            add(choice, gbc);
        }

        rows++;

        final Observable<T> observable = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                subscriber.onStart();
                choice.addActionListener(new ActionListener() {
                    private T selectedItem = null;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!choices.get(choice)) {
                            return;
                        }
                        if (!Objects.equals(selectedItem, choice.getSelectedItem())) {
                            selectedItem = SwingUIContainer.getSelectedItem(choice);
                            subscriber.onNext(selectedItem);
//                        subscriber.onCompleted();
                        }
                    }
                });
            }
        });

        return new UIChoice<T>() {
            @Override
            public Observable<T> getObservable() {
                return observable;
            }

            @Override
            public void setEnabled(boolean enable) {
                choice.setEnabled(enable);
            }

            @Override
            public T getSelectedItem() {
                return SwingUIContainer.getSelectedItem(choice);
            }

            @Override
            public void setItems(T[] items) {
                SwingUIContainer.this.setItems(choice, items);
            }
        };
    }

    private static <T> T getSelectedItem(JComboBox<T> choice) {
        int selectedIndex = choice.getSelectedIndex();
        if (selectedIndex < 0) {
            return null;
        } else {
            return choice.getItemAt(selectedIndex);
        }
    }

    private <T> void setItems(final JComboBox<T> choice, final T[] items) {
        final Object selectedItem = choice.getSelectedItem();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                choices.put(choice, false);

                choice.removeAllItems();

                if (items.length > 1) {
                    choice.addItem(null);
                }

                boolean found = false;
                for (T v : items) {
                    choice.addItem(v);
                    if (Objects.equals(selectedItem, v)) {
                        found = true;
                    }
                }

                if (found) {
                    choice.setSelectedItem(selectedItem);
                    choices.put(choice, true);
                } else {
                    choices.put(choice, true);
                    if (items.length == 1) {
                        choice.setSelectedItem(items[0]);
                    } else {
                        choice.setSelectedItem(null);
                    }
                }
            }
        });
    }

    @Override
    public UIButton addButton(String title) {
        SwingUIButton button = new SwingUIButton();
        button.setText(title);

        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = rows;
            add(button, gbc);
        }

        rows++;

        return button;
    }

    @Override
    public <T> UIList<T> addList(List<T> items, boolean allowRemove) {
        SwingUIList<T> list = new SwingUIList<>(items, allowRemove);

        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = rows;
            add(list, gbc);
        }

        rows++;

        return list;
    }

    @Override
    public <T> UIValue<T> add(final String title, final StringConverter<T> converter, final T defaultValue) {
        final JTextField textField = new JTextField();

        if (defaultValue != null) {
            textField.setText(converter.toString(defaultValue));
        }

        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = rows;
            gbc.insets.right = 5;
            gbc.insets.top = 5;
            gbc.anchor = GridBagConstraints.EAST;
            add(new JLabel(title), gbc);
        }

        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridy = rows;
            gbc.weightx = 1.0;
            gbc.insets.right = 5;
            gbc.insets.top = 5;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(textField, gbc);
        }

        rows++;

        final Observable<T> observable = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                subscriber.onStart();
                textField.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {

                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        T value = converter.fromString(textField.getText());
                        subscriber.onNext(value);
                    }
                });
                subscriber.onNext(defaultValue);
            }
        });

        return new UIValue<T>() {
            @Override
            public Observable<T> getObservable() {
                return observable;
            }

            @Override
            public T getValue() {
                return converter.fromString(textField.getText());
            }

            @Override
            public String getName() {
                return title;
            }
        };
    }

}
