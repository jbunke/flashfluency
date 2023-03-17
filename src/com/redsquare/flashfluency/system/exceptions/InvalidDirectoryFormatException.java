package com.redsquare.flashfluency.system.exceptions;

import com.redsquare.flashfluency.system.DirectoryParser;
import com.redsquare.flashfluency.system.Settings;

public class InvalidDirectoryFormatException extends InvalidFormatException {
    public static final String CONSEQUENCE_DIRECTORY_NOT_CREATED =
            "The directory was not created.";

    private InvalidDirectoryFormatException(String message, boolean fatal, String consequence) {
        super(message, fatal, consequence);
    }

    public static InvalidDirectoryFormatException rootLabelMissing() {
        return new InvalidDirectoryFormatException(DirectoryParser.NAME_BOUND +
                Settings.ROOT_CODE + DirectoryParser.NAME_BOUND +
                " label is missing at the beginning of the directory_mirror.txt file.",
                true, CONSEQUENCE_DIRECTORY_MIRROR_IS_EMPTY);
    }

    public static InvalidDirectoryFormatException emptyQuotesName() {
        return new InvalidDirectoryFormatException(
                "Directory or deck name is empty.", false,
                CONSEQUENCE_DIRECTORY_MIRROR_IS_EMPTY);
    }

    public static InvalidDirectoryFormatException unevenNumberOfQuotes() {
        return new InvalidDirectoryFormatException(
                "Directory encoding has an uneven number of quotes.", false,
                CONSEQUENCE_DIRECTORY_MIRROR_IS_EMPTY);
    }

    public static InvalidDirectoryFormatException parserQuitPrematurely(String remainingLine) {
        return new InvalidDirectoryFormatException(
                "Parser quit prematurely due to an formatting mistake here: " + remainingLine,
                false, CONSEQUENCE_FILE_READ_ABORTED);
    }

    public static InvalidDirectoryFormatException invalidNameForDirectory(String name) {
        return new InvalidDirectoryFormatException(
                "The name \"" + name + "\" is an invalid directory name as it contains ineligible characters",
                false, CONSEQUENCE_DIRECTORY_NOT_CREATED
        );
    }

    public static InvalidDirectoryFormatException directoryAlreadyContains(String name) {
        return new InvalidDirectoryFormatException(
                "This directory already contains a subdirectory with the name \"" + name + "\"",
                false, CONSEQUENCE_DIRECTORY_NOT_CREATED
        );
    }
}
