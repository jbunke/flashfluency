package com.redsquare.flashfluency.system;

import com.redsquare.flashfluency.cli.CLIInput;
import com.redsquare.flashfluency.cli.CLIOutput;
import com.redsquare.flashfluency.cli.ExceptionMessenger;
import com.redsquare.flashfluency.logic.Deck;
import com.redsquare.flashfluency.system.exceptions.FFErrorMessages;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;
import com.redsquare.flashfluency.system.exceptions.InvalidDirectoryFormatException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Settings {
    public static final String ROOT_CODE = "(root)";
    public static final String DECK_FILE_EXTENSION = ".flfl";
    public static final String SETTING_SEPARATOR = ":", NEW_LINE = "\n";

    // resource file locations
    // private static final String SETTINGS_FILEPATH = "resources/settings/settings.txt";
    private static final String THIS_DIR_FP = ".";
    private static final String RESOURCES_FP = "resources";
    private static final String SETTINGS_FP = "settings";
    private static final String SETTINGS_FILENAME = "settings.txt";
    // private static final String DIRECTORY_MIRROR_FILEPATH = "resources/settings/directory_mirror.txt";
    private static final String DIRECTORY_MIRROR_FILENAME = "directory_mirror.txt";

    // indices
    private static final int LESSON_INTRO_LIMIT = 0,
            LESSON_COUNTER_NEW = 1, LESSON_COUNTER_REVIEW = 2,
            SECONDS_TIMEOUT = 3, NUM_TECHNICAL_SETTINGS = 4;
    private static final int MARK_FOR_ACCENTS = 0,
            OPTION_TO_MARK_MISMATCH_AS_CORRECT = 1, IGNORE_BRACKETED = 2,
            REVERSE_MODE = 3, TIMED_MODE = 4, NUM_FLAGS = 5;

    // KEYWORDS
    private static final String[] TECHNICAL_KEYWORDS =
            { "lesson_introduction_limit", "lesson_counter_new",
                    "lesson_counter_review", "seconds_timeout" };
    private static final String[] FLAGS_KEYWORDS =
            { "mark_for_accents", "option_to_mark_mismatch_as_correct",
                    "ignore_bracketed", "reverse_mode", "timed_mode" };
    private static final String KEYWORD_SETUP = "setup", KEYWORD_ROOT = "root",
            KEYWORD_USERNAME = "username";

    // DEFAULTS
    private static final int[] TECHNICAL_SETTINGS_DEFAULTS = { 40, 3, 2, 30 };
    private static final boolean[] FLAGS_DEFAULTS = { false, true, true, false, false };

    private static final int[] TECHNICAL_SETTINGS = new int[NUM_TECHNICAL_SETTINGS];
    private static final boolean[] FLAGS = new boolean[NUM_FLAGS];

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

    public static int getSecondsTimeout() {
        return TECHNICAL_SETTINGS[SECONDS_TIMEOUT];
    }

    public static boolean isNotMarkingForAccents() {
        return !FLAGS[MARK_FOR_ACCENTS];
    }

    public static boolean isOptionForMarkingMismatchAsCorrect() {
        return FLAGS[OPTION_TO_MARK_MISMATCH_AS_CORRECT];
    }

    public static boolean isIgnoringBracketed() {
        return FLAGS[IGNORE_BRACKETED];
    }

    public static boolean isInReverseMode() {
        return FLAGS[REVERSE_MODE];
    }

    public static boolean isInTimedMode() {
        return FLAGS[TIMED_MODE];
    }

    public static void save() throws IOException {
        writeToSettingsFile();
        writeToDirectoryMirrorFile();
    }

    private static void writeToDirectoryMirrorFile() throws IOException {
        Path path = FileSystems.getDefault().getPath(THIS_DIR_FP,
                RESOURCES_FP, SETTINGS_FP, DIRECTORY_MIRROR_FILENAME);
        // BufferedWriter bw = new BufferedWriter(new FileWriter(DIRECTORY_MIRROR_FILEPATH));
        BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

        bw.write(rootDirectory.encode(0));
        bw.newLine();
        bw.close();
    }

    private static void writeToSettingsFile() throws IOException {
        Path path = FileSystems.getDefault().getPath(THIS_DIR_FP,
                RESOURCES_FP, SETTINGS_FP, SETTINGS_FILENAME);
        // BufferedWriter bw = new BufferedWriter(new FileWriter(SETTINGS_FILEPATH));
        BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

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
        CLIOutput.writeSetRootDirectoryPrompt();
        rootFilepath = CLIInput.readInput();

        rootDirectory = FFDirectory.createRoot();

        CLIOutput.writeSetUsernamePrompt();
        username = CLIInput.readInput();

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

    public static void loadSettings() throws InvalidDirectoryFormatException {
        Path path = FileSystems.getDefault().getPath(THIS_DIR_FP,
                RESOURCES_FP, SETTINGS_FP, SETTINGS_FILENAME);
        // File settingsFile = new File(SETTINGS_FILEPATH);

        try {
            if (path.toFile().exists()) {
                BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8);
                populateSettings(br.lines().toList());
            } else {
                setup();
                // if (path.toFile().createNewFile())
                writeToSettingsFile();
            }
        } catch (FileNotFoundException e) {
            ExceptionMessenger.deliver(FFErrorMessages.MESSAGE_FAILED_TO_READ_FROM_SETTINGS,
                    false, FFErrorMessages.CONSEQUENCE_COULD_NOT_SAVE);
        } catch (IOException e) {
            ExceptionMessenger.deliver(FFErrorMessages.MESSAGE_FAILED_TO_WRITE_TO_SETTINGS,
                    false, FFErrorMessages.CONSEQUENCE_COULD_NOT_SAVE);
        }
    }

    public static void loadDirectory() {
        Path path = FileSystems.getDefault().getPath(THIS_DIR_FP,
                RESOURCES_FP, SETTINGS_FP, DIRECTORY_MIRROR_FILENAME);
        // File directoryMirrorFile = new File(DIRECTORY_MIRROR_FILEPATH);

        try {
            if (setUp && !rootFilepath.equals("") && path.toFile().exists()) {
                // BufferedReader directoryBR = new BufferedReader(new FileReader(directoryMirrorFile));
                BufferedReader directoryBR = Files.newBufferedReader(path, StandardCharsets.UTF_8);

                StringBuilder sb = new StringBuilder();
                directoryBR.lines().forEach(x -> sb.append(x.trim()));

                parseDirectoryMirror(sb.toString());
            } else {
                if (!setUp)
                    setup();

                // if (!path.toFile().exists() && path.toFile().createNewFile())
                writeToDirectoryMirrorFile();
            }
        } catch (FileNotFoundException e) {
            ExceptionMessenger.deliver(FFErrorMessages.MESSAGE_FAILED_TO_READ_FROM_DIR_MIRROR,
                    false, FFErrorMessages.CONSEQUENCE_COULD_NOT_SAVE);
        } catch (IOException e) {
            ExceptionMessenger.deliver(FFErrorMessages.MESSAGE_FAILED_TO_WRITE_TO_DIR_MIRROR,
                    false, FFErrorMessages.CONSEQUENCE_COULD_NOT_SAVE);
        }
    }

    private static void populateSettings(final List<String> lines) {
        // ensures that settings not listed in file are set to defaults on startup
        setTechnicalSettingsToDefaults();
        setFlagsToDefaults();

        for (String line : lines) {
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
    }

    private static void parseDirectoryMirror(final String line) {
        try {
            rootDirectory = FFDirectory.createRoot();
            DirectoryParser.parse(line, rootDirectory);
        } catch (InvalidDirectoryFormatException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    public static void set(String settingID, String value) {
        // int v; boolean b;

        boolean matchFound = false;

        // technical settings
        for (int i = 0; !matchFound && i < TECHNICAL_SETTINGS.length; i++)
            if (settingID.equals(TECHNICAL_KEYWORDS[i])) {
                int v = Integer.parseInt(value);
                TECHNICAL_SETTINGS[i] = v;
                matchFound = true;
            }

        // flags
        for (int i = 0; !matchFound && i < FLAGS.length; i++)
            if (settingID.equals(FLAGS_KEYWORDS[i])) {
                boolean b = Boolean.parseBoolean(value);
                FLAGS[i] = b;
                matchFound = true;
            }

        // other
        if (!matchFound) {
            if (settingID.equals(KEYWORD_USERNAME)) {
                username = value;
            } // extensible with else-ifs here
        }

        if (matchFound)
            CLIOutput.writeSettingSet(settingID, value, true, true);
        else
            ExceptionMessenger.deliver("The argument \"" + settingID +
                            "\" is not a legitimate setting ID.", false,
                    FlashFluencyLogicException.CONSEQUENCE_COMMAND_NOT_EXECUTED);
    }

    public static void printSettings() {
        final String[] OTHER_KEYWORDS = { KEYWORD_USERNAME };
        final String[] OTHER_SETTINGS = { username };

        CLIOutput.writePrintSettings(TECHNICAL_KEYWORDS, TECHNICAL_SETTINGS,
                FLAGS_KEYWORDS, FLAGS, OTHER_KEYWORDS, OTHER_SETTINGS);
    }

    public static void irreversibleDeckSettingsUpdate(final boolean isSR) {
        FLAGS[REVERSE_MODE] = false;

        settingsUpdateNotification(
                Deck.TAG_IRREVERSIBLE,
                new String[] { FLAGS_KEYWORDS[REVERSE_MODE] },
                new String[] { "false" },
                isSR
        );
    }

    public static void strictDeckSettingsUpdate(final boolean isSR) {
        FLAGS[MARK_FOR_ACCENTS] = true;
        FLAGS[OPTION_TO_MARK_MISMATCH_AS_CORRECT] = false;
        FLAGS[IGNORE_BRACKETED] = false;

        settingsUpdateNotification(
                Deck.TAG_STRICT,
                new String[] {
                        FLAGS_KEYWORDS[MARK_FOR_ACCENTS], FLAGS_KEYWORDS[IGNORE_BRACKETED],
                        FLAGS_KEYWORDS[OPTION_TO_MARK_MISMATCH_AS_CORRECT]
                },
                new String[] {
                        "true", "false", "false"
                },
                isSR
        );
    }

    private static void settingsUpdateNotification(
            final String reason, final String[] settingIDs, final String[] values,
            final boolean isSR
    ) {
        if (settingIDs.length != values.length) {
            ExceptionMessenger.deliver(
                    FlashFluencyLogicException.numberOfUpdatedSettingsDoesNotMatchExpected(
                            settingIDs.length, values.length, isSR
                    )
            );
        }

        CLIOutput.writeSettingUpdateNotification(reason);

        for (int i = 0; i < settingIDs.length; i++)
            CLIOutput.writeSettingSet(settingIDs[i], values[i],
                    i == 0, false);
    }
}
