package org.jobsui.ui.javafx.edit;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.fxmisc.richtext.CodeArea;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.ui.*;
import org.jobsui.core.xml.*;
import org.jobsui.ui.javafx.JobsUIFXStyles;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by enrico on 4/28/17.
 */
public class ItemDetail extends VBox {
    public static final String ID_DETAIL_PARAMETER_NAME = "detailParameterName";
    private static final Border CODE_AREA_DARK_FOCUSED_BORDER =
            new Border(new BorderStroke(Paint.valueOf("039ED3"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
    private static final Border CODE_AREA_FOCUSED_BORDER =
            new Border(new BorderStroke(new Color(77d / 256, 102d / 256, 204d / 256, 1), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                    new BorderWidths(2, 2, 2, 2, false, false, false, false)));
    private static final Border CODE_AREA_DARK_NOT_FOCUSED_BORDER =
            new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
    private static final Border CODE_AREA_NOT_FOCUSED_BORDER =
            new Border(new BorderStroke(Paint.valueOf("gray"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
    private static final String ID_DETAIL_PARAMETER_KEY = "detailParameterKey";

    private final UI ui;
    private final JobsUIPreferences preferences;
    private UIComponentRegistry uiComponentRegistry;

    ItemDetail(UI ui) {
        super(0);
        this.ui = ui;
        this.preferences = ui.getPreferences();
    }

    void setSelectedItem(TreeItem<EditItem> treeItem) {
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
                setSimpleParameterDetail(treeItem);
                break;

            case Expression: {
                setExpressionDetail(treeItem);
                break;
            }

            case Call: {
                setCallDetail(treeItem);
                break;
            }

            case Library: {
                setLibraryDetail(treeItem);
                break;
            }

            case WizardStep:
                setWizardStepDetail(treeItem);
                break;
        }
    }

    private void setWizardStepDetail(TreeItem<EditItem> treeItem) {
        WizardStep wizardStep = (WizardStep) treeItem.getValue().payload;
        addTextProperty(treeItem, "Name", wizardStep::getName, wizardStep::setName);
    }

    private boolean setLibraryDetail(TreeItem<EditItem> treeItem) {
        ProjectLibraryXML library = (ProjectLibraryXML) treeItem.getValue().payload;

        ProjectXML projectXML = EditProject.findAncestorPayload(treeItem, ItemType.Project);

        if (projectXML == null) {
            ui.showMessage("Cannot find project for item.");
            return false;
        }

        addTextProperty(treeItem, "Group id", library::getGroupId, library::setGroupId);
        addTextProperty(treeItem, "Artifact id", library::getArtifactId, library::setArtifactId);
        addTextProperty(treeItem, "Version", library::getVersion, library::setVersion);

        return true;
    }

    private void setProjectDetail(TreeItem<EditItem> treeItem) {
        ProjectFSXML project = (ProjectFSXML) treeItem.getValue().payload;
        addTextProperty(treeItem, "Id", project::getId, project::setId);
        addTextProperty(treeItem, "Name", project::getName, project::setName);
        addTextProperty(treeItem, "Version", project::getVersion, project::setVersion);
    }

    private void setJobDetail(TreeItem<EditItem> treeItem) {
        JobXMLImpl jobXML = (JobXMLImpl) treeItem.getValue().payload;

        addTextProperty(treeItem, "Key", jobXML::getId, jobXML::setId);
        addTextProperty(treeItem, "Name", jobXML::getName, jobXML::setName);
        addTextProperty(treeItem, "Version", jobXML::getVersion, jobXML::setVersion);

        addTextAreaProperty(treeItem, "Validate", jobXML::getValidateScript, jobXML::setValidateScript,
                false);
        addTextAreaProperty(treeItem, "Run", jobXML::getRunScript, jobXML::setRunScript, false);
    }

    private void setGroovyFileDetail(TreeItem<EditItem> treeItem) {
        String scriptsRoot = EditProject.findAncestorPayload(treeItem, ItemType.ScriptsLocation);
        Objects.requireNonNull(scriptsRoot);

        ProjectFSXML project = EditProject.findAncestorPayload(treeItem, ItemType.Project);
        Objects.requireNonNull(project);

        String scriptName = (String) treeItem.getValue().payload;

        addTextAreaProperty(treeItem, "Content", () -> project.getScriptContent(scriptsRoot, scriptName),
                content -> project.setScriptContent(scriptsRoot, scriptName, content), true);
    }

    private void setCallDetail(TreeItem<EditItem> treeItem) {
        if (!setParameterDetail(treeItem)) {
            return;
        }
    }

    private void setExpressionDetail(TreeItem<EditItem> treeItem) {
        if (!setParameterDetail(treeItem)) {
            return;
        }

        ExpressionXML parameter = (ExpressionXML) treeItem.getValue().payload;

        addTextAreaProperty(treeItem, "Evaluate", parameter::getEvaluateScript, parameter::setEvaluateScript,
                false);
    }

    private void setSimpleParameterDetail(TreeItem<EditItem> treeItem) {
        if (!setParameterDetail(treeItem)) {
            return;
        }

        SimpleParameterXML parameter = (SimpleParameterXML) treeItem.getValue().payload;

        List<UIComponentType> uiComponentTypes = uiComponentRegistry.getComponentTypes().stream()
                .sorted(Comparator.comparing(UIComponentType::getName))
                .collect(Collectors.toList());

        addCheckboxProperty(treeItem, "Optional", parameter::isOptional, parameter::setOptional);

        addComboProperty(treeItem, "Component", parameter::getComponent, parameter::setComponent,
                uiComponentTypes);

        addTextAreaProperty(treeItem, "On init", parameter::getOnInitScript,
                parameter::setOnInitScript, false);

        addTextAreaProperty(treeItem, "On dependencies change", parameter::getOnDependenciesChangeScript,
                parameter::setOnDependenciesChangeScript, false);

        addTextAreaProperty(treeItem, "Validate", parameter::getValidateScript, parameter::setValidateScript,
                false);
    }

    private boolean setParameterDetail(TreeItem<EditItem> treeItem) {
        if (treeItem == null) {
            ui.showMessage("Cannot find item.");
            return false;
        }

        JobXMLImpl jobXML = EditProject.findAncestorPayload(treeItem, ItemType.Job);

        if (jobXML == null) {
            ui.showMessage("Cannot find job for item.");
            return false;
        }

        ParameterXML parameter = (ParameterXML) treeItem.getValue().payload;

        addTextProperty(treeItem, "Key", parameter::getKey, key -> jobXML.changeParameterKey(parameter, key),
                ID_DETAIL_PARAMETER_KEY);

        addTextProperty(treeItem, "Name", parameter::getName, parameter::setName, ID_DETAIL_PARAMETER_NAME);

        return true;
    }

    private <T extends Serializable> void addComboProperty(TreeItem<EditItem> treeItem, String title, Supplier<T> get, Consumer<T> set, List<T> items) {
        addPropertyNameLabel(title);

        UIChoice<Node> control = ui.createChoice();
        control.setTitle(title);
        control.setItems(items);
        control.setValue(get.get());

        control.getObservable().subscribe(newValue -> {
            set.accept((T)newValue);
            updateSelectedItem(treeItem);
        });
        getChildren().add(control.getComponent());
    }

    private void addTextProperty(TreeItem<EditItem> treeItem, String title, Supplier<String> get, Consumer<String> set) {
        addTextProperty(treeItem, title, get, set, null);
    }

    private void addTextProperty(TreeItem<EditItem> treeItem, String title, Supplier<String> get, Consumer<String> set, String idForTest) {
        addPropertyNameLabel(title);

        UIValue<Node> control = ui.createValue();
        control.setTitle(title);
        control.setValue(get.get());
        if (idForTest != null) {
            control.getComponent().setId(idForTest);
        }

        control.getObservable().subscribe(newValue -> {
            set.accept(Objects.toString(newValue));
            updateSelectedItem(treeItem);
        });
        getChildren().add(control.getComponent());
    }

    private void addPropertyNameLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(JobsUIFXStyles.FIELD_LABEL);
        if (!getChildren().isEmpty()) {
            label.setPadding(new Insets(20, 0, 0, 0));
        }
        getChildren().add(label);
    }

    private void addCheckboxProperty(TreeItem<EditItem> treeItem, String title, Supplier<Boolean> get, Consumer<Boolean> set) {
        addPropertyNameLabel(title);

        UICheckBox<Node> control = ui.createCheckBox();
        control.setTitle(title);
        control.setValue(get.get());

        control.getObservable().subscribe(newValue -> {
            set.accept((Boolean)newValue);
            updateSelectedItem(treeItem);
        });
        getChildren().add(control.getComponent());
    }

    private void updateSelectedItem(TreeItem<EditItem> treeItem) {
        EditProject.validate(treeItem, true);

        EditItem value = treeItem.getValue();
        treeItem.setValue(null);
        treeItem.setValue(value);
        value.setChanged(true);
    }

    private void addTextAreaProperty(TreeItem<EditItem> treeItem, String title, Supplier<String> get, Consumer<String> set,
                                     boolean showLineNumbers) {
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

    void setUiComponentRegistry(UIComponentRegistry uiComponentRegistry) {
        this.uiComponentRegistry = uiComponentRegistry;
    }
}
