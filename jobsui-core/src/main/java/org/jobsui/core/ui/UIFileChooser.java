package org.jobsui.core.ui;

public interface UIFileChooser<C> extends UIComponent<C> {

    void setFolder();

    void setFileSave();

    void setFileOpen();

    enum FileChooserType {
        FOLDERS,
        FILE_OPEN,
        FILE_SAVE
    }

    void setType(FileChooserType type);

}
