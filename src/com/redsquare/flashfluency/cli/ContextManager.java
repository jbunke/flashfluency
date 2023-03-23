package com.redsquare.flashfluency.cli;

import com.redsquare.flashfluency.system.FFDeckFile;
import com.redsquare.flashfluency.system.FFDirectory;
import com.redsquare.flashfluency.system.FFFile;
import com.redsquare.flashfluency.system.Settings;
import com.redsquare.flashfluency.system.exceptions.FFErrorMessages;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;
import com.redsquare.flashfluency.system.exceptions.InvalidDirectoryFormatException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContextManager {
    private static FFFile context;
    private static boolean inLesson;

    private static final String VERSION = "0.2";
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
            CLIOutput.writeWelcomeMessage(VERSION);
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

    public static void setContextToRoot() {
        context = Settings.getRootDirectory();
    }

    public static void setContextToParent() {
        try {
            FFDirectory parent = context.getParent();

            if (parent == null)
                throw FlashFluencyLogicException.manipulateRootDirectory();

            context = parent;
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    public static void setContextToChild(final String name) {
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

    public static void setContextToChildWithSegment(final boolean isPrefix, final String segment) {
        try {
            if (context instanceof FFDeckFile)
                throw FlashFluencyLogicException.deckFilesHaveNoChildren();

            FFDirectory d = (FFDirectory) context;

            final List<String> matchingChildren = new ArrayList<>();

            for (String childName : d.getChildrenNames())
                if (isPrefix ? childName.startsWith(segment) : childName.endsWith(segment))
                    matchingChildren.add(childName);

            if (matchingChildren.isEmpty())
                throw FlashFluencyLogicException.fileWithSegmentTypeDoesNotExistInDir(isPrefix, segment);
            else if (matchingChildren.size() > 1)
                throw FlashFluencyLogicException.multipleMatchesForSegmentType(isPrefix, segment);

            context = d.getChild(matchingChildren.get(0));
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    public static void setContextManually(FFFile file) {
        context = file;
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
