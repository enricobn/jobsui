package org.jobsui.core.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class UIComponentRegistryImpl implements UIComponentRegistry {
    public static UIComponentType Button = new UIComponentTypeAbstract("Button") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createButton();
        }
    };

    public static UIComponentType CheckBox = new UIComponentTypeAbstract("CheckBox") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createCheckBox();
        }
    };

    public static UIComponentType Choice = new UIComponentTypeAbstract("Choice") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createChoice();
        }
    };

    public static UIComponentType List = new UIComponentTypeAbstract("List") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createList();
        }
    };

    public static UIComponentType Password = new UIComponentTypeAbstract("Password") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createPassword();
        }
    };

    public static UIComponentType Value = new UIComponentTypeAbstract("Value") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createValue();
        }
    };

    public static UIComponentType FileChooser = new UIComponentTypeAbstract("FileChooser") {
        @Override
        public <COMP extends UIComponent> COMP create(UI ui) throws UnsupportedComponentException {
            return (COMP) ui.createFileChooser();
        }
    };

    private Collection<UIComponentType> componentTypes = Arrays.asList(Button, CheckBox, Choice, List, Password, Value,
            FileChooser);

    public Optional<UIComponentType> getComponentType(String name) {
        return componentTypes.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst();
    }

    @Override
    public Collection<UIComponentType> getComponentTypes() {
        return componentTypes;
    }

}
