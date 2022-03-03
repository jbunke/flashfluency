package com.redsquare.flashfluency.cli;

import com.redsquare.flashfluency.system.FFDeckFile;
import com.redsquare.flashfluency.system.FFDirectory;
import com.redsquare.flashfluency.system.FFFile;
import com.redsquare.flashfluency.system.Settings;
import com.redsquare.flashfluency.system.exceptions.FFErrorMessages;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;
import com.redsquare.flashfluency.system.exceptions.InvalidDirectoryFormatException;

import java.io.IOException;

public class ContextManager {
    private static FFFile context;
    private static boolean inLesson;

    private static final int EXIT_CODE_EXPECTED = 0;

    public static void main(String[] args) {
        startUp();
    }

    public static void quit() {
        try {
            Settings.save();
        } catch (IOException e) {
            ExceptionMessenger.deliver(
                    FFErrorMessages.MESSAGE_FAILED_TO_WRITE_TO_SETTINGS, false,
                    FFErrorMessages.CONSEQUENCE_SETTINGS_NOT_SAVED
            );
        }

        System.exit(EXIT_CODE_EXPECTED);
    }

    private static void commandLoop() {
        while (!inLesson) {
            CLIOutput.writeUsernamePrompt();
            CLIInput.readCommand();
        }
    }

    private static void startUp() {
        try {
            Settings.loadSettings();
            Settings.loadDirectory();
            initializeContext();
            commandLoop();
        } catch (InvalidDirectoryFormatException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    private static void initializeContext() {
        setContextToRoot();
        inLesson = false;
    }

    private static void setContextToRoot() {
        context = Settings.getRootDirectory();
    }

    public static void setContextToParent() {
        try {
            FFDirectory parent = context.getParent();

            if (parent == null)
                throw FlashFluencyLogicException.cantGoBackFromRootDirectory();

            context = parent;
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    public static void setContextToChild(String name) {
        try {
            if (context instanceof FFDeckFile)
                throw FlashFluencyLogicException.deckFilesHaveNoChildren();

            FFDirectory d = (FFDirectory) context;

            if (!d.hasChild(name))
                throw FlashFluencyLogicException.fileDoesNotExistInDir(name);

            context = d.getChild(name);
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    public static void lessonStarted() {
        inLesson = true;
    }

    public static void lessonFinished() {
        inLesson = false;
    }

    public static FFFile getContext() {
        return context;
    }
}
