package org.bef.core.ui.swing;

import org.bef.core.ui.*;
import org.bef.core.utils.Tuple2;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func2;

import javax.swing.*;
import javax.swing.border.BevelBorder;
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
//    private final Map<JComboBox, Boolean> choices = new HashMap<>();
    private int rows = 0;

    public static void main(String[] args) {
        SwingUIWindow window = new SwingUIWindow("Tetst");

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
        SwingUIChoice<T> choice = new SwingUIChoice<>();
        choice.setItems(items);

        addRow(title, choice.getComponent());

        return choice;
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
        SwingUIValue<T> uiValue = new SwingUIValue<>(converter, defaultValue);

        addRow(title, uiValue.getComponent());

        return uiValue;
    }

    public void addFiller() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridy = rows++;

        // filler
        add(new JLabel(), constraints);
    }

    public void addRow(String label, Component component) {
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = rows;
            gbc.insets.right = 5;
            gbc.insets.left = 5;
            gbc.insets.top = 5;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            final JLabel jlabel = new JLabel(label);
//            jlabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            add(jlabel, gbc);
        }

        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = rows;
            gbc.weightx = 10.0; // why 1.0 does not work ???
            gbc.insets.right = 5;
            gbc.insets.top = 5;
            gbc.insets.left = 5;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(component, gbc);
//            textField.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }

        rows++;

    }

}
