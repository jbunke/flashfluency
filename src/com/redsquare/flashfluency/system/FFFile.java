package com.redsquare.flashfluency.system;

import com.redsquare.flashfluency.cli.ExceptionMessenger;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;

import java.io.File;
import java.util.Comparator;
import java.util.Set;

public abstract class FFFile {
    private final String name;
    private FFDirectory parent;

    private static final Comparator<FFFile> ALPHABETICAL_COMPARATOR =
            Comparator.comparing(FFFile::getName);
    private static final Comparator<FFDeckFile> COMPLETION_COMPARATOR =
            Comparator.comparingInt(deck -> -deck.getAssociatedDeck().getPercentageScore());
    private static final Comparator<FFDeckFile> DUE_COMPARATOR =
            Comparator.comparingInt(deck -> -deck.getAssociatedDeck().getNumDueFlashCards());

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

    public boolean moveTo(final FFDirectory destination) {
        if (destination.equals(getParent())) {
            ExceptionMessenger.deliver(
                    "The file was already in the specified destination.",
                    false,
                    FlashFluencyLogicException.CONSEQUENCE_COMMAND_NOT_EXECUTED
            );
            return false;
        }

        boolean success = setParent(destination);

        if (success)
            updateFileSystem();

        return success;
    }

    public void delete() {
        final String oldFilepath = getFilepath();

        parent.removeChild(name);
        parent = null;

        FileIOHelper.deleteFileFootprintFromSystem(oldFilepath);
    }

    public abstract void updateFileSystem();

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

    public String encode(final int depthLevel) {
        return (depthLevel == 0 ? "" : DirectoryParser.NEW_LINE +
                DirectoryParser.TAB.repeat(depthLevel)) +
                DirectoryParser.NAME_BOUND + name + DirectoryParser.NAME_BOUND;
    }

    public void getDecksWithMatchingTags(
            final Set<FFDeckFile> hasMatchingTags, final String[] tags
    ) {
    }

    public void getDecksWithDue(final Set<FFDeckFile> hasDue) {
    }

    public static Comparator<FFFile> getComparator(final String flag) {
        final String FLAG_COMPLETION = "-c", FLAG_DUE = "-d"; // FLAG_ALPHABETICAL = "-a"

        Comparator<FFDeckFile> deckComparator = switch (flag) {
            case FLAG_COMPLETION -> COMPLETION_COMPARATOR;
            case FLAG_DUE -> DUE_COMPARATOR;
            default -> Comparator.comparing(FFDeckFile::getName);
        };

        return (o1, o2) -> {
            if (o1 instanceof FFDirectory dir1 && o2 instanceof FFDirectory dir2)
                return ALPHABETICAL_COMPARATOR.compare(dir1, dir2);
            else if (o1 instanceof FFDeckFile deck1 && o2 instanceof FFDeckFile deck2)
                return deckComparator.compare(deck1, deck2);
            else if (o1 instanceof FFDeckFile && o2 instanceof FFDirectory)
                return 1;
            else if (o2 instanceof FFDeckFile && o1 instanceof FFDirectory)
                return -1;

            return 0;
        };
    }

    protected boolean setParent(final FFDirectory parent) {
        final FFDirectory insurance = this.parent;

        if (this.parent != null)
            this.parent.removeChild(name);

        this.parent = parent;
        boolean success = parent.addExistingChild(this);

        if (!success && insurance != null) {
            this.parent = insurance;
            insurance.addExistingChild(this);
        }

        return success;
    }

    @Override
    public String toString() {
        return name;
    }
}
