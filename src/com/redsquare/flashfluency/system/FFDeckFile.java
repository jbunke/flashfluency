package com.redsquare.flashfluency.system;

import com.redsquare.flashfluency.cli.ExceptionMessenger;
import com.redsquare.flashfluency.logic.Deck;
import com.redsquare.flashfluency.system.exceptions.FFErrorMessages;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;

import java.io.IOException;
import java.util.Set;

public class FFDeckFile extends FFFile {
    private Deck associatedDeck;

    private FFDeckFile(String name, FFDirectory parent) {
        super(name, parent);
    }

    public static FFDeckFile create(String name, FFDirectory parent) {
        return new FFDeckFile(name, parent);
    }

    @Override
    public boolean moveTo(FFDirectory destination) {
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
            associatedDeck.updateFilepath(getFilepath());

        return setParent(destination);
    }

    public void setAssociatedDeck(Deck deck) {
        associatedDeck = deck;
    }

    public Deck getAssociatedDeck() {
        if (associatedDeck == null)
            setAssociatedDeck(Deck.createNew(getName(), getFilepath()));
        return associatedDeck;
    }

    @Override
    public void getDecksWithMatchingTags(
            final Set<FFDeckFile> hasMatchingTags, final String[] tags
    ) {
        final Set<String> tagsInDeck = associatedDeck.getTags();

        for (String tag : tags)
            if (!tagsInDeck.contains(tag))
                return;

        hasMatchingTags.add(this);
    }

    @Override
    public void getDecksWithDue(final Set<FFDeckFile> hasDue) {
        if (associatedDeck.getNumDueFlashCards() > 0)
            hasDue.add(this);
    }

    @Override
    public String getFileExtension() {
        return Settings.DECK_FILE_EXTENSION;
    }

    @Override
    public String encode(final int depthLevel) {
        try {
            getAssociatedDeck().saveToFile();
        } catch (IOException e) {
            ExceptionMessenger.deliver(FFErrorMessages.MESSAGE_FAILED_WRITE_TO_DECK_FILE,
                    false, FFErrorMessages.CONSEQUENCE_DECK_DATA_NOT_SAVED);
        }
        return super.encode(depthLevel);
    }

    @Override
    public String toString() {
        return getFilepath();
    }
}
