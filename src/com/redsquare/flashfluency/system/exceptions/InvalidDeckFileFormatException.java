package com.redsquare.flashfluency.system.exceptions;

public class InvalidDeckFileFormatException extends InvalidFormatException {
    public static String CONSEQUENCE_DECK_FILE_COULD_NOT_BE_PARSED =
            "The deck file could not be parsed and thus the associated deck has been created as an empty deck.";
    public static String CONSEQUENCE_DECK_FILE_NOT_CREATED =
            "The deck file was not created.";

    protected InvalidDeckFileFormatException(String message, boolean fatal, String consequence) {
        super(message, fatal, consequence);
    }

    public static InvalidDeckFileFormatException tooFewLinesInFileForNecessaryParameters(String filepath) {
        return new InvalidDeckFileFormatException("The file " + filepath + " has too few lines " +
                "to contain all of the necessary parameters.",
                false, CONSEQUENCE_DECK_FILE_COULD_NOT_BE_PARSED);
    }

    public static InvalidDeckFileFormatException descriptionImproperlyFormatted(String filepath) {
        return new InvalidDeckFileFormatException(
                "The description is improperly formatted in the file " + filepath,
                false, CONSEQUENCE_DECK_FILE_COULD_NOT_BE_PARSED);
    }

    public static InvalidDeckFileFormatException tagsImproperlyFormatted(String filepath) {
        return new InvalidDeckFileFormatException(
                "The tags are improperly formatted in the file " + filepath,
                false, CONSEQUENCE_DECK_FILE_COULD_NOT_BE_PARSED);
    }

    public static InvalidDeckFileFormatException flashCardsImproperlyFormatted(String filepath) {
        return new InvalidDeckFileFormatException(
                "The flash cards are improperly formatted in the file " + filepath,
                false, CONSEQUENCE_DECK_FILE_COULD_NOT_BE_PARSED);
    }

    public static InvalidDeckFileFormatException invalidNameForDeck(String name) {
        return new InvalidDeckFileFormatException(
                "The name \"" + name + "\" is an invalid deck name as it contains ineligible characters",
                false, CONSEQUENCE_DECK_FILE_NOT_CREATED
        );
    }

    public static InvalidDeckFileFormatException directoryAlreadyContains(String name) {
        return new InvalidDeckFileFormatException(
                "This directory already contains a deck with the name \"" + name + "\"",
                false, CONSEQUENCE_DECK_FILE_NOT_CREATED
        );
    }
}
