package com.redsquare.flashfluency.system;

import com.redsquare.flashfluency.system.exceptions.InvalidDirectoryFormatException;

public class DirectoryParser {
    public static final String DIR_MARKER = "->", SEPARATOR = ",",
            SCOPE_OPENER = "[", SCOPE_CLOSER = "]", NAME_BOUND = "\"";

    public static void parse(final String l, final FFDirectory root) throws InvalidDirectoryFormatException {
        String shouldStartWith = NAME_BOUND + Settings.ROOT_CODE +
                NAME_BOUND + DIR_MARKER + SCOPE_OPENER;

        if (!l.startsWith(shouldStartWith))
            throw InvalidDirectoryFormatException.rootLabelMissing();

        FFDirectory scope = root;
        String toProcess = l.substring(shouldStartWith.length());
        StringBuilder processed = new StringBuilder(shouldStartWith);

        while (!toProcess.equals("") && scope != null) {
            if (toProcess.startsWith(NAME_BOUND)) {
                String name = extractName(toProcess.substring(1));

                toProcess = process(toProcess,
                        NAME_BOUND + name + NAME_BOUND, processed);

                if (toProcess.startsWith(DIR_MARKER + SCOPE_OPENER)) {
                    scope = scope.addChildDirectoryR(name);
                    toProcess = process(toProcess,
                            DIR_MARKER + SCOPE_OPENER, processed);
                } else if (toProcess.startsWith(SEPARATOR)) {
                    FFDeckFile deckFile = scope.addDeckR(name);
                    DeckFileParser.parse(deckFile);

                    toProcess = process(toProcess, SEPARATOR, processed);
                } else if (toProcess.startsWith(SCOPE_CLOSER)) {
                    FFDeckFile deckFile = scope.addDeckR(name);
                    DeckFileParser.parse(deckFile);

                    scope = scope.getParent();
                    toProcess = process(toProcess, SCOPE_CLOSER, processed);
                } else {
                    throw InvalidDirectoryFormatException.parserQuitPrematurely(toProcess);
                }
            } else if (toProcess.startsWith(SCOPE_CLOSER)) {
                scope = scope.getParent();
                toProcess = process(toProcess, SCOPE_CLOSER, processed);
            } else if (toProcess.startsWith(SEPARATOR)) {
                toProcess = process(toProcess, SEPARATOR, processed);
            } else {
                throw InvalidDirectoryFormatException.parserQuitPrematurely(toProcess);
            }
        }
    }

    private static String process(String toProcess, String processing, StringBuilder processed) {
        processed.append(processing);
        return toProcess.substring(processing.length());
    }

    private static String extractName(final String l) throws InvalidDirectoryFormatException {
        int nameBoundIndex = l.indexOf(NAME_BOUND);

        if (nameBoundIndex == -1)
            throw InvalidDirectoryFormatException.unevenNumberOfQuotes();
        else if (nameBoundIndex == 0)
            throw InvalidDirectoryFormatException.emptyQuotesName();

        return l.substring(0, l.indexOf(NAME_BOUND));
    }
}
