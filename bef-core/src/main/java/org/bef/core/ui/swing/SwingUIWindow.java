package org.bef.core.ui.swing;

import org.bef.core.ui.*;
import org.bef.core.utils.Tuple2;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by enrico on 2/14/16.
 */
public class SwingUIWindow implements UIWindow<JComponent> {
    private final JFrame frame;
    private final JButton okButton = new JButton("OK");
    private final SwingUIContainer container;
    private boolean ok;

    private static <T> UIWindow<T> createWindow(UI<T> ui) throws UnsupportedComponentException {
        UIWindow<T> window = ui.createWindow("Test");

        final UIChoice<String,T> version = ui.create(UIChoice.class);
        version.setItems(Arrays.asList("1.0", "2.0"));
        window.add("Version", version);

        final UIChoice<String,T> db = ui.create(UIChoice.class);
        window.add("DB", db);

        final UIChoice<String,T> user = ui.create(UIChoice.class);
        window.add("User", user);

        UIValue<String,T> name = ui.create(UIValue.class);
        name.setConverter(new StringConverterString());
        name.setDefaultValue("hello");
        window.add("Name", name);

        version.getObservable().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println("Version " + s);
                if (s == null) {
                    db.setItems(Collections.<String>emptyList());
                } else if (s.equals("1.0")) {
                    db.setItems(Arrays.asList("Dev-1.0", "Cons-1.0", "Dev"));
                } else {
                    db.setItems(Arrays.asList("Dev-2.0", "Cons-2.0", "Dev"));
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
                    user.setItems(Collections.<String>emptyList());
                } else {
                    user.setItems(Arrays.asList(versionDB.toString()));
                }
            }
        });


        final UIList<String,T> list = ui.create(UIList.class);
        list.setItems(Arrays.asList("First", "Second"));
        window.add("Datasources", list);

        UIButton<T> button = ui.create(UIButton.class);
        button.setText("Add");
        window.add(button);

        button.getObservable().subscribe(new Action1<Void>() {
            @Override
            public void call(Void o) {
                list.addItem("Other");
            }
        });
        return window;
    }

    public static void main(String[] args) throws Exception {
        UI<?> ui = new SwingUI();
        UIWindow window = createWindow(ui);

        System.out.println("OK = " + window.show());
//        System.exit(0);
    }

    public SwingUIWindow(String title) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container contentPane = frame.getContentPane();

        contentPane.setLayout(new GridBagLayout());
        frame.setSize(300, 500);

        // to center on the screen
        frame.setLocationRelativeTo(null);

        // container
        container = new SwingUIContainer();

        {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
//            containersPanel.setLayout(new GridBagLayout());
            contentPane.add(container.getComponent(), constraints);
        }

        // buttons

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());

        {
            GridBagConstraints constraints;
            constraints = new GridBagConstraints();
            constraints.weightx = 1.0;
            constraints.gridy = 1;
            constraints.anchor = GridBagConstraints.SOUTH;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            contentPane.add(buttonsPanel, constraints);
        }

//
//        // filler
//        {
//            GridBagConstraints constraints;
//            constraints = new GridBagConstraints();
//            constraints.fill = GridBagConstraints.HORIZONTAL;
//            constraints.weightx = 1.0;
//            buttonsPanel.add(new JPanel(), constraints);
//        }

        // OK
        {
            GridBagConstraints constraints;
            constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = 1;
            constraints.insets.top = 5;
            constraints.insets.bottom = 5;
            constraints.insets.right = 5;

            buttonsPanel.add(okButton, constraints);
        }

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                frame.dispose();
                ok = true;
            }
        });

        // Cancel
        JButton cancelButton = new JButton("Cancel");
        {
            GridBagConstraints constraints;
            constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = 2;
            constraints.insets.top = 5;
            constraints.insets.bottom = 5;
            constraints.insets.right = 5;

            buttonsPanel.add(cancelButton, constraints);
        }

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                frame.dispose();
                ok = false;
            }
        });
    }


    @Override
    public boolean show() {
        container.addFiller();

        frame.setVisible(true);
        while (frame.isVisible()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return ok;
    }

    @Override
    public void setValid(boolean valid) {
        okButton.setEnabled(valid);
    }

    @Override
    public <T> void add(String title, UIComponent<T, JComponent> component) {
        container.add(title, component);
    }

    @Override
    public <T> void add(UIComponent<T, JComponent> component) {
        container.add(component);
    }

    @Override
    public void add(UIContainer<JComponent> container) {
        this.container.add(container);
    }

    @Override
    public JComponent getComponent() {
        return container.getComponent();
    }
}
