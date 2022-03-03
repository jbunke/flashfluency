package com.redsquare.flashfluency.cli;

import com.redsquare.flashfluency.system.exceptions.FlashFluencyException;

public class ExceptionMessenger {
    public static void deliver(final String message, final boolean fatal, final String consequence) {
        CLIOutput.writeError(message, fatal, consequence);

        if (fatal)
            System.exit(0); // TODO - potentially expand exit sequence
    }

    public static void deliver(final String message, final boolean fatal) {
        deliver(message, fatal, "[Consequence not specified]");
    }

    public static void deliver(FlashFluencyException e) {
        deliver(e.getMessage(), e.isFatal(), e.getConsequence());
    }
}
