package org.jobsui.core.ui.swing;

import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.job.Project;
import org.jobsui.core.job.Job;
import org.jobsui.core.ui.*;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Created by enrico on 2/14/16.
 */
public class SwingUIWindow implements UIWindow<JComponent> {
    private final JFrame frame;
    private final SwingUIContainer container;
//    private final OKCancelHandler okCancelHandler;

//    // TODO remove or put in another class
//    private static <T extends Serializable> UIWindow<T> createWindow(UI<T> ui) throws UnsupportedComponentException {
//        UIWindow<T> window = ui.createWindow("Test");
//
//        final UIChoice<String,T> version = ui.create(UIChoice.class);
//        version.setItems(Arrays.asList("1.0", "2.0"));
//        window.add("Version", version);
//
//        final UIChoice<String,T> db = ui.create(UIChoice.class);
//        window.add("DB", db);
//
//        final UIChoice<String,T> user = ui.create(UIChoice.class);
//        window.add("User", user);
//
//        UIValue<String,T> name = ui.create(UIValue.class);
//        name.setConverter(new StringConverterString());
//        name.setDefaultValue("hello");
//        window.add("Name", name);
//
//        version.getObservable().subscribe(s -> {
//            System.out.println("Version " + s);
//            if (s == null) {
//                db.setItems(Collections.emptyList());
//            } else if (s.equals("1.0")) {
//                db.setItems(Arrays.asList("Dev-1.0", "Cons-1.0", "Dev"));
//            } else {
//                db.setItems(Arrays.asList("Dev-2.0", "Cons-2.0", "Dev"));
//            }
//        });
//
//        db.getObservable().subscribe(s -> {
//            System.out.println("DB " + s);
//        });
//
//        user.getObservable().subscribe(s -> {
//            System.out.println("User " + s);
//        });
//
//        Observable.combineLatest(version.getObservable(), db.getObservable(), (version1, db1) -> new Tuple2<>(version1, db1)).subscribe(versionDB -> {
//            if (versionDB.first == null || versionDB.second == null) {
//                user.setItems(Collections.emptyList());
//            } else {
//                user.setItems(Collections.singletonList(versionDB.toString()));
//            }
//        });
//
//
//        final UIList<String,T> list = ui.create(UIList.class);
//        list.setValue(Arrays.asList("First", "Second"));
//        window.add("Datasources", list);
//
//        UIButton<T> button = ui.create(UIButton.class);
//        button.setTitle("Add");
//        window.add(button);
//
//        button.getObservable().subscribe(o -> {
//            list.addItem("Other");
//        });
//        return window;
//    }
//
//    public static void main(String[] args) throws Exception {
//        UI<?> ui = new SwingUI();
//        UIWindow window = createWindow(ui);
//
//        System.out.println("OK = " + window.show(() -> {}));
////        System.exit(0);
//    }

    public SwingUIWindow(String title) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.getContentPane().setLayout(new GridBagLayout());
        frame.setSize(300, 500);

        // to center on the screen
        frame.setLocationRelativeTo(null);

        // container
        container = new SwingUIContainer();

//        okCancelHandler = new OKCancelHandler(frame, (JComponent) frame.getContentPane(), container.getComponent());
    }


    @Override
    public void show(Project project, Job job, Runnable callback) {
        container.addFiller();

        callback.run();

        frame.setVisible(true);
        while (frame.isVisible()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        return okCancelHandler.isOk();
    }

//    @Override
//    public void setValid(boolean valid) {
//        okCancelHandler.setOKButtonEnabled(valid);
//    }

    @Override
    public void showValidationMessage(String message) {
        new SwingUI().showMessage(message);
    }

    @Override
    public void addButton(UIButton<JComponent> button) {
        // TODO
        add(button);
    }

    @Override
    public void setOnOpenBookmark(Consumer<Bookmark> onOpenBookmark) {
        // TODO
    }

    @Override
    public UIWidget<JComponent> add(String title, UIComponent<JComponent> component) {
        return container.add(title, component);
    }

    @Override
    public UIWidget<JComponent> add(UIComponent<JComponent> component) {
        return container.add(component);
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
