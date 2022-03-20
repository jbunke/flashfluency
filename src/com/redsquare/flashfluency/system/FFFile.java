package com.redsquare.flashfluency.system;

import java.io.File;
import java.util.Set;

public abstract class FFFile {
    private final String name;
    private final FFDirectory parent;

    protected FFFile(String name, FFDirectory parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public FFDirectory getParent() {
        return parent;
    }

    public String getFilepath() {
        if (name.equals(Settings.ROOT_CODE))
            return Settings.getRootFilepath();
        else
            return parent.getFilepath() + File.separator +
                    name + getFileExtension();
    }

    public String getFileExtension() {
        return "";
    }

    public String encode() {
        return DirectoryParser.NAME_BOUND + name + DirectoryParser.NAME_BOUND;
    }

    public void getDecksWithDue(Set<FFDeckFile> hasDue) {
    }

    @Override
    public String toString() {
        return name;
    }
}
