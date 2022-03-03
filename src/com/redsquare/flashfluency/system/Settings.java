package com.redsquare.flashfluency.system;

import com.redsquare.flashfluency.cli.CLIOutput;
import com.redsquare.flashfluency.cli.ExceptionMessenger;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;
import com.redsquare.flashfluency.system.exceptions.InvalidDirectoryFormatException;

import java.io.*;
import java.util.Scanner;

public class Settings {
    public static final String ROOT_CODE = "(root)";
    public static final String DECK_FILE_EXTENSION = ".flfl";
    public static final String SETTING_SEPARATOR = ":", NEW_LINE = "\n";

    // resource file locations
    private static final String SETTINGS_FILEPATH = "res\\settings\\settings.txt";
    private static final String DIRECTORY_MIRROR_FILEPATH =
            "res\\settings\\directory_mirror.txt";

    // indices
    private static final int LESSON_INTRO_LIMIT = 0,
            LESSON_COUNTER_NEW = 1, LESSON_COUNTER_REVIEW = 2;
            // FAST_ANSWER_SECONDS_THRESHOLD = 3;
    private static final int MARK_FOR_ACCENTS = 0,
            OPTION_TO_MARK_MISMATCH_AS_CORRECT = 1, IGNORE_BRACKETED = 2;

    // KEYWORDS
    private static final String[] TECHNICAL_KEYWORDS =
            { "lesson_introduction_limit", "lesson_counter_new",
                    "lesson_counter_review" }; // "fast_answer_threshold" };
    private static final String[] FLAGS_KEYWORDS =
            { "mark_for_accents", "option_to_mark_mismatch_as_correct", "ignore_bracketed" };
    private static final String KEYWORD_SETUP = "setup", KEYWORD_ROOT = "root",
            KEYWORD_USERNAME = "username";

    // DEFAULTS
    private static final int[] TECHNICAL_SETTINGS_DEFAULTS = { 40, 3, 2 };
    private static final boolean[] FLAGS_DEFAULTS = { false, true, true };

    private static final int[] TECHNICAL_SETTINGS = new int[3];
    private static final boolean[] FLAGS = new boolean[3];

    // System settings
    private static boolean setUp = false;
    private static String rootFilepath = "";
    private static FFDirectory rootDirectory;
    private static String username = "Jordan";

    public static String getUsername() {
        return username;
    }

    public static String getRootFilepath() {
        return rootFilepath;
    }

    public static FFDirectory getRootDirectory() {
        return rootDirectory;
    }

    public static int getLessonIntroLimit() {
        return TECHNICAL_SETTINGS[LESSON_INTRO_LIMIT];
    }

    public static int getLessonCounterNew() {
        return TECHNICAL_SETTINGS[LESSON_COUNTER_NEW];
    }

    public static int getLessonCounterReview() {
        return TECHNICAL_SETTINGS[LESSON_COUNTER_REVIEW];
    }

    public static boolean isMarkForAccents() {
        return FLAGS[MARK_FOR_ACCENTS];
    }

    public static boolean isOptionForMarkingMismatchAsCorrect() {
        return FLAGS[OPTION_TO_MARK_MISMATCH_AS_CORRECT];
    }

    public static boolean isIgnoreBracketed() {
        return FLAGS[IGNORE_BRACKETED];
    }

    public static void saveSettings() throws IOException {
        writeToSettingsFile();
        writeToDirectoryMirrorFile();
    }

