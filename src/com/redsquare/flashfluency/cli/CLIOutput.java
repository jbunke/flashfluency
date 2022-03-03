package com.redsquare.flashfluency.cli;

import com.redsquare.flashfluency.logic.Deck;
import com.redsquare.flashfluency.logic.FlashCard;
import com.redsquare.flashfluency.logic.Lesson;
import com.redsquare.flashfluency.logic.Question;
import com.redsquare.flashfluency.system.FFDirectory;
import com.redsquare.flashfluency.system.FFFile;
import com.redsquare.flashfluency.system.Settings;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CLIOutput {
    private static final String EMPTY = "";
    private static final String NEW_LINE = "\n";

    private static final int BORDER_TICK_NUM = 50;
    private static final String BORDER_TICK = "+-";

    private static final int UNDERLINE_NUM = 20;
    private static final String UNDERLINE = "-";

    private static final String ANSI_RESET = "\033[0m";

    // Regular Colors
    // private static final String ANSI_BLACK = "\033[0;30m";   // BLACK
    private static final String ANSI_RED = "\033[0;31m";     // RED
    private static final String ANSI_GREEN = "\033[0;32m";   // GREEN
    private static final String ANSI_YELLOW = "\033[0;33m";  // YELLOW
    private static final String ANSI_BLUE = "\033[0;34m";    // BLUE
    private static final String ANSI_PURPLE = "\033[0;35m";  // PURPLE
    private static final String ANSI_CYAN = "\033[0;36m";    // CYAN
    // private static final String ANSI_WHITE = "\033[0;37m";   // WHITE

    // Bold
    // private static final String ANSI_BLACK_BOLD = "\033[1;30m";  // BLACK
    private static final String ANSI_RED_BOLD = "\033[1;31m";    // RED
    private static final String ANSI_GREEN_BOLD = "\033[1;32m";  // GREEN
    private static final String ANSI_YELLOW_BOLD = "\033[1;33m"; // YELLOW
    private static final String ANSI_BLUE_BOLD = "\033[1;34m";   // BLUE
    private static final String ANSI_PURPLE_BOLD = "\033[1;35m"; // PURPLE
    private static final String ANSI_CYAN_BOLD = "\033[1;36m";   // CYAN
    // private static final String ANSI_WHITE_BOLD = "\033[1;37m";  // WHITE

    private static void write(final String formatted, final boolean newLine) {
        System.out.print(formatted + ANSI_RESET + (newLine ? NEW_LINE : EMPTY));
    }

    public static void writeError(final String message,
                                  final boolean fatal, final String consequence) {
        String sb = borderLine() +
                (fatal ? (ANSI_RED_BOLD + "[ FATAL ") : (ANSI_RED + "[ ")) +
                "ERROR ]" + NEW_LINE + message + NEW_LINE + " -> " + consequence +
                NEW_LINE + ANSI_RESET + borderLine();

        write(sb, false);
    }

    private static String borderLine() {
        return BORDER_TICK.repeat(BORDER_TICK_NUM) + NEW_LINE;
    }

    public static void writeDeck(Deck deck) {
        StringBuilder sb = new StringBuilder();

        sb.append("Status of flash card deck \"").append(deck.getName()).append("\":").append(NEW_LINE.repeat(2));

        appendSectionTitle(sb, "Description:");
        sb.append(deck.getDescription()).append(NEW_LINE.repeat(2));

        appendSectionTitle(sb, "Tags:");

        for (String tag : deck.getTags())
            sb.append(ANSI_CYAN_BOLD).append(tag).append(ANSI_RESET).append(", ");
        if (!deck.getTags().isEmpty())
            sb.delete(sb.length() - 2, sb.length());
        sb.append(NEW_LINE.repeat(2));

        appendSectionTitle(sb, "Flash Cards:");

        final String[] CATEGORIES = { "Clue", "Answer", "Introduced?", "Due", "Status", "Answers to Promotion" };
        final List<Function<FlashCard, String>> FUNCTIONS = List.of(
                FlashCard::getClue, FlashCard::getAnswer, x -> x.isIntroduced() ? "Yes" : "No",
                x -> {
                    LocalDate d = x.getDue();

                    return d.getDayOfMonth() + "-" + d.getMonthValue() + "-" + d.getYear();
                },
                x -> switch (x.getPot()) {
                    case A -> ANSI_CYAN_BOLD;
                    case B -> ANSI_GREEN_BOLD;
                    case C, D -> ANSI_YELLOW_BOLD;
                    case NEW -> ANSI_PURPLE_BOLD;
                    case F -> ANSI_RED_BOLD;
                } + x.getPot() + ANSI_RESET,
                x -> String.valueOf(x.getPotCounter())
        );
        final int CAT_COUNT = 6;
        final int[] SPACES = { 50, 50, 15, 15, 15, 30 };
        final int OFFSET = 3;
        final String WHITESPACE = " ";
        final String NON_PRINTED_POSIX = "\\P{Print}";

        final OptionalInt MAX_CLUE_LENGTH = deck.getFlashCardClues().stream().mapToInt(String::length).max();
        final OptionalInt MAX_ANSWER_LENGTH = deck.getFlashCardAnswers().stream().mapToInt(String::length).max();

        SPACES[0] = MAX_CLUE_LENGTH.isPresent() ? (MAX_CLUE_LENGTH.getAsInt() + 5) : 50;
        SPACES[1] = MAX_ANSWER_LENGTH.isPresent() ? (MAX_ANSWER_LENGTH.getAsInt() + 5) : 50;

        // Categories
        StringBuilder catSB = new StringBuilder();
        int catSpaceSum = 0;

        for (int i = 0; i < CAT_COUNT; i++) {
            catSpaceSum += SPACES[i];
            catSB.append(WHITESPACE.repeat(OFFSET)).append(CATEGORIES[i]).
                    append(WHITESPACE.repeat(catSpaceSum -
                            catSB.toString().replaceAll(NON_PRINTED_POSIX, "").length()));
        }

        sb.append(catSB).append(NEW_LINE).append(UNDERLINE.repeat(catSpaceSum)).append(NEW_LINE);

        Set<String> fs = deck.getFlashCardClues();

        for (String f : fs) {
            FlashCard fc = deck.getFlashCard(f);
            StringBuilder flSB = new StringBuilder();
            catSpaceSum = 0;
            for (int i = 0; i < CAT_COUNT; i++) {
                catSpaceSum += SPACES[i];
                if (i == 4) catSpaceSum += 10; // TODO: get rid of this hotfix
                flSB.append(WHITESPACE.repeat(OFFSET)).append(FUNCTIONS.get(i).apply(fc)).
                        append(WHITESPACE.repeat(catSpaceSum -
                                flSB.toString().replaceAll(NON_PRINTED_POSIX, "").length()));
            }

            sb.append(flSB).append(NEW_LINE.repeat(2));
        }

        write(sb.toString(), false);
    }

    private static void appendSectionTitle(StringBuilder sb, String sectionTitle) {
        sb.append(sectionTitle).append(NEW_LINE).append(ANSI_RESET).
                append(UNDERLINE.repeat(UNDERLINE_NUM)).append(NEW_LINE.repeat(2));
    }

    public static void writeDirectoryList(FFDirectory directory) {
        List<String> childrenNames = directory.getChildrenNames().stream().toList();

        StringBuilder sb = new StringBuilder();

        sb.append("Contents of directory \"").append(directory.getName()).append("\":").append(NEW_LINE);

        childrenNames.forEach(x -> {
            sb.append(ANSI_RESET).append(" -> ");

            FFFile child = directory.getChild(x);
            sb.append((child instanceof FFDirectory) ? ANSI_BLUE_BOLD : ANSI_PURPLE_BOLD);
            sb.append(x).append(NEW_LINE);
        });

        write(sb.toString(), false);
    }

    public static void writeUsernamePrompt() {
        String color = ContextManager.getContext() instanceof FFDirectory ? ANSI_BLUE_BOLD : ANSI_PURPLE_BOLD;

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
        String s = borderLine() + ANSI_PURPLE_BOLD + "[ Clue ] : " + clue;
        write(s, true);
    }

    public static void writeFlashCardAnswerPrompt() {
        String s = ANSI_PURPLE_BOLD + "[ Answer ] : ";
        write(s, false);
    }

    public static void writeCorrectAnswer() {
        String s = ANSI_GREEN_BOLD + "[ CORRECT! ]";
        write(s, true);
    }

    public static void writeWrongAnswer(String correctAnswer) {
        String s = ANSI_RED_BOLD + "[ WRONG! ] The correct answer is " +
                ANSI_CYAN_BOLD + correctAnswer;
        write(s, true);
    }

    public static void writeCorrectAnswerAccentDiscrepancy(String correctAnswer) {
        String s = ANSI_GREEN_BOLD + "[ CORRECT! ] ... but watch out for accents: " +
                ANSI_CYAN_BOLD + correctAnswer + ANSI_GREEN_BOLD +
                " is the perfect answer";
        write(s, true);
    }

    public static void writeWrongAnswerWithOptionToMarkCorrect(String correctAnswer) {
        String s = ANSI_RED_BOLD + "[ WRONG! ] The correct answer is " +
                ANSI_CYAN_BOLD + correctAnswer + NEW_LINE + ANSI_PURPLE_BOLD + "Type \"" +
                ANSI_CYAN_BOLD + CLIInput.getTypeToMarkCorrect() + ANSI_PURPLE_BOLD +
                "\" to mark as correct anyway";
        write(s, true);
    }

    public static void writeLessonReview(Lesson lesson) {
        StringBuilder sb = new StringBuilder();
        sb.append(borderLine()).append(ANSI_PURPLE_BOLD).append((lesson.isSR()) ? "[ Training" : "[ Test");
        sb.append(" Finished ]");

        int rightAnswers = lesson.getQuestions().stream().
                filter(Question::isCorrect).collect(Collectors.toSet()).size();

        if (!lesson.isSR())
            sb.append(" ").append(rightAnswers).append(" / ").append(lesson.getQuestions().size());

        sb.append(NEW_LINE);

        sb.append(ANSI_RESET).append(borderLine());
        write(sb.toString(), false);
    }

    public static void writeLessonIntro(Lesson lesson) {
        StringBuilder sb = new StringBuilder();
        sb.append(borderLine()).append(ANSI_PURPLE_BOLD).append((lesson.isSR()) ? "[ Training" : "[ Test");
        sb.append(" Begun ]");

        final int cards = lesson.getQuestions().size();

        sb.append(lesson.isSR() ? (" ( " + cards + " active flash cards )") :
                (" ( does not count towards memorization )")).append(NEW_LINE);

        write(sb.toString(), false);
    }

    public static void writeNewFlashCardCluePrompt() {
        String s = ANSI_PURPLE_BOLD + "Clue for new flash card: ";
        write(s, false);
    }

    public static void writeNewFlashCardAnswerPrompt() {
        String s = ANSI_PURPLE_BOLD + "Answer for new flash card: ";
        write(s, false);
    }

    public static void writeImportedFlashCard(FlashCard flashCard) {
        String s = ANSI_PURPLE_BOLD + "Imported flash card: [ CLUE ] : " +
                ANSI_CYAN_BOLD + flashCard.getClue() + ANSI_PURPLE_BOLD + " , [ ANSWER ] : " +
                ANSI_CYAN_BOLD + flashCard.getAnswer() + ANSI_PURPLE_BOLD;

        write(s, true);
    }

    public static void writeSavedDeck(final Deck deck, final String filepath) {
        String s = borderLine() + ANSI_PURPLE_BOLD + "Saved deck \"" + deck.getName() +
                "\" to file " + filepath + NEW_LINE + ANSI_RESET + borderLine();

        write(s, false);
    }

    public static void writeClearedDeck(final Deck deck) {
        String s = borderLine() + ANSI_PURPLE_BOLD + "Cleared flash cards in deck \"" +
                deck.getName() + "\"" + NEW_LINE + ANSI_RESET + borderLine();

        write(s, false);
    }

    public static void writeCreated(final String name, final boolean isDeck) {
        String s = borderLine() + ANSI_BLUE_BOLD + "Created " +
                (isDeck ? "deck" : "directory") + " \"" + name + "\"" +
                NEW_LINE + ANSI_RESET + borderLine();

        write(s, false);
    }

    public static void writeRetiredLesson() {
        String s = borderLine() + ANSI_PURPLE_BOLD + "Retired from lesson.";

        write(s, true);
    }

    public static void writeSettingSet(String settingID, String value) {
        String s = ANSI_YELLOW_BOLD + "Set \"" + settingID +
                "\" to " + value;

        write(s, true);
    }

    public static void writePrintSettings(String[] technicalKeywords, int[] technicalSettings,
                                          String[] flagsKeywords, boolean[] flags,
                                          String[] otherKeywords, String[] otherSettings) {
        for (int i = 0; i < otherKeywords.length; i++) {
            writePrintSetting(otherKeywords[i], String.valueOf(otherSettings[i]));
        }

        for (int i = 0; i < technicalKeywords.length; i++) {
            writePrintSetting(technicalKeywords[i], String.valueOf(technicalSettings[i]));
        }

        for (int i = 0; i < flagsKeywords.length; i++) {
            writePrintSetting(flagsKeywords[i], String.valueOf(flags[i]));
        }
    }

    private static void writePrintSetting(final String keyword, final String value) {
        String s = ANSI_YELLOW_BOLD + keyword + ANSI_RESET + " -> " +
                ANSI_YELLOW_BOLD + value;
        write(s, true);
    }

    public static void writeHelp(final boolean isDeck, final String[] validCommands,
                                 final String[] explanation) {
        String color = isDeck ? ANSI_PURPLE_BOLD : ANSI_BLUE_BOLD;
        String type = isDeck ? "deck" : "directory";

        StringBuilder sb = new StringBuilder();

        sb.append(borderLine()).append(color).append("The current context is a ")
                .append(ANSI_CYAN_BOLD).append(type).append(color)
                .append(", so the valid commands are:").append(NEW_LINE)
                .append(ANSI_RESET).append(borderLine());

        for (int i = 0; i < validCommands.length; i++) {
            sb.append(ANSI_YELLOW_BOLD).append(validCommands[i]).append(ANSI_RESET)
                    .append(" : ").append(color).append(color).append(explanation[i])
                    .append(NEW_LINE);
        }

        sb.append(ANSI_RESET).append(borderLine());

        write(sb.toString(), false);
    }

    public static void writeDescriptionPrompt() {
        String s = ANSI_PURPLE_BOLD + "Set deck description: ";
        write(s, false);
    }

    public static void writeRemovedTag(String arg) {
        String s = ANSI_PURPLE_BOLD + "Removed tag \"" + arg +
                "\" from the deck";

        write(s, true);
    }

    public static void writeAddedTag(String arg) {
        String s = ANSI_PURPLE_BOLD + "Added tag \"" + arg +
                "\" from the deck";

        write(s, true);
    }

    public static void writeSetRootDirectoryPrompt() {
        String s = ANSI_BLUE_BOLD + "Set root directory: ";
        write(s, false);
    }

    public static void writeSetUsernamePrompt() {
        String s = ANSI_BLUE_BOLD + "Set username: ";
        write(s, false);
    }
}