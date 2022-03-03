package com.redsquare.flashfluency.system.exceptions;

public abstract class FlashFluencyException extends Exception {
    // consequences
    public static String CONSEQUENCE_FILE_READ_ABORTED =
            "File read was aborted part-way. Data may have partially been read.";
    public static String CONSEQUENCE_DIRECTORY_MIRROR_IS_EMPTY =
            "The directory could not be parsed and thus is running as an empty mirror.";
    public static String CONSEQUENCE_QUESTION_ALREADY_ANSWERED =
            "The question has not been remarked.";

    private final String message;
    private final boolean fatal;
    private final String consequence;

    protected FlashFluencyException(String message, boolean fatal, String consequence) {
        this.message = message;
        this.fatal = fatal;
        this.consequence = consequence;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFatal() {
        return fatal;
    }

    public String getConsequence() {
        return consequence;
    }
}