    private static void writeToDirectoryMirrorFile() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(DIRECTORY_MIRROR_FILEPATH));

        bw.write(rootDirectory.encode());
        bw.newLine();
        bw.close();
    }

    private static void writeToSettingsFile() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(SETTINGS_FILEPATH));

        bw.write(KEYWORD_SETUP + SETTING_SEPARATOR + setUp + NEW_LINE);
        bw.write(KEYWORD_ROOT + SETTING_SEPARATOR + rootFilepath + NEW_LINE);
        bw.write(KEYWORD_USERNAME + SETTING_SEPARATOR + username + NEW_LINE);
        bw.newLine();

        for (int i = 0; i < TECHNICAL_SETTINGS.length; i++) {
            bw.write(TECHNICAL_KEYWORDS[i] + SETTING_SEPARATOR + TECHNICAL_SETTINGS[i] + NEW_LINE);
        }

        for (int i = 0; i < FLAGS.length; i++) {
            bw.write(FLAGS_KEYWORDS[i] + SETTING_SEPARATOR + FLAGS[i] + NEW_LINE);
        }

        bw.close();
    }

    private static void setup() {
        Scanner sc = new Scanner(System.in);

        // TODO - prompt for root directory
        System.out.println("Root directory:"); // temp
        rootFilepath = sc.nextLine();

        rootDirectory = FFDirectory.createRoot();

        // TODO - prompt for username
        System.out.println("Username:"); // temp
        username = sc.nextLine();

        setTechnicalSettingsToDefaults();
        setFlagsToDefaults();
        setUp = true;
    }

    private static void setFlagsToDefaults() {
        System.arraycopy(FLAGS_DEFAULTS, 0, FLAGS, 0, FLAGS.length);
    }

    private static void setTechnicalSettingsToDefaults() {
        System.arraycopy(TECHNICAL_SETTINGS_DEFAULTS, 0,
                TECHNICAL_SETTINGS, 0, TECHNICAL_SETTINGS.length);
    }

    private static void extractFlags(String l) {
        for (int i = 0; i < FLAGS.length; i++) {
            if (l.startsWith(FLAGS_KEYWORDS[i])) {
                String k = FLAGS_KEYWORDS[i];
                String setting = removeKeywordAndExtractSetting(k, l);
                FLAGS[i] = Boolean.parseBoolean(setting);
            }
        }
    }

    private static void extractTechnicalSettings(String l) {
        for (int i = 0; i < TECHNICAL_SETTINGS.length; i++) {
            if (l.startsWith(TECHNICAL_KEYWORDS[i])) {
                String k = TECHNICAL_KEYWORDS[i];
                String setting = removeKeywordAndExtractSetting(k, l);
                TECHNICAL_SETTINGS[i] = Integer.parseInt(setting);
            }
        }
    }

    private static String removeKeywordAndExtractSetting(String k, String l) {
        return l.substring((k + SETTING_SEPARATOR).length()).trim();
    }

    public static void loadSettings() throws FileNotFoundException, InvalidDirectoryFormatException {
        BufferedReader br = new BufferedReader(
                new FileReader(SETTINGS_FILEPATH));

        for (String line : br.lines().toList()) {
            String l = line.trim();

            if (l.startsWith(KEYWORD_SETUP))
                setUp = Boolean.parseBoolean(
                        removeKeywordAndExtractSetting(KEYWORD_SETUP, l)
                );
            if (l.startsWith(KEYWORD_ROOT))
                rootFilepath = removeKeywordAndExtractSetting(KEYWORD_ROOT, l);
            if (l.startsWith(KEYWORD_USERNAME))
                username = removeKeywordAndExtractSetting(KEYWORD_USERNAME, l);

            extractTechnicalSettings(l);
            extractFlags(l);
        }

        if (setUp && !rootFilepath.equals("")) {
            BufferedReader directoryBR = new BufferedReader(
                    new FileReader(DIRECTORY_MIRROR_FILEPATH));

            StringBuilder sb = new StringBuilder();
            directoryBR.lines().forEach(sb::append);

            rootDirectory = FFDirectory.createRoot();

            DirectoryParser.parse(sb.toString(), rootDirectory);
        } else {
            setup();
        }
    }

    public static void set(String settingID, String value) {
        int v; boolean b;

        // technical settings
        if (settingID.equals(TECHNICAL_KEYWORDS[LESSON_INTRO_LIMIT])) {
            v = Integer.parseInt(value);
            TECHNICAL_SETTINGS[LESSON_INTRO_LIMIT] = v;
        } else if (settingID.equals(TECHNICAL_KEYWORDS[LESSON_COUNTER_NEW])) {
            v = Integer.parseInt(value);
            TECHNICAL_SETTINGS[LESSON_COUNTER_NEW] = v;
        } else if (settingID.equals(TECHNICAL_KEYWORDS[LESSON_COUNTER_REVIEW])) {
            v = Integer.parseInt(value);
            TECHNICAL_SETTINGS[LESSON_COUNTER_REVIEW] = v;

            // flags
        } else if (settingID.equals(FLAGS_KEYWORDS[MARK_FOR_ACCENTS])) {
            b = Boolean.parseBoolean(value);
            FLAGS[MARK_FOR_ACCENTS] = b;
        } else if (settingID.equals(FLAGS_KEYWORDS[OPTION_TO_MARK_MISMATCH_AS_CORRECT])) {
            b = Boolean.parseBoolean(value);
            FLAGS[OPTION_TO_MARK_MISMATCH_AS_CORRECT] = b;
        } else if (settingID.equals(FLAGS_KEYWORDS[IGNORE_BRACKETED])) {
            b = Boolean.parseBoolean(value);
            FLAGS[IGNORE_BRACKETED] = b;

            // other
        } else if (settingID.equals(KEYWORD_USERNAME)) {
            username = value;
        } else {
            ExceptionMessenger.deliver("The argument \"" + settingID +
                    "\" is not a legitimate setting ID.", false,
                    FlashFluencyLogicException.CONSEQUENCE_COMMAND_NOT_EXECUTED);
            return;
        }

        CLIOutput.writeSettingSet(settingID, value);
    }

    public static void printSettings() {
        final String[] OTHER_KEYWORDS = { KEYWORD_USERNAME };
        final String[] OTHER_SETTINGS = { username };

        CLIOutput.writePrintSettings(TECHNICAL_KEYWORDS, TECHNICAL_SETTINGS,
                FLAGS_KEYWORDS, FLAGS, OTHER_KEYWORDS, OTHER_SETTINGS);
    }
}
