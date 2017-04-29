package org.jobsui.ui.javafx.edit;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.fxmisc.richtext.CodeArea;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.ui.JobsUITheme;
import org.jobsui.core.xml.*;
import org.jobsui.ui.javafx.JavaFXUI;
import org.jobsui.ui.javafx.JobsUIFXStyles;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by enrico on 4/28/17.
 */
class ItemDetail extends VBox {
    private static final Border CODE_AREA_DARK_FOCUSED_BORDER =
            new Border(new BorderStroke(Paint.valueOf("039ED3"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
    private static final Border CODE_AREA_FOCUSED_BORDER =
            new Border(new BorderStroke(new Color(77d / 256, 102d / 256, 204d / 256, 1), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                    new BorderWidths(2, 2, 2, 2, false, false, false, false)));
    private static final Border CODE_AREA_DARK_NOT_FOCUSED_BORDER =
            new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
    private static final Border CODE_AREA_NOT_FOCUSED_BORDER =
            new Border(new BorderStroke(Paint.valueOf("gray"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));

    private final JavaFXUI ui;
    private final JobsUIPreferences preferences;

    public ItemDetail(JavaFXUI ui) {
        super(0);
        this.ui = ui;
        this.preferences = ui.getPreferences();
    }

    public void setSelectedItem(TreeItem<EditItem> treeItem) {
        EditItem item = treeItem.getValue();
        getChildren().clear();

        switch (item.itemType) {
            case Project:
                setProjectDetail(treeItem);
                break;

            case ScriptFile:
                setGroovyFileDetail(treeItem);
                break;

            case Job:
                setJobDetail(treeItem);
                break;

            case Parameter:
                setParameterDetail(treeItem);
                break;

            case Expression: {
                setExpressionDetail(treeItem);
                break;
            }

            case Call: {
                setCallDetail(treeItem);
                break;
            }
        }
    }

    private void setProjectDetail(TreeItem<EditItem> treeItem) {
        ProjectFSXML project = (ProjectFSXML) treeItem.getValue().payload;
        addTextProperty(treeItem, "Name", project::getName, project::setName);
        addTextProperty(treeItem, "Version", project::getVersion, project::setVersion);
    }

    private void setJobDetail(TreeItem<EditItem> treeItem) {
        JobXMLImpl jobXML = (JobXMLImpl) treeItem.getValue().payload;

        // TODO key (file name)
        addTextProperty(treeItem, "Name", jobXML::getName, jobXML::setName);

        addTextAreaProperty(treeItem, "Validate", jobXML::getValidateScript, jobXML::setValidateScript,
                false);
        addTextAreaProperty(treeItem, "Run", jobXML::getRunScript, jobXML::setRunScript, false);
    }

    private void setGroovyFileDetail(TreeItem<EditItem> treeItem) {
        String scriptsRoot = EditProject.findAncestorPayload(treeItem, EditProject.ItemType.Scripts);
        Objects.requireNonNull(scriptsRoot);

        ProjectFSXML project = EditProject.findAncestorPayload(treeItem, EditProject.ItemType.Project);
        Objects.requireNonNull(project);

        String scriptName = (String) treeItem.getValue().payload;

        addTextAreaProperty(treeItem, "Content", () -> project.getScriptContent(scriptsRoot, scriptName),
                content -> project.setScriptContent(scriptsRoot, scriptName, content), true);
    }

    private void setCallDetail(TreeItem<EditItem> treeItem) {
        CallXML parameter = (CallXML) treeItem.getValue().payload;

        addTextProperty(treeItem, "Key", parameter::getKey, parameter::setKey);
        addTextProperty(treeItem, "Name", parameter::getName, parameter::setName);

        // TODO
    }

    private void setExpressionDetail(TreeItem<EditItem> treeItem) {
        ExpressionXML parameter = (ExpressionXML) treeItem.getValue().payload;

        addTextProperty(treeItem, "Key", parameter::getKey, parameter::setKey);
        addTextProperty(treeItem, "Name", parameter::getName, parameter::setName);

        addTextAreaProperty(treeItem, "Evaluate", parameter::getEvaluateScript, parameter::setEvaluateScript,
                false);
    }

    private void setParameterDetail(TreeItem<EditItem> treeItem) {
        if (treeItem == null) {
            ui.showMessage("Cannot find item.");
            return;
        }

        JobXMLImpl jobXML = EditProject.findAncestorPayload(treeItem, EditProject.ItemType.Job);

        if (jobXML == null) {
            ui.showMessage("Cannot find job for item.");
            return;
        }

        SimpleParameterXML parameter = (SimpleParameterXML) treeItem.getValue().payload;

        addTextProperty(treeItem, "Key", parameter::getKey, key -> jobXML.changeParameterKey(parameter, key));

        addTextProperty(treeItem, "Name", parameter::getName, parameter::setName);

        addTextAreaProperty(treeItem, "On init", parameter::getOnInitScript,
                parameter::setOnInitScript, false);

        addTextAreaProperty(treeItem, "On dependencies change", parameter::getOnDependenciesChangeScript,
                parameter::setOnDependenciesChangeScript, false);

        addTextAreaProperty(treeItem, "Validate", parameter::getValidateScript, parameter::setValidateScript,
                false);
    }

    private void addTextProperty(TreeItem<EditItem> treeItem, String title, Supplier<String> get, Consumer<String> set) {
        addPropertyNameLabel(title);

        TextField control = ui.createTextField();
        control.setPromptText(title);
        control.setText(get.get());

        control.textProperty().addListener((observable, oldValue, newValue) -> {
            set.accept(newValue);
            updateSelectedItem(treeItem);
        });
        getChildren().add(control);
    }

    private void addPropertyNameLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(JobsUIFXStyles.EDIT_PROPERTY_NAME_TEXT);
        if (!getChildren().isEmpty()) {
            label.setPadding(new Insets(20, 0, 0, 0));
        }
        getChildren().add(label);
    }

    private void updateSelectedItem(TreeItem<EditItem> treeItem) {
        Object payload = treeItem.getValue().payload;
        if (payload instanceof ValidatingXML) {
            validate(treeItem, (ValidatingXML) payload);
        }

        EditItem value = treeItem.getValue();
        treeItem.setValue(null);
        treeItem.setValue(value);
    }

    private void validate(TreeItem<EditItem> treeItem, ValidatingXML validatingXML) {
        List<String> validate = validatingXML.validate();
        if (!validate.isEmpty()) {
            Label label = new Label("?");
            label.setTextFill(Color.RED);
            label.setTooltip(new Tooltip(String.join(", ", validate)));
            treeItem.setGraphic(label);
        } else {
            treeItem.setGraphic(null);
        }
    }

    private void addTextAreaProperty(TreeItem<EditItem> treeItem, String title, Supplier<String> get, Consumer<String> set, boolean showLineNumbers) {
        addPropertyNameLabel(title);

        VBox parent = new VBox();
        CodeArea codeArea = GroovyCodeArea.getCodeArea(true, preferences.getTheme());

        String content = get.get();
        if (content != null) {
            GroovyCodeArea.setText(codeArea, content);
            GroovyCodeArea.resetCaret(codeArea);
        }

        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            set.accept(newValue);
            updateSelectedItem(treeItem);
        });

        VBox.setVgrow(codeArea, Priority.ALWAYS);

        getChildren().add(parent);

        parent.getChildren().add(codeArea);

        Border focusedBorder;
        Border notFocusedBorder;
        if (preferences.getTheme().equals(JobsUITheme.Dark)) {
            focusedBorder = CODE_AREA_DARK_FOCUSED_BORDER;
            notFocusedBorder = CODE_AREA_DARK_NOT_FOCUSED_BORDER;
        } else {
            focusedBorder = CODE_AREA_FOCUSED_BORDER;
            notFocusedBorder = CODE_AREA_NOT_FOCUSED_BORDER;
        }

        parent.setBorder(notFocusedBorder);

        codeArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                parent.setBorder(focusedBorder);
            } else {
                parent.setBorder(notFocusedBorder);
            }
        });

        VBox.setVgrow(parent, Priority.ALWAYS);
    }


}
