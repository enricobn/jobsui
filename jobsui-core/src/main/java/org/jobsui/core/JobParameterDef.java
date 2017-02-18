package org.jobsui.core;

import java.io.Serializable;

/**
 * Created by enrico on 4/29/16.
 */
public interface JobParameterDef<T extends Serializable> extends ParameterDef<T>, ParameterDefUI<T> {
}
