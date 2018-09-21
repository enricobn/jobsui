package org.joubsui.textui;

import com.googlecode.lanterna.gui2.*;
import org.jobsui.core.bookmark.Bookmark;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.ui.UIButton;
import org.jobsui.core.ui.UIContainer;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.ui.UIWindow;

import java.util.function.Consumer;

public class TextUIWindow implements UIWindow<Component> {
    private final WindowBasedTextGUI textGUI;
    private final BasicWindow window;
    private final Panel contentPanel = new Panel(new LinearLayout(Direction.VERTICAL));

    public TextUIWindow(WindowBasedTextGUI textGUI) {
        this.textGUI = textGUI;
        window = new BasicWindow("");
        window.setComponent(contentPanel);
    }

    @Override
    public void show(Project project, Job job, Runnable callback) {
        callback.run();
        textGUI.addWindowAndWait(window);
    }

    @Override
    public void showValidationMessage(String message) {
        // TODO
    }

    @Override
    public void addButton(UIButton<Component> button) {
        contentPanel.addComponent(button.getComponent());
    }

    @Override
    public void setOnOpenBookmark(Consumer<Bookmark> consumer) {

    }

    @Override
    public void setOnDeleteBookmark(Consumer<Bookmark> consumer) {

    }

    @Override
    public void refreshBookmarks(Project project, Job job, Bookmark activeBookmark) {

    }

    @Override
    public void setTitle(String title) {
        window.setTitle(title);
    }

    @Override
    public void add(UIWidget<Component> widget) {
        contentPanel.addComponent(widget.getComponent().getComponent());
    }

    @Override
    public void add(UIContainer<Component> container) {
        //TODO
    }

    @Override
    public Component getComponent() {
        return contentPanel;
    }

    @Override
    public void clear() {
        contentPanel.removeAllComponents();
    }
}
