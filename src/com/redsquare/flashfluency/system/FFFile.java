package com.redsquare.flashfluency.system;

import java.io.File;
import java.util.Comparator;
import java.util.Set;

public abstract class FFFile {
    private final String name;
    private final FFDirectory parent;

    public static final Comparator<FFFile> ALPHABETICAL_COMPARATOR =
            Comparator.comparing(FFFile::getName);

    public static final Comparator<FFFile> COMPLETION_COMPARATOR = new Comparator<>() {
        final Comparator<FFDeckFile> deckComparator =
                Comparator.comparingInt(deck -> deck.getAssociatedDeck().getPercentageScore());

        @Override
        public int compare(FFFile o1, FFFile o2) {
            if (o1 instanceof FFDirectory dir1 && o2 instanceof FFDirectory dir2)
                return ALPHABETICAL_COMPARATOR.compare(dir1, dir2);
            else if (o1 instanceof FFDeckFile deck1 && o2 instanceof FFDeckFile deck2)
                return deckComparator.compare(deck1, deck2);
            else if (o1 instanceof FFDeckFile && o2 instanceof FFDirectory)
                return -1;
            else if (o2 instanceof FFDeckFile && o1 instanceof FFDirectory)
                return 1;

            return 0;
        }
    };

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
