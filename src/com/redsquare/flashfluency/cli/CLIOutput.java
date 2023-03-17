package com.redsquare.flashfluency.cli;

import com.redsquare.flashfluency.logic.*;
import com.redsquare.flashfluency.system.FFDeckFile;
import com.redsquare.flashfluency.system.FFDirectory;
import com.redsquare.flashfluency.system.FFFile;
import com.redsquare.flashfluency.system.Settings;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CLIOutput {
    private static final String EMPTY = "", INDENT = "    ",
            INDENT_WITH_ARROW = "|   ", HIERARCHY_ARROW = "|-> ";
    private static final String NEW_LINE = "\n";

    private static final int BORDER_TICK_NUM = 50;
    private static final String BORDER_TICK = "+-";

    private static final String DIR_SEPARATOR = "/";

    private static final String ANSI_RESET = "\033[0m";

    // Bold
    // private static final String ANSI_BLACK_BOLD = "\033[1;30m";  // BLACK
    private static final String ANSI_RED_BOLD = "\033[1;31m";    // RED
    private static final String ANSI_GREEN_BOLD = "\033[1;32m";  // GREEN
    private static final String ANSI_YELLOW_BOLD = "\033[1;33m"; // YELLOW
    private static final String ANSI_BLUE_BOLD = "\033[1;34m";   // BLUE
    private static final String ANSI_PURPLE_BOLD = "\033[1;35m"; // PURPLE
    private static final String ANSI_CYAN_BOLD = "\033[1;36m";   // CYAN
    // private static final String ANSI_WHITE_BOLD = "\033[1;37m";  // WHITE

    private static final String NAME_HIGHLIGHT_COLOR = ANSI_CYAN_BOLD,
            VALUE_HIGHLIGHT_COLOR = ANSI_PURPLE_BOLD,
            DIRECTORY_COLOR = ANSI_BLUE_BOLD, DECK_COLOR = ANSI_PURPLE_BOLD,
            SETTING_COLOR = ANSI_YELLOW_BOLD;

    private static void write(final String formatted, final boolean newLine) {
        System.out.print(formatted + ANSI_RESET + (newLine ? NEW_LINE : EMPTY));
    }

    public static void writeError(final String message,
                                  final boolean fatal, final String consequence) {
        String sb = borderLine() + ANSI_RED_BOLD +
                (fatal ? "[ FATAL " : "[ ") +
                "ERROR ]" + NEW_LINE + message + NEW_LINE + "so... " + consequence +
                NEW_LINE + borderLine();

        write(sb, false);
    }

    private static String borderLine() {
        return ANSI_RESET + BORDER_TICK.repeat(BORDER_TICK_NUM) + NEW_LINE;
    }

    private static String potColor(Pot pot) {
        return switch (pot) {
            case A -> ANSI_CYAN_BOLD;
            case B -> ANSI_GREEN_BOLD;
            case C, D -> ANSI_YELLOW_BOLD;
            case NEW -> ANSI_PURPLE_BOLD;
            case F -> ANSI_RED_BOLD;
        };
    }

    public static void writeDeck(Deck deck) {
        StringBuilder sb = new StringBuilder();

        sb.append(borderLine());

        sb.append(DECK_COLOR).append("Details of flash card deck ")
                .append(highlightName(deck.getName(), DECK_COLOR)).append(":")
                .append(NEW_LINE).append(borderLine());

        appendSectionTitle(sb, "Description:");
        sb.append(deck.getDescription()).append(NEW_LINE).append(borderLine());

        appendSectionTitle(sb, "Tags:");

        for (String tag : deck.getTags())
            sb.append(highlightName(tag, ANSI_RESET)).append(", ");
        if (!deck.getTags().isEmpty())
            sb.delete(sb.length() - 2, sb.length());
        sb.append(NEW_LINE).append(borderLine());

        appendSectionTitle(sb, "Breakdown:");

        String color = deckPercentageScoreColor(deck);

        sb.append(ANSI_CYAN_BOLD).append(deck.getNumOfFlashCards())
                .append(ANSI_RESET).append(" total flash cards; ")
                .append(ANSI_PURPLE_BOLD).append(deck.getNumDueFlashCards())
                .append(ANSI_RESET).append(" due; ")
                .append(color).append(deck.getPercentageScore())
                .append(ANSI_RESET).append("% memorized");
        sb.append(NEW_LINE).append("(");

        final Pot[] pots = { Pot.A, Pot.B, Pot.C, Pot.D, Pot.F, Pot.NEW };
        int processed = 0;

        for (Pot pot : pots) {
            color = potColor(pot);

            int cardsInPot = deck.getNumFlashCardsInPot(pot);

            if (processed > 0)
                sb.append(", ");

            sb.append(cardsInPot).append("x ").append(color).append(pot).append(ANSI_RESET);
            processed++;
        }

        sb.append(")").append(NEW_LINE).append(borderLine());

        appendSectionTitle(sb, "Flash Card Table:");
        sb.append(NEW_LINE);

        final int CLUE_INDEX = 0, ANSWER_INDEX = 1, INTRODUCED_INDEX = 2,
                DUE_INDEX = 3, STATUS_INDEX = 4, PROMOTION_INDEX = 5, CAT_COUNT = 6;

        final String[] CATEGORIES = { "Clue", "Answer", "Introduced?", "Due",
                "Status", "Answers to Promotion" };

        final List<Function<FlashCard, String>> FUNCTIONS = List.of(
                FlashCard::getClue, FlashCard::getAnswer, x -> x.isIntroduced() ? "Yes" : "No",
                x -> {
                    LocalDate d = x.getDue();
                    return d.getDayOfMonth() + "-" + d.getMonthValue() + "-" + d.getYear();
                },
                x -> potColor(x.getPot()) + x.getPot() + ANSI_RESET,
                x -> {
                    final int counter = x.getPotCounter();
                    return counter > 0 ? String.valueOf(counter) : "N/A";
                }
        );

        final int[] SPACES = new int[6];
        SPACES[CLUE_INDEX] = 50;
        SPACES[ANSWER_INDEX] = 50;
        SPACES[INTRODUCED_INDEX] = 15;
        SPACES[DUE_INDEX] = 15;
        SPACES[STATUS_INDEX] = 15;
        SPACES[PROMOTION_INDEX] = 30;

        final int OFFSET = 3, BUFFER_THRESHOLD = 5;
        final String WHITESPACE = " ";
        final String NON_PRINTED_POSIX = "\\p{C}";

        final OptionalInt MAX_CLUE_LENGTH = deck.getFlashCardClues().stream().mapToInt(String::length).max();
        final OptionalInt MAX_ANSWER_LENGTH = deck.getFlashCardAnswers().stream().mapToInt(String::length).max();

        if (MAX_CLUE_LENGTH.isPresent())
            SPACES[CLUE_INDEX] = MAX_CLUE_LENGTH.getAsInt() + BUFFER_THRESHOLD;
        if (MAX_ANSWER_LENGTH.isPresent())
            SPACES[ANSWER_INDEX] = MAX_ANSWER_LENGTH.getAsInt() + BUFFER_THRESHOLD;

        // Categories
        StringBuilder catSB = new StringBuilder();
        int catSpaceSum = 0;

        for (int i = 0; i < CAT_COUNT; i++) {
            catSpaceSum += SPACES[i];
            catSB.append(WHITESPACE.repeat(OFFSET)).append(CATEGORIES[i]).
                    append(WHITESPACE.repeat(catSpaceSum -
                            catSB.toString().replaceAll(NON_PRINTED_POSIX, "").length()));
        }

        sb.append(catSB).append(NEW_LINE).append(BORDER_TICK.repeat(catSpaceSum / 2)).append(NEW_LINE);

        List<String> fs = new ArrayList<>(deck.getFlashCardClues());
        fs.sort(Comparator.naturalOrder());

        for (String f : fs) {
            FlashCard fc = deck.getFlashCard(f);
            StringBuilder flSB = new StringBuilder();
            catSpaceSum = 0;
            for (int i = 0; i < CAT_COUNT; i++) {
                catSpaceSum += SPACES[i];
                if (i == STATUS_INDEX) catSpaceSum += 9; // TODO: get rid of this hotfix

                final String precedingWhitespace = WHITESPACE.repeat(OFFSET);
                flSB.append(precedingWhitespace);

                final String flashCardCategoryText = FUNCTIONS.get(i).apply(fc);
                flSB.append(flashCardCategoryText);

                final String lineWithNonPrintedRemoved = flSB.toString().replaceAll(NON_PRINTED_POSIX, "");
                final String followingWhitespace = WHITESPACE.repeat(catSpaceSum - lineWithNonPrintedRemoved.length());
                flSB.append(followingWhitespace);
            }

            sb.append(flSB).append(NEW_LINE.repeat(2));
        }

        sb.delete(sb.length() - 1, sb.length());
        sb.append(borderLine());

        write(sb.toString(), false);
    }

    private static void appendSectionTitle(StringBuilder sb, String sectionTitle) {
        sb.append(DECK_COLOR).append(sectionTitle).append(NEW_LINE).append(ANSI_RESET);
    }

    public static void writeDecksWithMatchingTags(final FFDirectory directory, final String tagString) {
        final String TAG_SEPARATOR = ",";
        final String[] tags = tagString.split(TAG_SEPARATOR);

        for (int i = 0; i < tags.length; i++)
            tags[i] = tags[i].trim();

        final Set<FFDeckFile> hasMatchingTags = new HashSet<>();
        directory.getDecksWithMatchingTags(hasMatchingTags, tags);

        final List<FFDeckFile> decksWithMatchingTags = new ArrayList<>(hasMatchingTags);
        decksWithMatchingTags.sort(Comparator.comparing(
                x -> relativePath(directory, x) + deckInLine(x.getAssociatedDeck())
        ));

        StringBuilder sb = new StringBuilder();
        sb.append(borderLine());
        sb.append(DIRECTORY_COLOR)
                .append("Decks with the tag");

        if (tags.length > 1)
            sb.append("s ");
        else
            sb.append(" ");

        for (String tag : tags)
            sb.append(highlightName(tag, DIRECTORY_COLOR)).append(", ");

        sb.append("accessible from directory ")
                .append(highlightName(directory.getName(), DIRECTORY_COLOR))
                .append(":").append(NEW_LINE);
        sb.append(borderLine());

        formatDeckRelativePaths(decksWithMatchingTags, directory, sb);

        sb.append(borderLine());

        write(sb.toString(), false);
    }

    public static void writeDirectoryTree(final FFDirectory directory) {
        StringBuilder sb = new StringBuilder();
        sb.append(borderLine());
        sb.append(DIRECTORY_COLOR)
                .append("Content sub-tree from directory ")
                .append(highlightName(directory.getName(), DIRECTORY_COLOR))
                .append(":").append(NEW_LINE);
        sb.append(borderLine());

        formatTreeNode(sb, directory, new boolean[] {});

        sb.append(borderLine());

        write(sb.toString(), false);
    }

    private static void formatTreeNode(
            final StringBuilder sb, final FFFile node,
            final boolean[] depthRankArray
    ) {
        sb.append(ANSI_RESET);

        if (depthRankArray.length > 0) {
            for (int i = 1; i < depthRankArray.length; i++)
                sb.append(depthRankArray[i - 1] ? INDENT : INDENT_WITH_ARROW);

            sb.append(HIERARCHY_ARROW);
        }

        if (node instanceof FFDeckFile deckFile)
            sb.append(deckInLine(deckFile.getAssociatedDeck())).append(NEW_LINE);
        else if (node instanceof FFDirectory directory) {
            sb.append(DIRECTORY_COLOR).append(directory.getName()).append(NEW_LINE);

            List<String> children = new ArrayList<>(directory.getChildrenNames());
            children.sort(Comparator.comparing(x -> x));

            for (int i = 0; i < children.size(); i++) {
                final boolean[] newDepthRankArray = new boolean[depthRankArray.length + 1];
                System.arraycopy(depthRankArray, 0, newDepthRankArray, 0, depthRankArray.length);

                final boolean isLastRankForDepth = i + 1 == children.size();
                newDepthRankArray[depthRankArray.length] = isLastRankForDepth;

                formatTreeNode(sb, directory.getChild(children.get(i)), newDepthRankArray);
            }
        }
    }

    public static void writeDecksWithDueCards(final FFDirectory directory) {
        final Set<FFDeckFile> hasDue = new HashSet<>();
        directory.getDecksWithDue(hasDue);

        final List<FFDeckFile> decksWithDueCards = new ArrayList<>(hasDue);
        decksWithDueCards.sort(Comparator.comparingInt(o -> -o.getAssociatedDeck().getNumDueFlashCards()));

        StringBuilder sb = new StringBuilder();
        sb.append(borderLine());
        sb.append(DIRECTORY_COLOR)
                .append("Decks with flash cards that are due, accessible from directory ")
                .append(highlightName(directory.getName(), DIRECTORY_COLOR))
                .append(":").append(NEW_LINE);
        sb.append(borderLine());

        formatDeckRelativePaths(decksWithDueCards, directory, sb);

        sb.append(borderLine());

        write(sb.toString(), false);
    }

    private static void formatDeckRelativePaths(
            final List<FFDeckFile> deckFileList, final FFDirectory directory, final StringBuilder sb
    ) {
        deckFileList.forEach(x ->
                sb.append(ANSI_RESET).append(" -> ")
                        .append(relativePath(directory, x))
                        .append(deckInLine(x.getAssociatedDeck()))
                        .append(NEW_LINE));
    }

    private static String relativePath(final FFDirectory from, final FFFile to) {
        StringBuilder sb = new StringBuilder();

        FFDirectory context = to.getParent();

        while (!context.equals(from)) {
            sb.insert(0, DIR_SEPARATOR);
            sb.insert(0, ANSI_RESET);
            sb.insert(0, context.getName());
            sb.insert(0, DIRECTORY_COLOR);

            context = context.getParent();
        }

        sb.append(ANSI_RESET);
        return sb.toString();
    }

    public static void writeDirectoryList(FFDirectory directory, final String sortingFlag) {
        Comparator<FFFile> sorter = FFFile.getComparator(sortingFlag);

        List<FFFile> children = new ArrayList<>();
        for (String s : directory.getChildrenNames()) {
            FFFile directoryChild = directory.getChild(s);
            children.add(directoryChild);
        }
        children.sort(sorter);

        StringBuilder sb = new StringBuilder();
        sb.append(borderLine());
        sb.append(DIRECTORY_COLOR).append("Contents of directory ")
                .append(highlightName(directory.getName(), DIRECTORY_COLOR))
                .append(":").append(NEW_LINE);
        sb.append(borderLine());

        children.forEach(x -> {
            sb.append(ANSI_RESET).append(" -> ");

            if (x instanceof FFDirectory)
                sb.append(DIRECTORY_COLOR).append(x.getName());
            else
                sb.append(deckInLine(((FFDeckFile) x).getAssociatedDeck()));

            sb.append(NEW_LINE);
        });

        sb.append(borderLine());

        write(sb.toString(), false);
    }

    private static String deckInLine(Deck deck) {
        return DECK_COLOR + deck.getName() +
                ANSI_RESET + " [ " +
                ANSI_PURPLE_BOLD + deck.getNumDueFlashCards() +
                ANSI_RESET + " due, " +
                deckPercentageScoreColor(deck) + deck.getPercentageScore() +
                ANSI_RESET + "% memorized ]";
    }

    private static String deckPercentageScoreColor(Deck deck) {
        final int percentage = deck.getPercentageScore();
        String color;

        if (percentage > 80)
            color = ANSI_CYAN_BOLD;
        else if (percentage > 55)
            color = ANSI_GREEN_BOLD;
        else if (percentage > 30)
            color = ANSI_YELLOW_BOLD;
        else
            color = ANSI_RED_BOLD;

        return color;
    }

    public static void writeUsernamePrompt() {
        String color = ContextManager.getContext() instanceof FFDirectory ? DIRECTORY_COLOR : DECK_COLOR;

        String s = color + "[ " +
                ContextManager.getContext().getName() + " | " + Settings.getUsername() + " ] > ";
        write(s, false);
    }

    public static void writeCardRepeatNotification(int lessonCounter) {
        String s = ANSI_YELLOW_BOLD + "[ This card will repeat at least ("
                + lessonCounter + ") more times. ]" ;
        write(s, true);
    }

    public static void writeFlashCardClue(String clue) {
        String s = borderLine() + DECK_COLOR + "[ Clue ] : " + NAME_HIGHLIGHT_COLOR + clue;
        write(s, true);
    }

    public static void writeFlashCardAnswerPrompt() {
        String s = DECK_COLOR + "[ Answer ] : " + ANSI_RESET;
        write(s, false);
    }

    public static void writeQuestionFeedback(
            final boolean initiallyMarkAsCorrect,
            final boolean timedOut, final boolean isStrictlyCorrect,
            final boolean isCorrectWithConcessions,
            final String correctAnswer, final int elapsedTime
    ) {
        StringBuilder sb = new StringBuilder();

        if (initiallyMarkAsCorrect) {
            sb.append(ANSI_GREEN_BOLD).append("[ CORRECT! ]");

            if (!isStrictlyCorrect && isCorrectWithConcessions)
                sb.append(" ... but watch out for accents: ")
                        .append(highlightName(correctAnswer, ANSI_GREEN_BOLD))
                        .append(" is the perfect answer");

            if (Settings.isInTimedMode())
                sb.append(NEW_LINE).append(DECK_COLOR).append("Answered in ")
                        .append(highlightName(String.valueOf(elapsedTime), DECK_COLOR))
                        .append(" seconds");
        } else {
            sb.append(ANSI_RED_BOLD);

            if (!(isStrictlyCorrect || isCorrectWithConcessions))
                sb.append("[ WRONG! ]")
                        .append(" ... the correct answer is ")
                        .append(highlightName(correctAnswer, ANSI_RED_BOLD));
            else if (timedOut)
                sb.append("[ CORRECT ] ... but you took ")
                        .append(highlightName(String.valueOf(elapsedTime), ANSI_RED_BOLD))
                        .append(" seconds to answer the question, so it will be marked as wrong");
        }

        write(sb.toString(), true);
    }

    public static void writeOptionToMarkCorrectPrompt() {
        final String toMarkAsCorrect = CLIInput.getTypeToMarkCorrect();

        String s = DECK_COLOR + (toMarkAsCorrect.length() > 1 ? "Type " : "Press ") +
                highlightName("[ " + toMarkAsCorrect + " ]",
                        DECK_COLOR) + " to mark as correct anyway: ";
        write(s, false);
    }

    public static void writeLessonReview(Lesson lesson) {
        StringBuilder sb = new StringBuilder();
        sb.append(borderLine()).append(ANSI_PURPLE_BOLD).append("[ Finished ");
        sb.append((lesson.isSR()) ? "Training" : "Test").append(" ]");

        final String arrowDirection = Settings.isInReverseMode() ? " <- " : " -> ";

        int rightAnswers = lesson.getQuestions().stream().
                filter(Question::isCorrect).collect(Collectors.toSet()).size();

        if (!lesson.isSR())
            sb.append(" ").append(rightAnswers).append(" / ").append(lesson.getQuestions().size());

        sb.append(NEW_LINE);

        sb.append(borderLine());

        List<FlashCard> flashCards = new ArrayList<>();
        List<Question> questions = lesson.getQuestions();

        for (Question q : questions) {
            FlashCard f = q.getFlashCard();
            if (!flashCards.contains(f))
                flashCards.add(f);
        }

        for (FlashCard f : flashCards) {
            sb.append(ANSI_RESET).append(flashCards.indexOf(f) + 1)
                    .append(". ").append(ANSI_PURPLE_BOLD).append(f.getClue())
                    .append(ANSI_RESET).append(arrowDirection).append(ANSI_PURPLE_BOLD)
                    .append(f.getAnswer()).append(ANSI_RESET).append(" [");

            questions.stream().filter(x -> x.getFlashCard().equals(f)).forEach(x -> {
                String representation = x.isAnswered() ? " X" : " -";
                sb.append(x.isCorrect() ? ANSI_GREEN_BOLD : ANSI_RED_BOLD)
                        .append(representation);
            });

            sb.append(ANSI_RESET);
            if (lesson.isSR())
                sb.append(" ] ... updated status: ")
                        .append(potColor(f.getPot())).append(f.getPot().name());
            else
                sb.append(" ]");
            sb.append(NEW_LINE);
        }

        sb.append(borderLine());

        write(sb.toString(), false);
    }

    public static void writeLessonIntro(Lesson lesson) {
        StringBuilder sb = new StringBuilder();
        sb.append(borderLine()).append(ANSI_PURPLE_BOLD).append("[ Started ");
        sb.append((lesson.isSR()) ? "Training" : "Test").append(" ]");

        final int cards = lesson.getQuestions().size();

        sb.append(lesson.isSR() ? (" ( " + cards + " active flash cards )") :
                (" ( does not count towards memorization )")).append(NEW_LINE);

        write(sb.toString(), false);
    }

    public static void writeNewFlashCardCluePrompt() {
        String s = DECK_COLOR + "Clue for new flash card: ";
        write(s, false);
    }

    public static void writeNewFlashCardAnswerPrompt() {
        String s = DECK_COLOR + "Answer for new flash card: ";
        write(s, false);
    }

    public static void writeImportedFlashCard(FlashCard flashCard) {
        String s = DECK_COLOR + "Imported flash card: [ CLUE ] : " +
                highlightName(flashCard.getClue(), DECK_COLOR) + " , [ ANSWER ] : " +
                highlightName(flashCard.getAnswer(), DECK_COLOR);

        write(s, true);
    }

    public static void writeSavedDeck(final Deck deck, final String filepath) {
        String s = borderLine() + DECK_COLOR + "Saved deck " +
                highlightName(deck.getName(), DECK_COLOR) + " to file " +
                highlightName(filepath, DECK_COLOR) + NEW_LINE + borderLine();

        write(s, false);
    }

    public static void writeClearedDeck(final Deck deck) {
        String s = borderLine() + DECK_COLOR + "Cleared flash cards in deck " +
                highlightName(deck.getName(), DECK_COLOR) +
                NEW_LINE + borderLine();

        write(s, false);
    }

    public static void writeResetDeckMemorizationData(final Deck deck) {
        String s = borderLine() + DECK_COLOR +
                "Reset memorization data of flash cards in deck " +
                highlightName(deck.getName(), DECK_COLOR) +
                NEW_LINE + borderLine();

        write(s, false);
    }

    public static void writeCreated(final String name, final boolean isDeck) {
        String s = borderLine() + DIRECTORY_COLOR + "Created " +
                (isDeck ? "deck " : "directory ") +
                highlightName(name, DIRECTORY_COLOR) +
                NEW_LINE + borderLine();

        write(s, false);
    }

    public static void writeRetiredLesson() {
        String s = borderLine() + DECK_COLOR + "Retired from lesson.";

        write(s, true);
    }

    public static void writeSettingUpdateNotification(final String reason) {
        String s = borderLine() + DECK_COLOR + "The active deck is " +
                highlightName(reason, DECK_COLOR) +
                ", so the following settings have been updated:";

        write(s, true);
    }

    public static void writeSettingSet(
            final String settingID, final String value,
            final boolean precedingBorderLine,
            final boolean followingBorderLine
    ) {
        String s = (precedingBorderLine ? borderLine() : "") +
                SETTING_COLOR + "Set " +
                highlightName(settingID, SETTING_COLOR) + " to " +
                VALUE_HIGHLIGHT_COLOR + value + NEW_LINE +
                (followingBorderLine ? borderLine() : "");

        write(s, false);
    }

    public static void writePrintSettings(String[] technicalKeywords, int[] technicalSettings,
                                          String[] flagsKeywords, boolean[] flags,
                                          String[] otherKeywords, String[] otherSettings) {
        write(borderLine(), false);

        for (int i = 0; i < otherKeywords.length; i++) {
            writePrintSetting(otherKeywords[i], String.valueOf(otherSettings[i]));
        }

        for (int i = 0; i < technicalKeywords.length; i++) {
            writePrintSetting(technicalKeywords[i], String.valueOf(technicalSettings[i]));
        }

        for (int i = 0; i < flagsKeywords.length; i++) {
            writePrintSetting(flagsKeywords[i], String.valueOf(flags[i]));
        }

        write(borderLine(), false);
    }

    private static void writePrintSetting(final String keyword, final String value) {
        String s = SETTING_COLOR + keyword + ANSI_RESET + " : " +
                SETTING_COLOR + value;
        write(s, true);
    }

    public static void writeHelp(final boolean isDeck, final String[] validCommands,
                                 final String[] explanation) {
        String color = isDeck ? DECK_COLOR : DIRECTORY_COLOR;
        String type = isDeck ? "deck" : "directory";

        StringBuilder sb = new StringBuilder();

        sb.append(borderLine()).append(color).append("The current context is a ")
                .append(highlightName(type, color))
                .append(", so the valid commands are:").append(NEW_LINE)
                .append(borderLine());

        for (int i = 0; i < validCommands.length; i++) {
            sb.append(ANSI_YELLOW_BOLD).append(validCommands[i])
                    .append(ANSI_RESET).append(" : ").append(color)
                    .append(explanation[i]).append(NEW_LINE);
        }

        sb.append(borderLine());

        write(sb.toString(), false);
    }

    public static void writeDescriptionPrompt() {
        String s = DECK_COLOR + "Set deck description: ";
        write(s, false);
    }

    public static void writeRemovedTag(String arg) {
        String s = DECK_COLOR + "Removed tag " +
                highlightName(arg, DECK_COLOR) +
                " from the deck";

        write(s, true);
    }

    public static void writeAddedTag(String arg) {
        String s = DECK_COLOR + "Added tag " +
                highlightName(arg, DECK_COLOR) +
                " to the deck";

        write(s, true);
    }

    public static void writeBurrowingSequence(final FFDirectory currentDirectory) {
        FFFile context = ContextManager.getContext();

        // Sanity check
        if (!context.equals(currentDirectory)) {
            ExceptionMessenger.deliver(
                    "The current context is not the expected directory.",
                    false, FlashFluencyLogicException.CONSEQUENCE_COMMAND_NOT_EXECUTED
            );
            return;
        }

        // Header
        write(borderLine(), false);

        // Burrowing logic
        final String NONE = "";

        while (context instanceof FFDirectory directory && directory.getChildrenNames().size() == 1) {
            String nestedName = NONE;

            for (String childName : directory.getChildrenNames())
                nestedName = childName;

            ContextManager.setContextToChild(nestedName);
            context = ContextManager.getContext();

            CLIOutput.writeBurrowNotification(
                    context.getName(), context instanceof FFDeckFile
            );
        }

        // Postmortem termination cases and messages
        if (context instanceof FFDirectory directory) {
            final int numChildren = directory.getChildrenNames().size();

            if (numChildren == 0)
                CLIOutput.writeBurrowTermination(
                        directory.getName(), false, " has no child to nest into"
                );
            else
                CLIOutput.writeBurrowTermination(
                        directory.getName(), false, " has multiple paths to choose from"
                );
        } else if (context instanceof FFDeckFile deckFile) {
            CLIOutput.writeBurrowTermination(
                    deckFile.getName(), true, " is a deck and thus a terminus"
            );
        }
    }

    private static void writeBurrowNotification(
            final String contextName, final boolean isDeck
    ) {
        final String color = isDeck ? DECK_COLOR : DIRECTORY_COLOR;
        String s = color + "Burrowed into " +
                (isDeck ? "deck " : "directory ") +
                highlightName(contextName, color) + ".";
        write(s, true);
    }

    private static void writeBurrowTermination(
            final String contextName, final boolean isDeck, final String reason
    ) {
        final String color = isDeck ? DECK_COLOR : DIRECTORY_COLOR;
        String s = color + "The " + (isDeck ? "deck " : "directory ") +
                highlightName(contextName, color) + reason +
                ", so the burrowing process has terminated." + NEW_LINE +
                borderLine();
        write(s, false);
    }

    public static void writeMoveTo(
            final boolean success, final boolean isDeck,
            final String name, final String filepath
    ) {
        final String color = isDeck ? DECK_COLOR : DIRECTORY_COLOR;

        String s = borderLine() + color + "The " + (isDeck ? "deck " : "directory ") +
                highlightName(name, color) +
                (success
                        ? (" was moved to " + filepath)
                        : " could not be moved.") +
                NEW_LINE + borderLine();

        write(s, false);
    }

    public static void writeSetRootDirectoryPrompt() {
        String s = DIRECTORY_COLOR + "Set root directory: ";
        write(s, false);
    }

    public static void writeSetUsernamePrompt() {
        String s = ANSI_RESET + "Set username: ";
        write(s, false);
    }

    public static void writeWelcomeMessage() {
        String s = borderLine() + DIRECTORY_COLOR + "Welcome to Flash Fluency!" + NEW_LINE +
                "Flash Fluency is a flash card spaced repetition memorization program." + NEW_LINE +
                "Jordan Bunke (2022)" + NEW_LINE +
                "Type " + highlightName(CommandParser.CMD_HELP, DIRECTORY_COLOR) + " to get started." +
                NEW_LINE + borderLine();
        write(s, false);
    }

    private static String highlightName(final String name, final String revertColor) {
        return NAME_HIGHLIGHT_COLOR + name + revertColor;
    }

    public static void writeDeleteAreYouSurePrompt(
            final String name, final boolean isDeck, final String typeToDelete
    ) {
        final String color = isDeck ? DECK_COLOR : DIRECTORY_COLOR;

        String s = color + "Are you sure you want to delete the " +
                (isDeck ? "deck " : "directory ") + highlightName(name, color) +
                "? Type " + ANSI_RED_BOLD + typeToDelete + color + " to delete: ";

        write(s, false);
    }

    public static void writeFileDeletedNotification(
            final String name, final boolean isDeck, final boolean deleted
    ) {
        final String color = isDeck ? DECK_COLOR : DIRECTORY_COLOR;

        String s = borderLine() + color + "The " +
                (isDeck ? "deck " : "directory ") +
                highlightName(name, color) + " was " +
                (deleted ? "" : "not ") + "deleted." +
                NEW_LINE + borderLine();
        write(s, false);
    }
}
