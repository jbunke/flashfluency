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
    public void getDecksWithMatchingTags(
            final Set<FFDeckFile> hasMatchingTags, final String[] tags
    ) {
        for (String childName : children.keySet())
            children.get(childName).getDecksWithMatchingTags(hasMatchingTags, tags);
    }

    @Override
    public void getDecksWithDue(final Set<FFDeckFile> hasDue) {
        for (String childName : children.keySet())
            children.get(childName).getDecksWithDue(hasDue);
    }

    @Override
    public String encode(final int depthLevel) {
        StringBuilder sb = new StringBuilder(super.encode(depthLevel) +
                DirectoryParser.DIR_MARKER + DirectoryParser.SCOPE_OPENER);

        Set<String> childrenNames = children.keySet();
        int i = 0;

        for (String childName : childrenNames) {
            FFFile child = children.get(childName);
            sb.append(child.encode(depthLevel + 1));
            i++;

            if (i < childrenNames.size())
                sb.append(DirectoryParser.SEPARATOR);
        }

        if (!childrenNames.isEmpty()) {
            sb.append(DirectoryParser.NEW_LINE);
            sb.append(DirectoryParser.TAB.repeat(depthLevel));
        }

        sb.append(DirectoryParser.SCOPE_CLOSER);
        return sb.toString();
    }

    @Override
    public String toString() {
        return encode(0) + "\n" + getFilepath();
    }
}
