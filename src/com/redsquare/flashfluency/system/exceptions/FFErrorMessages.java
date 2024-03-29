package com.redsquare.flashfluency.system.exceptions;

public class FFErrorMessages {
    public static final String MESSAGE_FAILED_WRITE_TO_DECK_FILE =
            "Failed to write deck data to the associated deck file.";
    public static final String MESSAGE_FAILED_TO_READ_FROM_SETTINGS =
            "Failed to read from settings.txt file";
    public static final String MESSAGE_FAILED_TO_WRITE_TO_SETTINGS =
            "Failed to write to settings.txt file";
    public static final String MESSAGE_FAILED_TO_READ_FROM_DIR_MIRROR =
            "Failed to read from directory_mirror.txt file";
    public static final String MESSAGE_FAILED_TO_WRITE_TO_DIR_MIRROR =
            "Failed to write to directory_mirror.txt file";

    public static final String CONSEQUENCE_DECK_DATA_NOT_SAVED =
            "Deck data has not been saved.";
    public static final String CONSEQUENCE_SETTINGS_NOT_SAVED =
            "Settings and directory changes have not been saved.";
    public static final String CONSEQUENCE_COULD_NOT_SAVE =
            "The program could not save its state.";
}
