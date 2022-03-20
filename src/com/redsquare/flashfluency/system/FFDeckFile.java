package com.redsquare.flashfluency.system;

import com.redsquare.flashfluency.cli.ExceptionMessenger;
import com.redsquare.flashfluency.logic.Deck;
import com.redsquare.flashfluency.system.exceptions.FFErrorMessages;

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

    public void setAssociatedDeck(Deck deck) {
        associatedDeck = deck;
    }

    public Deck getAssociatedDeck() {
        if (associatedDeck == null)
            setAssociatedDeck(Deck.createNew(getName(), getFilepath()));
        return associatedDeck;
    }

    @Override
    public void getDecksWithDue(Set<FFDeckFile> hasDue) {
        if (associatedDeck.getNumDueFlashCards() > 0)
            hasDue.add(this);
    }

    @Override
    public String getFileExtension() {
        return Settings.DECK_FILE_EXTENSION;
    }

    @Override
    public String encode() {
        try {
            getAssociatedDeck().saveToFile();
        } catch (IOException e) {
            ExceptionMessenger.deliver(FFErrorMessages.MESSAGE_FAILED_WRITE_TO_DECK_FILE,
                    false, FFErrorMessages.CONSEQUENCE_DECK_DATA_NOT_SAVED);
        }
        return super.encode();
    }

    @Override
    public String toString() {
        return getFilepath();
    }
}
