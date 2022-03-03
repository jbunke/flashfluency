package com.redsquare.flashfluency.system;

import com.redsquare.flashfluency.system.exceptions.InvalidDirectoryFormatException;
import org.junit.Test;

import java.io.FileNotFoundException;

public class DirectoryTests {

    @Test
    public void deckFileProducesExpectedFilepath() throws FileNotFoundException, InvalidDirectoryFormatException {
        Settings.loadSettings();
        FFDeckFile d = FFDirectory.createRoot().addChildDirectoryR("proximo").
                addDeckR("seen enough");

        System.out.println(d.getFilepath());
    }

    @Test
    public void directoryProducesExpectedEncoding() throws FileNotFoundException, InvalidDirectoryFormatException {
        Settings.loadSettings();
        FFDirectory d = FFDirectory.createRoot();

        d.addChildDirectoryR("pt").addChildDirectory("verb conj");
        d.addChildDirectory("de");
        d.addChildDirectoryR("en").addChildDirectoryR("vocab").addDeck("phrasal verbs");
        ((FFDirectory) d.getChild("en")).addDeck("countries");

        System.out.println(d.encode());
    }
}
