package org.joubsui.textui;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.jobsui.core.CommandLineArguments;
import org.jobsui.core.JobsUIApplication;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.runner.JobUIRunner;
import org.jobsui.core.ui.*;
import org.jobsui.core.xml.ProjectFSXML;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

public class TextUI implements UI<Component>, JobsUIApplication  {
    private final Screen screen;
    private final WindowBasedTextGUI textGUI;
    private final JobsUIPreferences preferences;
    private CommandLineArguments arguments;

    public TextUI(JobsUIPreferences preferences) throws IOException {
        this.preferences = preferences;
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        screen = terminalFactory.createScreen();
        screen.startScreen();
        textGUI = new MultiWindowTextGUI(screen);
    }

    @Override
    public void showMessage(String message) {
        MessageDialog.showMessageDialog(textGUI, "JobsUI", message, MessageDialogButton.OK);
    }

    @Override
    public UIWindow<Component> createWindow(String title) {
        return new TextUIWindow(textGUI);
    }

    @Override
    public void log(String message) {

    }

    @Override
    public void log(String message, Throwable th) {

    }

    @Override
    public UIButton<Component> createButton() {
        return null;
    }

    @Override
    public UICheckBox<Component> createCheckBox() {
        return null;
    }

    @Override
    public UIChoice<Component> createChoice() {
        return null;
    }

    @Override
    public UIList<Component> createList() {
        return null;
    }

    @Override
    public UIPassword<Component> createPassword() {
        return null;
    }

    @Override
    public UIValue<Component> createValue() {
        return null;
    }

    @Override
    public UIFileChooser<Component> createFileChooser() {
        return null;
    }

    @Override
    public void showError(String message, Throwable t) {

    }

    @Override
    public JobsUIApplication start(CommandLineArguments arguments) {
        this.arguments = arguments;
        return this;
    }

    @Override
    public Optional<String> askString(String message) {
        return Optional.empty();
    }

    @Override
    public boolean askOKCancel(String message) {
        return false;
    }

    @Override
    public JobsUIPreferences getPreferences() {
        return preferences;
    }

    @Override
    public UIWidget<Component> createWidget(String title, UIComponent<Component> component) {
        return null;
    }

    @Override
    public void gotoMain() {

    }

    @Override
    public void gotoRun(Project project, Job<Serializable> job) {
        JobUIRunner<Component> runner = new JobUIRunner<>(this);
        try {
            runner.run(project, job);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void gotoNew() {

    }

    @Override
    public void gotoEdit(ProjectFSXML projectXML) {

    }
}
