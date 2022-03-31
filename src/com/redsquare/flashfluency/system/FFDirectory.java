package com.redsquare.flashfluency.system;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FFDirectory extends FFFile {
    private final Map<String, FFFile> children;

    protected FFDirectory(String name, FFDirectory parent) {
        super(name, parent);

        children = new HashMap<>();
    }

    public static FFDirectory create(String name, FFDirectory parent) {
        return new FFDirectory(name, parent);
    }

    public static FFDirectory createRoot() {
        return create(Settings.ROOT_CODE, null);
    }

    public void addDeck(String name) {
        children.put(name, FFDeckFile.create(name, this));
    }

    public FFDeckFile addDeckR(String name) {
        addDeck(name);
        return (FFDeckFile) getChild(name);
    }

    public void addChildDirectory(String name) {
        children.put(name, FFDirectory.create(name, this));
    }

    public FFDirectory addChildDirectoryR(String name) {
        addChildDirectory(name);
        return (FFDirectory) getChild(name);
    }

    public boolean hasChild(String name) {
        return children.containsKey(name);
    }

    public FFFile getChild(String name) {
        return children.get(name);
    }

    public Set<String> getChildrenNames() {
        return children.keySet();
    }

    @Override
    public void getDecksWithDue(Set<FFDeckFile> hasDue) {
        for (String childName : children.keySet())
            children.get(childName).getDecksWithDue(hasDue);
    }

    @Override
    public String encode() {
        StringBuilder sb = new StringBuilder(super.encode() +
                DirectoryParser.DIR_MARKER + DirectoryParser.SCOPE_OPENER);

        Set<String> childrenNames = children.keySet();
        int i = 0;

        for (String childName : childrenNames) {
            FFFile child = children.get(childName);
            sb.append(child.encode());
            i++;

            if (i < childrenNames.size())
                sb.append(DirectoryParser.SEPARATOR);
        }

        sb.append(DirectoryParser.SCOPE_CLOSER);
        return sb.toString();
    }

    @Override
    public String toString() {
        return encode() + "\n" + getFilepath();
    }
}
