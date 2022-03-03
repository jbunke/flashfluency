package com.redsquare.flashfluency.cli;

import com.redsquare.flashfluency.logic.Deck;
import com.redsquare.flashfluency.logic.FlashCard;
import com.redsquare.flashfluency.logic.Lesson;
import com.redsquare.flashfluency.system.FFDeckFile;
import com.redsquare.flashfluency.system.FFDirectory;
import com.redsquare.flashfluency.system.Settings;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;
import com.redsquare.flashfluency.system.exceptions.InvalidDeckFileFormatException;
import com.redsquare.flashfluency.system.exceptions.InvalidDirectoryFormatException;
import com.redsquare.flashfluency.system.exceptions.InvalidFormatException;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CommandParser {
    private static final String ARG_SEPARATOR = " ";

    private static final String CMD_LEARN = "learn"; // DONE
    private static final String CMD_TEST = "test"; // DONE
    private static final String CMD_GOTO = "goto"; // DONE
    private static final String CMD_SET = "set"; // DONE
    private static final String CMD_SETTINGS = "settings"; // DONE
    private static final String CMD_HELP = "help"; // DONE
    private static final String CMD_ADD = "add"; // DONE
    private static final String CMD_REMOVE = "remove"; // DONE
    private static final String CMD_EDIT = "edit"; // DONE
    private static final String CMD_CREATE = "create"; // DONE
    private static final String CMD_IMPORT = "import"; // DONE
    private static final String CMD_CLEAR = "clear"; // DONE
    private static final String CMD_SAVE = "save"; // DONE
    private static final String CMD_LIST = "list"; // DONE
    private static final String CMD_VIEW = "view"; // DONE
    private static final String CMD_QUIT = "quit"; // DONE

    private static final String PARENT_DIR = "..";
    private static final String ALL = "all";
    private static final String SUBSET = "subset" + ARG_SEPARATOR;
    private static final String TAG = "tag" + ARG_SEPARATOR;
    private static final String FLASH_CARD = "flashcard";
    private static final String DIR_SEPARATOR = "/";
    private static final String OPTIONAL_OPEN = "(";
    private static final String OPTIONAL_CLOSE = ")";
    private static final String REPEAT = "*";
    private static final String VAL = "[X]";
    private static final String NAME = "[name]";
    private static final String SETTING_ID = "[setting_id]";
    private static final String FILEPATH = "[filepath]";
    private static final String DECK = "deck" + ARG_SEPARATOR;
    private static final String DIRECTORY = "dir" + ARG_SEPARATOR;

    public static void parse(String command) {
        if (command.startsWith(CMD_LEARN))
            parseDeckCommand(Lesson::learn);
        else if (command.startsWith(CMD_HELP))
            parseHelpCommand();
        else if (command.startsWith(CMD_QUIT))
            ContextManager.quit();
        else if (command.startsWith(CMD_LIST))
            parseDirectoryCommand(CLIOutput::writeDirectoryList);
        else if (command.startsWith(CMD_VIEW))
            parseDeckCommand(CLIOutput::writeDeck);
        else if (command.startsWith(CMD_CLEAR))
            parseDeckCommand(Deck::clearDeck);
        else if (command.startsWith(CMD_SAVE))
            parseDeckCommand(Deck::saveDeck);
        else if (command.startsWith(CMD_EDIT))
            parseEditCommand();
        else if (command.startsWith(CMD_SETTINGS))
            Settings.printSettings();
        else if (command.startsWith(CMD_GOTO + ARG_SEPARATOR))
            parseGotoCommand(getRemaining(command, CMD_GOTO + ARG_SEPARATOR));
        else if (command.startsWith(CMD_TEST + ARG_SEPARATOR))
            parseTestCommand(getRemaining(command, CMD_TEST + ARG_SEPARATOR));
        else if (command.startsWith(CMD_CREATE + ARG_SEPARATOR))
            parseCreateCommand(getRemaining(command, CMD_CREATE + ARG_SEPARATOR));
        else if (command.startsWith(CMD_ADD + ARG_SEPARATOR))
            parseAddCommand(getRemaining(command, CMD_ADD + ARG_SEPARATOR));
        else if (command.startsWith(CMD_REMOVE + ARG_SEPARATOR))
            parseRemoveCommand(getRemaining(command, CMD_REMOVE + ARG_SEPARATOR));
        else if (command.startsWith(CMD_IMPORT + ARG_SEPARATOR))
            parseImportCommand(getRemaining(command, CMD_IMPORT + ARG_SEPARATOR));
        else if (command.startsWith(CMD_SET + ARG_SEPARATOR))
            parseSetCommand(getRemaining(command, CMD_SET + ARG_SEPARATOR));
    }

    private static void parseRemoveCommand(String remaining) {
        if (remaining.startsWith(TAG)) {
            String arg = getRemaining(remaining, TAG);
            parseDeckCommand(Deck::removeTag, arg);
            CLIOutput.writeRemovedTag(arg);
        } else {
            ExceptionMessenger.deliver(FlashFluencyLogicException.invalidArgumentName());
        }

        // TODO: consider adding remove flash card functionality
    }

    private static void parseEditCommand() {
        CLIOutput.writeDescriptionPrompt();
        String description = CLIInput.readInput();
        parseDeckCommand(Deck::setDescription, description);
    }

    private static void parseSetCommand(String args) {
        final int SETTING = 0, VALUE = 1;

        String[] split = args.split(ARG_SEPARATOR);

        try {
            if (split.length != 2)
                throw FlashFluencyLogicException.invalidNumberOfArguments();

            Settings.set(split[SETTING].trim(), split[VALUE].trim());
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    private static void parseImportCommand(String filepath) {
        parseDeckCommand(Deck::importCards, filepath);
    }

    private static void parseAddCommand(String remaining) {

        if (remaining.startsWith(TAG)) {
            String arg = getRemaining(remaining, TAG);
            parseDeckCommand(Deck::addTag, arg);
            CLIOutput.writeAddedTag(arg);
        } else if (remaining.startsWith(FLASH_CARD)) {
            CLIOutput.writeNewFlashCardCluePrompt();
            String clue = CLIInput.readInput();
            CLIOutput.writeNewFlashCardAnswerPrompt();
            String answer = CLIInput.readInput();

            FlashCard flashCard = FlashCard.createNew(clue, answer);

            try {
                getDeck().addFlashCard(flashCard);
            } catch (FlashFluencyLogicException e) {
                ExceptionMessenger.deliver(e);
            }
        }
    }

    private static void parseCreateCommand(String remaining) {

        try {
            if (remaining.startsWith(DECK)) {
                String arg = getRemaining(remaining, DECK);
                if (isValidName(arg)) {
                    parseDirectoryCommand(FFDirectory::addDeck, arg);
                    CLIOutput.writeCreated(arg, true);
                } else
                    throw InvalidDeckFileFormatException.invalidNameForDeck(arg);
            } else if (remaining.startsWith(DIRECTORY)) {
                String arg = getRemaining(remaining, DIRECTORY);
                if (isValidName(arg)) {
                    parseDirectoryCommand(FFDirectory::addChildDirectory, arg);
                    CLIOutput.writeCreated(arg, false);
                } else
                    throw InvalidDirectoryFormatException.invalidNameForDirectory(arg);
            }
        } catch (InvalidFormatException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    private static boolean isValidName(final String name) {
        final char[] ineligible =
                { '\'', '"', '\\', '/', '.', '-', '>', '(', ')', '[', ']' };
        final int NOT_FOUND = -1;
        boolean valid = true;

        for (char c : ineligible) {
            valid &= name.indexOf(c) == NOT_FOUND;
        }

        return valid;
    }

    private static void parseTestCommand(String remaining) {

        if (remaining.startsWith(ALL))
            parseDeckCommand(Lesson::testAll);
        else if (remaining.startsWith(SUBSET)) {
            String arg = getRemaining(remaining, SUBSET);
            parseDeckCommand(Lesson::testSubset, arg);
        }
    }

    private static void parseGotoCommand(String remaining) {
        String[] rs = remaining.split(DIR_SEPARATOR);

        for (String r : rs) {
            if (r.equals(PARENT_DIR))
                ContextManager.setContextToParent();
            else
                ContextManager.setContextToChild(r);
        }
    }

    private static void parseHelpCommand() {
        final String[] DECK_COMMANDS = {
                CMD_LEARN,
                CMD_TEST + ARG_SEPARATOR + ALL,
                CMD_TEST + ARG_SEPARATOR + SUBSET + VAL,
                CMD_GOTO + ARG_SEPARATOR + PARENT_DIR,
                CMD_SET + ARG_SEPARATOR + SETTING_ID + ARG_SEPARATOR + VAL,
                CMD_SETTINGS,
                CMD_ADD + ARG_SEPARATOR + TAG + NAME,
                CMD_ADD + ARG_SEPARATOR + FLASH_CARD,
                CMD_IMPORT + ARG_SEPARATOR + FILEPATH,
                CMD_CLEAR,
                CMD_SAVE,
                CMD_VIEW,
                CMD_QUIT,
                CMD_EDIT,
                CMD_REMOVE + ARG_SEPARATOR + TAG + NAME,
                CMD_HELP
        };
        final String[] DECK_EXPLANATIONS = {
                "Runs a spaced repetition lesson in the current deck" +
                        " and updates the memorization status of tested flash cards", // learn
                "Tests all flash cards once with a score at the end;" +
                        " does not affect memorization status", // test all
                "Tests a subset of X cards in the deck with a score at the end;" +
                        " does not affect memorization status", // test subset [X]
                "Changes the context to the deck's parent directory", // goto ..
                "Sets setting " + SETTING_ID + " to the value " + VAL, // set [setting_id] [X]
                "Lists all the program settings and their current values", // settings
                "Adds tag " + NAME + " to the current deck", // add tag [name]
                "Adds a new flash card to the deck and prompts the user to pass in a clue and answer", // add flashcard
                "Imports flash cards from a CSV file " + FILEPATH +
                        " ; only lines with a single comma delimiter " +
                        "separating clue and value are valid and imported", // import [filepath]
                "Clears all of the flash cards from the deck, including their memorization data", // clear
                "Saves the deck to the associated deck file", // save
                "Prints the deck's description, tags, and flash cards", // view
                "Saves and quits the program", // quit
                "Prompts the user for a new description for the deck", // edit
                "Removes the tag " + NAME + " from the deck", // remove
                "Displays the valid commands at this context scope" // help
        };
        final String[] DIR_COMMANDS = {
                CMD_GOTO + ARG_SEPARATOR + PARENT_DIR,
                CMD_GOTO + ARG_SEPARATOR + NAME + OPTIONAL_OPEN +
                        DIR_SEPARATOR + NAME + OPTIONAL_CLOSE + REPEAT,
                CMD_SET + ARG_SEPARATOR + SETTING_ID + ARG_SEPARATOR + VAL,
                CMD_SETTINGS,
                CMD_CREATE + ARG_SEPARATOR + DIRECTORY + NAME,
                CMD_CREATE + ARG_SEPARATOR + DECK + NAME,
                CMD_LIST,
                CMD_QUIT,
                CMD_HELP
        };
        final String[] DIR_EXPLANATIONS = {
                "Changes the context to the parent directory", // goto ..
                "Goes to a specified directory or deck file using a " +
                        "sub-path specified from the current directory", // goto [name](/[name])*
                "Sets setting " + SETTING_ID + " to the value " + VAL, // set [setting_id] [X]
                "Lists all the program settings and their current values", // settings
                "Creates a child directory " + NAME + " in the current directory", // create dir [name]
                "Creates a flash card deck file " + NAME + " in the current directory", // create deck [name]
                "Lists the contents of the current directory", // list
                "Saves and quits the program", // quit
                "Displays the valid commands at this context scope" // help
        };

        if (ContextManager.getContext() instanceof FFDirectory)
            CLIOutput.writeHelp(false, DIR_COMMANDS, DIR_EXPLANATIONS);
        else
            CLIOutput.writeHelp(true, DECK_COMMANDS, DECK_EXPLANATIONS);
    }

    private static FFDirectory getDirectory() throws FlashFluencyLogicException {
        if (!(ContextManager.getContext() instanceof FFDirectory))
            throw FlashFluencyLogicException.deckFilesHaveNoChildren();

        return (FFDirectory) ContextManager.getContext();
    }

    private static Deck getDeck() throws FlashFluencyLogicException {
        if (!(ContextManager.getContext() instanceof FFDeckFile))
            throw FlashFluencyLogicException.contextIsNotDeckFile();

        return ((FFDeckFile) ContextManager.getContext()).getAssociatedDeck();
    }

    private static void parseDeckCommand(Consumer<Deck> c) {
        try {
            c.accept(getDeck());
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    private static void parseDeckCommand(BiConsumer<Deck, String> c, final String arg) {
        try {
            c.accept(getDeck(), arg);
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    private static void parseDirectoryCommand(Consumer<FFDirectory> c) {
        try {
            c.accept(getDirectory());
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    private static void parseDirectoryCommand(BiConsumer<FFDirectory, String> c, final String arg) {
        try {
            c.accept(getDirectory(), arg);
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    private static String getRemaining(final String command, final String cutStarting) {
        return command.substring(cutStarting.length()).trim();
    }
}