package org.jobsui.core;

import java.io.Serializable;

/**
 * Created by enrico on 2/18/17.
 */
public class SerializableVoid implements Serializable {
    public static Serializable VOID = new SerializableVoid();

    private SerializableVoid() {

    }
}
