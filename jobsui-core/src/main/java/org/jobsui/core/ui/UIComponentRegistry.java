package org.jobsui.core.ui;

import java.util.Collection;
import java.util.Optional;

public interface UIComponentRegistry {

    Optional<UIComponentType> getComponentType(String name);

    Collection<UIComponentType> getComponentTypes();

}
