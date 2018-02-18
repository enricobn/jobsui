package org.jobsui.core.ui;

import java.util.*;

/**
 * The first added is the one which wins in case of name clash.
 */
public class UIComponentRegistryComposite implements UIComponentRegistry{
    private List<UIComponentRegistry> uiComponentRegistries = new ArrayList<>();

    public void add(UIComponentRegistry uiComponentRegistry) {
        uiComponentRegistries.add(uiComponentRegistry);
    }

    @Override
    public Optional<UIComponentType> getComponentType(String name) {
        for (UIComponentRegistry uiComponentRegistry : uiComponentRegistries) {
            Optional<UIComponentType> componentType = uiComponentRegistry.getComponentType(name);
            if (componentType.isPresent()) {
                return componentType;
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<UIComponentType> getComponentTypes() {
        Map<String, UIComponentType> map = new HashMap<>();
        List<UIComponentRegistry> reversedUiComponentRegistries = new ArrayList<>(uiComponentRegistries);
        Collections.reverse(reversedUiComponentRegistries);

        for (UIComponentRegistry registry : reversedUiComponentRegistries) {
            registry.getComponentTypes().forEach(c -> map.put(c.getName(), c));
        }

        return map.values();
    }
}
