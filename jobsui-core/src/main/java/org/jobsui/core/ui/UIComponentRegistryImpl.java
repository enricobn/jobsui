package org.jobsui.core.ui;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UIComponentRegistryImpl implements UIComponentRegistry {

    public static final UIComponentType Button = new UIComponentTypeAbstract("Button") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createButton();
        }
    };

    public static final UIComponentType CheckBox = new UIComponentTypeAbstract("CheckBox") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createCheckBox();
        }
    };

    public static final UIComponentType Choice = new UIComponentTypeAbstract("Choice") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createChoice();
        }
    };

    public static final UIComponentType List = new UIComponentTypeAbstract("List") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createList();
        }
    };

    public static final UIComponentType Password = new UIComponentTypeAbstract("Password") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createPassword();
        }
    };

    public static final UIComponentType Value = new UIComponentTypeAbstract("Value") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createValue();
        }
    };

    public static final UIComponentType FileChooser = new UIComponentTypeAbstract("FileChooser") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createFileChooser();
        }
    };

    private final Map<String,UIComponentType> componentTypes = Stream.of(Button, CheckBox, Choice, List, Password, Value,
            FileChooser).collect(Collectors.toMap(UIComponentType::getName, it -> it));

    public Optional<UIComponentType> getComponentType(String name) {
        return Optional.ofNullable(componentTypes.get(name));
    }

    @Override
    public Collection<UIComponentType> getComponentTypes() {
        return componentTypes.values();
    }

}
