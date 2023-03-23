package com.redsquare.flashfluency.cli;

import com.redsquare.flashfluency.logic.Deck;
import com.redsquare.flashfluency.logic.FlashCard;
import com.redsquare.flashfluency.logic.Lesson;
import com.redsquare.flashfluency.system.FFDeckFile;
import com.redsquare.flashfluency.system.FFDirectory;
import com.redsquare.flashfluency.system.FFFile;
import com.redsquare.flashfluency.system.Settings;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;
import com.redsquare.flashfluency.system.exceptions.InvalidDeckFileFormatException;
import com.redsquare.flashfluency.system.exceptions.InvalidDirectoryFormatException;
import com.redsquare.flashfluency.system.exceptions.InvalidFormatException;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CommandParser {
    private static final String ARG_SEPARATOR = " ";

    private static final String CMD_LEARN = "learn"; // DONE
    private static final String CMD_TEST = "test"; // DONE
    private static final String CMD_GOTO = "goto"; // DONE
    private static final String CMD_SET = "set"; // DONE
    private static final String CMD_SETTINGS = "settings"; // DONE
    public static final String CMD_HELP = "help"; // DONE
    private static final String CMD_ADD = "add"; // DONE
    private static final String CMD_REMOVE = "remove"; // DONE
    private static final String CMD_EDIT = "edit"; // DONE
    private static final String CMD_CREATE = "create"; // DONE
    private static final String CMD_IMPORT = "import"; // DONE
    private static final String CMD_CLEAR = "clear"; // DONE
    private static final String CMD_RESET = "reset"; // DONE
    private static final String CMD_SAVE = "save"; // DONE
    private static final String CMD_LIST = "list"; // DONE
    private static final String CMD_VIEW = "view"; // DONE
    private static final String CMD_QUIT = "quit"; // DONE
    private static final String CMD_DUE = "due"; // DONE
    private static final String CMD_HASTAGS = "hastags"; // DONE
    private static final String CMD_BURROW = "burrow"; // DONE
    private static final String CMD_MOVETO = "moveto"; // DONE
    private static final String CMD_DELETE = "delete"; // DONE
    private static final String CMD_TREE = "tree"; // DONE
    private static final String CMD_PRUNE = "prune"; // DONE

    private static final String PARENT_DIR = "..", ROOT_DIR = "",
            COMPLETE_FOLLOWING = ">>", COMPLETE_PRECEDING = "<<", APPEND = "&&", ALL = "all",
            SUBSET = "subset" + ARG_SEPARATOR, TAG = "tag" + ARG_SEPARATOR,
            FLASH_CARD = "flashcard", DIR_SEPARATOR = "/",
            TAG_SEPARATOR = ",", OPTIONAL_OPEN = "(", OPTIONAL_CLOSE = ")",
            REPEAT = "*", VAL = "[X]", NAME = "[name]", ID_CODE = "[id_code]",
            SETTING_ID = "[setting_id]", FILEPATH = "[filepath]",
            DECK = "deck" + ARG_SEPARATOR, DIRECTORY = "dir" + ARG_SEPARATOR;

    public static void parse(String command) {
        if (command.contains(APPEND)) {
            final String[] commandSequence = command.split(APPEND);

            for (String c : commandSequence) {
                final String subCommand = c.trim();

                CLIOutput.writeEchoPrompt();
                CLIOutput.writeCommandEcho(subCommand);

                parse(subCommand);
            }

            return;
        }

        if (command.startsWith(CMD_LEARN))
            parseDeckCommand(Lesson::learn);
        else if (command.startsWith(CMD_HELP))
            parseHelpCommand();
        else if (command.startsWith(CMD_QUIT))
            ContextManager.quit();
        else if (command.startsWith(CMD_LIST))
            parseListCommand(command.length() > CMD_LIST.length()
                    ? getRemaining(command, CMD_LIST + ARG_SEPARATOR)
                    : "");
        else if (command.startsWith(CMD_VIEW))
            parseViewCommand(command.length() > CMD_VIEW.length()
                    ? getRemaining(command, CMD_VIEW + ARG_SEPARATOR)
                    : "");
        else if (command.startsWith(CMD_CLEAR))
            parseDeckCommand(Deck::clearDeck);
        else if (command.startsWith(CMD_RESET))
            parseDeckCommand(Deck::resetMemorizationData);
        else if (command.startsWith(CMD_SAVE))
            parseDeckCommand(Deck::saveDeck);
        else if (command.startsWith(CMD_EDIT))
            parseEditCommand();
        else if (command.startsWith(CMD_DELETE))
            parseDeleteCommand();
        else if (command.startsWith(CMD_PRUNE))
            parsePruneCommand();
        else if (command.startsWith(CMD_SETTINGS))
            Settings.printSettings();
        else if (command.startsWith(CMD_DUE))
            parseDirectoryCommand(CLIOutput::writeDecksWithDueCards);
        else if (command.startsWith(CMD_TREE))
            parseDirectoryCommand(CLIOutput::writeDirectoryTree);
        else if (command.startsWith(CMD_BURROW))
            parseDirectoryCommand(CLIOutput::writeBurrowingSequence);
        else if (command.startsWith(CMD_MOVETO + ARG_SEPARATOR))
            parseMovetoCommand(getRemaining(command, CMD_MOVETO + ARG_SEPARATOR));
        else if (command.startsWith(CMD_HASTAGS + ARG_SEPARATOR))
            parseHastagsCommand(getRemaining(command, CMD_HASTAGS + ARG_SEPARATOR));
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

    private static void parseHastagsCommand(final String remaining) {
        parseDirectoryCommand(CLIOutput::writeDecksWithMatchingTags, remaining);
    }

    private static void parseListCommand(final String remaining) {
        parseDirectoryCommand(CLIOutput::writeDirectoryList, remaining);
    }

    private static void parseViewCommand(final String remaining) {
        parseDeckCommand(CLIOutput::writeDeck, remaining);
    }

    private static void parseRemoveCommand(String remaining) {
        if (remaining.startsWith(TAG)) {
            final String tag = getRemaining(remaining, TAG);
            parseDeckCommand(Deck::removeTag, tag);
        } else if (remaining.startsWith(FLASH_CARD + ARG_SEPARATOR)) {
            final String TYPE_TO_REMOVE = "YES";

            try {
                final String flashCardCode = getRemaining(remaining, FLASH_CARD + ARG_SEPARATOR);
                final Deck deck = getDeck();
                final Optional<FlashCard> flashCardIfFound = deck.getFlashCardFromCode(flashCardCode);

                if (flashCardIfFound.isPresent()) {
                    final FlashCard flashCard = flashCardIfFound.get();

                    CLIOutput.writeRemoveFlashCardAreYouSurePrompt(flashCard, TYPE_TO_REMOVE);
                    final String answer = CLIInput.readInput().trim().toUpperCase();
                    final boolean decisionToDelete = answer.equals(TYPE_TO_REMOVE);

                    if (decisionToDelete)
                        deck.removeFlashCard(flashCard);
                    else
                        CLIOutput.writeDidNotRemoveFlashCardNotification();
                } else
                    throw FlashFluencyLogicException.noFlashCardMatchingCode(flashCardCode);

            } catch (FlashFluencyLogicException e) {
                ExceptionMessenger.deliver(e);
            }
        } else {
            ExceptionMessenger.deliver(FlashFluencyLogicException.invalidArgumentName());
        }
    }

    private static void parsePruneCommand() {
        final String typeToDelete = "YES";

        try {
            final FFDirectory toPruneFrom = getDirectory();

            CLIOutput.writePruneAreYouSurePrompt(toPruneFrom.getName(), typeToDelete);
            final String answer = CLIInput.readInput().trim().toUpperCase();
            final boolean decisionToPrune = answer.equals(typeToDelete);

            if (decisionToPrune) {
                toPruneFrom.prune(true);
                CLIOutput.writePrunedNotification(toPruneFrom.getName());
            }

        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    private static void parseDeleteCommand() {
        final String TYPE_TO_DELETE = "YES";
        final FFFile toDelete = ContextManager.getContext();
        final boolean isDeck = toDelete instanceof FFDeckFile;

        CLIOutput.writeDeleteContextAreYouSurePrompt(toDelete.getName(), isDeck, TYPE_TO_DELETE);
        final String answer = CLIInput.readInput().trim().toUpperCase();
        final boolean decisionToDelete = answer.equals(TYPE_TO_DELETE);

        if (decisionToDelete) {
            try {
                if (toDelete.equals(Settings.getRootDirectory()))
                    throw FlashFluencyLogicException.manipulateRootDirectory();

                ContextManager.setContextToParent();
                toDelete.delete();
            } catch (FlashFluencyLogicException e) {
                ExceptionMessenger.deliver(e);
                return;
            }
        }

        CLIOutput.writeFileDeletedNotification(
                toDelete.getName(), isDeck, decisionToDelete);
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
        } else if (remaining.startsWith(FLASH_CARD)) {
            CLIOutput.writeNewFlashCardCluePrompt();
            String clue = CLIInput.readInput();
            CLIOutput.writeNewFlashCardAnswerPrompt();
            String answer = CLIInput.readInput();

            FlashCard flashCard = FlashCard.createNew(clue, answer);

            try {
                getDeck().addFlashCard(flashCard, false);
            } catch (FlashFluencyLogicException e) {
                ExceptionMessenger.deliver(e);
            }
        }
    }

    private static void parseCreateCommand(String remaining) {
        try {
            if (remaining.startsWith(DECK)) {
                String arg = getRemaining(remaining, DECK);
                if (directoryAlreadyContains(arg)) {
                    throw InvalidDeckFileFormatException.directoryAlreadyContains(arg);
                } else if (isValidName(arg)) {
                    parseDirectoryCommand(FFDirectory::addDeck, arg);
                    CLIOutput.writeCreated(arg, true);
                } else
                    throw InvalidDeckFileFormatException.invalidNameForDeck(arg);
            } else if (remaining.startsWith(DIRECTORY)) {
                String arg = getRemaining(remaining, DIRECTORY);
                if (directoryAlreadyContains(arg)) {
                    throw InvalidDirectoryFormatException.directoryAlreadyContains(arg);
                } else if (isValidName(arg)) {
                    parseDirectoryCommand(FFDirectory::addChildDirectory, arg);
                    CLIOutput.writeCreated(arg, false);
                } else
                    throw InvalidDirectoryFormatException.invalidNameForDirectory(arg);
            }
        } catch (InvalidFormatException | FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    private static boolean isValidName(final String name) {
        if (name.equals(""))
            return false;

        final Set<Character> eligible = Set.of('-', '(', ')', '_', '*', ',', '.', ' ');
        boolean valid = true;

        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);

            final boolean isLowercaseLetter = c >= 'a' && c <= 'z';
            final boolean isUppercaseLetter = c >= 'A' && c <= 'Z';
            final boolean isNumber = c >= '0' && c <= '9';
            final boolean isOtherEligibleCharacter = eligible.contains(c);

            valid &= (isLowercaseLetter || isUppercaseLetter ||
                    isNumber || (c > 0 && isOtherEligibleCharacter));
        }

        return valid;
    }

    private static boolean directoryAlreadyContains(final String name)
            throws FlashFluencyLogicException {
        FFDirectory directory = getDirectory();
        return directory.getChildrenNames().contains(name);
    }

    private static void parseTestCommand(String remaining) {

        if (remaining.startsWith(ALL))
            parseDeckCommand(Lesson::testAll);
        else if (remaining.startsWith(SUBSET)) {
            String arg = getRemaining(remaining, SUBSET);
            parseDeckCommand(Lesson::testSubset, arg);
        }
    }

    private static void parseMovetoCommand(String remaining) {
        final FFFile sourceContext = ContextManager.getContext();

        try {
            if (sourceContext.equals(Settings.getRootDirectory()))
                throw FlashFluencyLogicException.manipulateRootDirectory();
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
            return;
        }

        setContextAsLocation(remaining);

        final FFFile destinationContext = ContextManager.getContext();

        try {
            if (destinationContext instanceof FFDirectory destinationDirectory) {
                final boolean success = sourceContext.moveTo(destinationDirectory);
                ContextManager.setContextManually(sourceContext);

                CLIOutput.writeMoveTo(
                        success, sourceContext instanceof FFDeckFile,
                        sourceContext.getName(), sourceContext.getFilepath()
                );
            } else
                throw FlashFluencyLogicException.deckFilesHaveNoChildren();
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    private static void parseGotoCommand(String remaining) {
        setContextAsLocation(remaining);
    }

    private static void setContextAsLocation(final String path) {
        String[] rs = path.split(DIR_SEPARATOR);

        for (String r : rs) {
            if (r.equals(PARENT_DIR))
                ContextManager.setContextToParent();
            else if (r.equals(ROOT_DIR))
                ContextManager.setContextToRoot();
            else if (r.endsWith(COMPLETE_FOLLOWING)) {
                String prefix = r.substring(0, r.length() - COMPLETE_FOLLOWING.length());
                ContextManager.setContextToChildWithSegment(true, prefix);
            } else if (r.startsWith(COMPLETE_PRECEDING)) {
                String suffix = r.substring(COMPLETE_PRECEDING.length());
                ContextManager.setContextToChildWithSegment(false, suffix);
            } else
                ContextManager.setContextToChild(r);
        }
    }

    private static void parseHelpCommand() {
        final String[] DECK_COMMANDS = {
                CMD_ADD + ARG_SEPARATOR + FLASH_CARD,
                CMD_ADD + ARG_SEPARATOR + TAG + NAME,
                CMD_CLEAR,
                CMD_DELETE,
                CMD_EDIT,
                CMD_GOTO + ARG_SEPARATOR + PARENT_DIR,
                CMD_HELP,
                CMD_IMPORT + ARG_SEPARATOR + FILEPATH,
                CMD_LEARN,
                CMD_MOVETO + ARG_SEPARATOR + NAME + OPTIONAL_OPEN +
                        DIR_SEPARATOR + NAME + OPTIONAL_CLOSE + REPEAT,
                CMD_QUIT,
                CMD_REMOVE + ARG_SEPARATOR + FLASH_CARD + ARG_SEPARATOR + ID_CODE,
                CMD_REMOVE + ARG_SEPARATOR + TAG + NAME,
                CMD_RESET,
                CMD_SAVE,
                CMD_SET + ARG_SEPARATOR + SETTING_ID + ARG_SEPARATOR + VAL,
                CMD_SETTINGS,
                CMD_TEST + ARG_SEPARATOR + ALL,
                CMD_TEST + ARG_SEPARATOR + SUBSET + VAL,
                CMD_VIEW
        };
        final String[] DECK_EXPLANATIONS = {
                "Adds a new flash card to the deck and prompts the user to pass in a clue and answer", // add flashcard
                "Adds tag " + NAME + " to the current deck", // add tag [name]
                "Clears all of the flash cards from the deck, including their memorization data", // clear
                "Deletes the deck - THIS CANNOT BE UNDONE", // delete
                "Prompts the user for a new description for the deck", // edit
                "Changes the context to the deck's parent directory", // goto ..
                "Displays the valid commands at this context scope", // help
                "Imports flash cards from a CSV file " + FILEPATH +
                        " ; only lines with a single comma delimiter " +
                        "separating clue and value are valid and imported", // import [filepath]
                "Runs a spaced repetition lesson in the current deck" +
                        " and updates the memorization status of tested flash cards", // learn
                "Moves the current deck to the destination specified by the path " +
                        "(relative or full)", // moveto [name](/[name])*
                "Saves and quits the program", // quit
                "Removes the flash card with ID code " + ID_CODE + " from the deck", // remove flashcard [id_code]
                "Removes the tag " + NAME + " from the deck", // remove tag [name]
                "Resets all of memorization data for every flash card in the deck", // reset
                "Saves the deck to the associated deck file", // save
                "Sets setting " + SETTING_ID + " to the value " + VAL, // set [setting_id] [X]
                "Lists all the program settings and their current values", // settings
                "Tests all flash cards once with a score at the end;" +
                        " does not affect memorization status", // test all
                "Tests a subset of X cards in the deck with a score at the end;" +
                        " does not affect memorization status", // test subset [X]
                "Prints the deck's description, tags, and flash cards" // view
        };
        final String[] DIR_COMMANDS = {
                CMD_BURROW,
                CMD_CREATE + ARG_SEPARATOR + DECK + NAME,
                CMD_CREATE + ARG_SEPARATOR + DIRECTORY + NAME,
                CMD_DELETE,
                CMD_DUE,
                CMD_GOTO + ARG_SEPARATOR + PARENT_DIR,
                CMD_GOTO + ARG_SEPARATOR + NAME + OPTIONAL_OPEN +
                        DIR_SEPARATOR + NAME + OPTIONAL_CLOSE + REPEAT,
                CMD_HASTAGS + ARG_SEPARATOR + NAME + OPTIONAL_OPEN +
                        TAG_SEPARATOR + NAME + OPTIONAL_CLOSE + REPEAT,
                CMD_HELP,
                CMD_LIST,
                CMD_MOVETO + ARG_SEPARATOR + NAME + OPTIONAL_OPEN +
                        DIR_SEPARATOR + NAME + OPTIONAL_CLOSE + REPEAT,
                CMD_PRUNE,
                CMD_QUIT,
                CMD_SET + ARG_SEPARATOR + SETTING_ID + ARG_SEPARATOR + VAL,
                CMD_SETTINGS,
                CMD_TREE
        };
        final String[] DIR_EXPLANATIONS = {
                "\"Burrows\" deeper within the current directory path until a fork " +
                        "or terminus is reached", // burrow
                "Creates a flash card deck file " + NAME + " in the current directory", // create deck [name]
                "Creates a child directory " + NAME + " in the current directory", // create dir [name]
                "Deletes the directory and its subdirectories and decks - THIS CANNOT BE UNDONE", // delete
                "Finds all of the decks accessible via this context " +
                        "with flash cards that are due", // due
                "Changes the context to the parent directory", // goto ..
                "Goes to a specified directory or deck file using a " +
                        "sub-path specified from the current directory", // goto [name](/[name])*
                "Finds all of the decks accessible via this context " +
                        "with ALL of the tags in the search", // hastags [name](,[name])*
                "Displays the valid commands at this context scope", // help
                "Lists the contents of the current directory", // list
                "Moves the current directory and its subdirectories and decks " +
                        "to the destination specified by the path (relative or full)", // moveto [name](/[name])*
                "Deletes all empty directories accessible from this directory", // prune
                "Saves and quits the program", // quit
                "Sets setting " + SETTING_ID + " to the value " + VAL, // set [setting_id] [X]
                "Lists all the program settings and their current values", // settings
                "Displays the content sub-tree accessible from the current directory" // tree
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
