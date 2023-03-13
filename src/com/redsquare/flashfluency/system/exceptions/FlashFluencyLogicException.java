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
                "The file or directory \"" + name + "\" does not exist in this directory.",
                false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException contextIsNotDeckFile() {
        return new FlashFluencyLogicException(
                "This context is not a deck file.", false,
                CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException deckFilesHaveNoChildren() {
        return new FlashFluencyLogicException(
                "This context is a deck file; deck files have no children.",
                false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }

    public static FlashFluencyLogicException cantGoBackFromRootDirectory() {
        return new FlashFluencyLogicException(
                "The current context is the root directory; the scope of this program is limited to this folder.",
                false, CONSEQUENCE_COMMAND_NOT_EXECUTED
        );
    }
}
