package org.jobsui.core.xml;

import org.jobsui.core.utils.JobsUIUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/11/16.
 */
public class JobXMLImpl implements JobXML {
    private final Map<String, ParameterXML> parameters = new LinkedHashMap<>();
    private final List<WizardStep> wizardSteps = new ArrayList<>();
    private final String id;
    private final String version;
    private String name;
    private String runScript;
    private String validateScript;
    private int order;

    public JobXMLImpl(String id, String name, String version) {
        this.name = name;
        this.id = id;
        this.version = version;
    }

    public void setRunScript(String runScript) {
        this.runScript = XMLUtils.scriptToEditForm(runScript);
    }

    public void setValidateScript(String validateScript) {
        this.validateScript = XMLUtils.scriptToEditForm(validateScript);
    }

    public void add(SimpleParameterXML simpleParameterXML) throws Exception {
        addCheckedParameter(simpleParameterXML);
    }

    public void add(ExpressionXML expressionXML) throws Exception {
        addCheckedParameter(expressionXML);
    }

    public void add(CallXML callXML) throws Exception {
        addCheckedParameter(callXML);
    }

    @Override
    public String getRunScript() {
        return runScript;
    }

    @Override
    public String getValidateScript() {
        return validateScript;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<SimpleParameterXML> getSimpleParameterXMLs() {
        return getParameters(SimpleParameterXML.class);
    }

    @Override
    public List<ExpressionXML> getExpressionXMLs() {
        return getParameters(ExpressionXML.class);
    }

    @Override
    public List<CallXML> getCallXMLs() {
        return getParameters(CallXML.class);
    }

    private <T extends ParameterXML> List<T> getParameters(Class<T> clazz) {
        return parameters.values().stream()
                .filter(clazz::isInstance)
                .map(e -> (T) e)
                .collect(Collectors.toList());
    }

    private void addCheckedParameter(ParameterXML parameterXML) throws Exception {
        if (parameters.containsKey(parameterXML.getKey())) {
            throw new Exception("Duplicate key '" + parameterXML.getKey() + "' for parameter " +
                    "with name '" + parameterXML.getName() + "'.");
        }

        parameters.put(parameterXML.getKey(), parameterXML);
        parameterXML.setOrder(order);
        order += 1_000;
    }

    @Override
    public ParameterXML getParameter(String key) {
        return parameters.get(key);
    }

    @Override
    public ExpressionXML getExpression(String key) {
        return getParameter(key, ExpressionXML.class);
    }

    private <T extends ParameterXML> T getParameter(String key, Class<T> clazz) {
        ParameterXML parameterXML = parameters.get(key);
        if (parameterXML == null) {
            return null;
        } else if (clazz.isAssignableFrom(parameterXML.getClass())) {
            return (T) parameterXML;
        }
        throw new RuntimeException("Key='" + key + "' is a " + parameterXML.getClass().getName() + ".");
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void changeParameterKey(ParameterXML parameterXML, String newKey) {
        String oldKey = parameterXML.getKey();
        parameterXML.setKey(newKey);
        parameters.remove(oldKey);
        parameters.put(newKey, parameterXML);
        parameters.values().stream()
                .filter(parameter -> parameter.removeDependency(oldKey))
                .forEach(parameter -> parameter.addDependency(newKey));
    }

    @Override
    public List<String> validate() {
        List<String> messages = new ArrayList<>(0);
        if (JobsUIUtils.isNullOrEmptyOrSpaces(id)) {
            messages.add("Id is mandatory.");
        }

        if (JobsUIUtils.isNullOrEmptyOrSpaces(name)) {
            messages.add("Name is mandatory.");
        }

        if (JobsUIUtils.isNullOrEmptyOrSpaces(version)) {
            messages.add("Version is mandatory.");
        }

        Set<String> dependencyNames = new HashSet<>();
        Set<String> dependencyKeys = new HashSet<>();

        for (JobDependencyXML jobDependencyXML : getJobDependencyXmls()) {
            List<String> jobDependencyXMLMessages = jobDependencyXML.validate();
            if (!jobDependencyXMLMessages.isEmpty()) {
                messages.add(jobDependencyXML.getName() + ": " + String.join(" ", jobDependencyXMLMessages));
            }
            if (!dependencyKeys.add(jobDependencyXML.getKey())) {
                messages.add("Duplicate dependency key '" + jobDependencyXML.getKey() + "'.");
            }

            if (!dependencyNames.add(jobDependencyXML.getName())) {
                messages.add("Duplicate dependency name '" + jobDependencyXML.getName() + "'.");
            }
        }

        Set<String> wizardStepName = new HashSet<>();

        for (WizardStep wizardStep : getWizardSteps()) {
            List<String> wizardStepMessages = wizardStep.validate();
            if (!wizardStepMessages.isEmpty()) {
                messages.add(wizardStep.getName() + ": " + String.join(" ", wizardStepMessages));
            }

            if (!wizardStepName.add(wizardStep.getName())) {
                messages.add("Duplicate wizard name '" + wizardStep.getName() + "'.");
            }
        }

        if (JobsUIUtils.isNullOrEmptyOrSpaces(runScript)) {
            messages.add("Run script is mandatory.");
        }

        return messages;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(WizardStep wizardStep) {
        wizardSteps.add(wizardStep);
    }

    @Override
    public List<WizardStep> getWizardSteps() {
        return wizardSteps;
    }
}
