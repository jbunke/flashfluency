package com.redsquare.flashfluency.system.exceptions;

public abstract class InvalidFormatException extends FlashFluencyException {
    protected InvalidFormatException(String message, boolean fatal, String consequence) {
        super(message, fatal, consequence);
    }
}
