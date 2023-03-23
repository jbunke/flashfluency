package com.redsquare.flashfluency.system.exceptions;

public class FlashFluencyLogicException extends FlashFluencyException {
    public static final String CONSEQUENCE_COMMAND_NOT_EXECUTED =
            "The command was not executed.";

    private FlashFluencyLogicException(String message, boolean fatal, String consequence) {
        super(message, fatal, consequence);
    }

    public static FlashFluencyLogicException numberOfUpdatedSettingsDoesNotMatchExpected(
            final int settings, final int values, final boolean isSR
    ) {
        return new FlashFluencyLogicException(
                "The number of updated settings (" + settings +
                        ") does not match the expected amount (" + values + ".",
                false, "No settings were updated and the " +
                (isSR ? "lesson" : "test") + " was aborted."
        );
    }

    public static FlashFluencyLogicException directoryAlreadyHasChildOfThisName(
            final String name
    ) {
        return new FlashFluencyLogicException(
                "This directory already has a child of the name \"" +
                        name +"\".", false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException invalidArgumentName() {
        return new FlashFluencyLogicException(
                "The command contained an invalid argument.",
                false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException invalidNumberOfArguments() {
        return new FlashFluencyLogicException(
                "The command contains an invalid number of arguments.",
                false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException questionHasAlreadyBeenAnswered() {
        return new FlashFluencyLogicException(
                "The question has already been answered.", false, CONSEQUENCE_QUESTION_ALREADY_ANSWERED
        );
    }

    public static FlashFluencyLogicException fileDoesNotExistInDir(final String name) {
        return new FlashFluencyLogicException(
                "The deck or subdirectory \"" + name + "\" does not exist in this directory.",
                false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException fileWithSegmentTypeDoesNotExistInDir(
            final boolean isPrefix, final String name
    ) {
        final String message = "No deck or subdirectory " +
                (isPrefix ? "starting" : "ending") +
                " with \"" + name + "\" exists in this directory.";

        return new FlashFluencyLogicException(
                message, false, CONSEQUENCE_COMMAND_NOT_EXECUTED);
    }

    public static FlashFluencyLogicException multipleMatchesForSegmentType(
            final boolean isPrefix, final String name
    ) {
        final String message = "There are multiple decks and/or subdirectories " +
                (isPrefix ? "starting" : "ending") +
                " with \"" + name + "\" in this directory.";

        return new FlashFluencyLogicException(
                message, false, CONSEQUENCE_COMMAND_NOT_EXECUTED);
    }

    public static FlashFluencyLogicException contextIsNotDeckFile() {
        return new FlashFluencyLogicException(
                "This context is not a deck file.", false,
                CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException deckFilesHaveNoChildren() {
        return new FlashFluencyLogicException(
                "This context is a deck file, not a directory; deck files have no children.",
                false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException manipulateRootDirectory() {
        return new FlashFluencyLogicException(
                "The current context is the root directory; this action cannot be performed here.",
                false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException attemptedToAddExistingTagToDeck(
            final String tag) {
        return new FlashFluencyLogicException(
                "This deck is already labelled with the tag \"" + tag + "\".", false,
                CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException attemptedToRemoveTagNotInDeck(
            final String tag) {
        return new FlashFluencyLogicException(
                "This deck is not labelled with the tag \"" + tag +
                        "\" that you are trying to remove.", false,
                CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException attemptedToAddFlashCardWithDuplicateClue(
            final String clue) {
        return new FlashFluencyLogicException(
                "This deck is already has a flash card with the clue \"" + clue + "\".",
                false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException attemptedToRemoveFlashCardNotInDeck() {
        return new FlashFluencyLogicException(
                "This deck does not contain the flash card you are trying to remove.",
                false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException noFlashCardMatchingCode(final String code) {
        return new FlashFluencyLogicException(
                "This deck does not contain a flash card with the ID code \"" +
                        code + "\".", false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }
}
