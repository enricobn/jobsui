package org.jobsui.ui.swing;

import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
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

    public SwingUIWindow(String title) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.getContentPane().setLayout(new GridBagLayout());
        frame.setSize(300, 500);

        // to center on the screen
        frame.setLocationRelativeTo(null);

        // container
        container = new SwingUIContainer();
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
    }

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
    public void refreshBookmarks(Project project, Job job) {
        // todo
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
