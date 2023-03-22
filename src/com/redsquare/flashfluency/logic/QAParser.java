package com.redsquare.flashfluency.logic;

import com.redsquare.flashfluency.system.Settings;

import java.util.*;

public class QAParser {
    private static final String EMPTY = "",
            TERM_SEPARATOR_REGEX = "\\|", TERM_SEPARATOR = "|",
            OPEN_BRACKET = "(", CLOSE_BRACKET = ")",
            OPEN_SQUARE = "[", CLOSE_SQUARE = "]",
            OPEN_CURLY = "{", CLOSE_CURLY = "}";
    private static final int NOT_FOUND = -1;

    private static char baseGlyph(final char toConvert) {
        return switch (toConvert) {
            case 'ã', 'á', 'â', 'à', 'ä', 'a' -> 'a';
            case 'ç', 'č', 'ć' -> 'c';
            case 'đ' -> 'd';
            case 'é', 'è', 'ê', 'ë', 'ě', 'ẹ' -> 'e';
            case 'ğ' -> 'g';
            case 'í', 'ì', 'î', 'ï', 'ı' -> 'i';
            case 'ñ' -> 'n';
            case 'ó', 'ò', 'õ', 'ô', 'ö', 'ő', 'ọ' -> 'o';
            case 'ř' -> 'r';
            case 'š', 'ş', 'ṣ' -> 's';
            case 'ü', 'ú', 'ù', 'ů', 'ű' -> 'u';
            case 'ý' -> 'y';
            case 'ž' -> 'z';
            default -> toConvert;
        };
    }
    
    private static String convertToUnaccented(final String toConvert) {
        String s = toConvert.trim().toLowerCase();
        StringBuilder sb = new StringBuilder();

        for (char c : s.toCharArray())
            sb.append(baseGlyph(c));

        return sb.toString();
    }

    public static Set<String> validOptionsForQADefinition(final String def) {
        final Set<String> validDefinitionMatches = new HashSet<>();

        final String[] terms = separateQATerms(
                removeAnnotations(def));

        for (String term : terms) {
            final Set<String> validTermMatches =
                    validOptionsForQATerm(term);
            validDefinitionMatches.addAll(validTermMatches);
        }

        return validDefinitionMatches;
    }

    private static Set<String> validOptionsForQATerm(final String term) {
        final Set<String> choicePermutations = choicePermutationsForAQATerm(term);

        final Set<String> validOptions = new HashSet<>();

        for (String choicePermutation : choicePermutations) {
            final Set<String> optionalPermutations =
                    optionalPermutationsForAQAChoice(choicePermutation);

            for (String optionalPermutation : optionalPermutations)
                validOptions.add(optionalPermutation.trim()); // final trim
        }

        return validOptions;
    }

    private static Set<String> choicePermutationsForAQATerm(final String term) {
        // step 1: define sections and choices for each section
        final List<String[]> sections = new ArrayList<>();

        String unprocessed = term.trim();

        while (!unprocessed.isEmpty()) {
            final int openIndex = unprocessed.indexOf(OPEN_CURLY);
            final int closeIndex = unprocessed.indexOf(CLOSE_CURLY);

            // case 1: no opener, indicating there is no choice to be made
            if (openIndex == NOT_FOUND) {
                sections.add(new String[] { unprocessed });
                unprocessed = EMPTY;
            }
            // case 2: valid
            else if (openIndex < closeIndex) {
                if (openIndex > 0)
                    sections.add(new String[] { unprocessed.substring(0, openIndex) });

                final String[] choices =
                        separate(unprocessed.substring(openIndex + OPEN_CURLY.length(), closeIndex));

                sections.add(choices);

                unprocessed = unprocessed.substring(closeIndex + CLOSE_CURLY.length());
            }
            // case 3: invalid format - just accept remaining
            else {
                sections.add(new String[] { unprocessed });
                unprocessed = EMPTY;
            }
        }

        // step 2: go through and define permutation of choices
        final Set<String> choicePermutations = new HashSet<>();
        generatePermutations(sections, choicePermutations, 0, EMPTY);

        return choicePermutations;
    }

    private static void generatePermutations(
            final List<String[]> sections, final Set<String> toPopulate,
            final int sectionIndex, final String soFar
    ) {
        if (sectionIndex >= sections.size())
            toPopulate.add(soFar);
        else {
            final String[] sectionChoices = sections.get(sectionIndex);

            for (String choice : sectionChoices)
                generatePermutations(sections, toPopulate, sectionIndex + 1, soFar + choice);
        }
    }

    private static Set<String> optionalPermutationsForAQAChoice(final String choice) {
        // step 1:
        Set<String> permutations = new HashSet<>();

        String unprocessed = choice.trim();

        while (!unprocessed.isEmpty()) {
            final int openIndex = unprocessed.indexOf(OPEN_BRACKET);
            final int closeIndex = unprocessed.indexOf(CLOSE_BRACKET);

            // case 1: no opener, indicating there is no optional substring
            if (openIndex == NOT_FOUND) {
                if (permutations.isEmpty())
                    permutations.add(unprocessed);
                else {
                    final Set<String> updatedPermutations = new HashSet<>();

                    for (String permutation : permutations)
                        updatedPermutations.add(permutation + unprocessed);

                    permutations = updatedPermutations;
                }
                unprocessed = EMPTY;
            }
            // case 2: valid
            else if (openIndex < closeIndex) {
                final String beforeOpen = unprocessed.substring(0, openIndex);
                final String optionalSection =
                        unprocessed.substring(openIndex + OPEN_BRACKET.length(), closeIndex);

                if (permutations.isEmpty())
                    permutations.add(EMPTY);

                final Set<String> updatedPermutations = new HashSet<>();

                for (String permutation : permutations) {
                    if (Settings.isIgnoringBracketed())
                        updatedPermutations.add(permutation + beforeOpen);

                    updatedPermutations.add(permutation + beforeOpen + optionalSection);
                }

                permutations = updatedPermutations;
                unprocessed =
                        unprocessed.substring(closeIndex + CLOSE_BRACKET.length());
            }
            // case 3: remaining
            else {
                if (permutations.isEmpty())
                    permutations.add(unprocessed);
                else {
                    final Set<String> updatedPermutations = new HashSet<>();

                    for (String permutation : permutations)
                        updatedPermutations.add(permutation + unprocessed);

                    permutations = updatedPermutations;
                }
                unprocessed = EMPTY;
            }
        }

        return permutations;
    }

    private static String[] separateQATerms(final String def) {
        if (!(def.contains(OPEN_CURLY) || def.contains(CLOSE_CURLY)))
            return separate(def);

        String unprocessed = def;
        int termCounter = 0;
        final String[] provisionalTerms = new String[unprocessed.length()];

        while (!unprocessed.isEmpty()) {
            final int separatorIndex = unprocessed.indexOf(TERM_SEPARATOR);
            final int openIndex = unprocessed.indexOf(OPEN_CURLY);
            // final int closeIndex = unprocessed.indexOf(CLOSE_CURLY);

            // case 1: no term separator - whole unprocessed string is last term
            if (separatorIndex == NOT_FOUND) {
                provisionalTerms[termCounter] = unprocessed;
                unprocessed = EMPTY;
            }
            // case 2: term separator but no open square bracket,
            // or open square bracket occurs after first term separator
            else if (openIndex == NOT_FOUND || (separatorIndex < openIndex)) {
                final String term = unprocessed.substring(0, separatorIndex);
                provisionalTerms[termCounter] = term;
                unprocessed = unprocessed.substring(separatorIndex + TERM_SEPARATOR.length());
            }
            // case 3: build term character by character based on whether it is enclosed
            else {
                int enclosureLevel = 0;
                StringBuilder tb = new StringBuilder();

                for (char c : unprocessed.toCharArray()) {
                    final String chAsString = String.valueOf(c);

                    switch (chAsString) {
                        case OPEN_CURLY -> {
                            enclosureLevel++;
                            tb.append(chAsString);
                        }
                        case CLOSE_CURLY -> {
                            enclosureLevel--;
                            tb.append(chAsString);
                        }
                        case TERM_SEPARATOR -> {
                            if (enclosureLevel == 0) {
                                final String term = tb.toString();
                                tb = new StringBuilder();

                                provisionalTerms[termCounter] = term;
                                termCounter++;

                                unprocessed = unprocessed.substring(term.length() + TERM_SEPARATOR.length());
                            } else
                                tb.append(chAsString);
                        }
                        default -> tb.append(chAsString);
                    }
                }

                if (!unprocessed.equals(EMPTY) && unprocessed.equals(tb.toString())) {
                    provisionalTerms[termCounter] = unprocessed;
                    unprocessed = EMPTY;
                } else
                    termCounter--;
            }

            termCounter++;
        }

        final String[] terms = new String[termCounter];

        System.arraycopy(provisionalTerms, 0, terms, 0, terms.length);

        return terms;
    }

    private static String[] separate(final String s) {
        String[] separated = s.split(TERM_SEPARATOR_REGEX);

        for (int i = 0; i < separated.length; i++)
            separated[i] = separated[i].trim();

        return separated;
    }

    public static String removeBrackets(final String toConvert) {
        return toConvert.replace(OPEN_BRACKET, EMPTY).replace(CLOSE_BRACKET, EMPTY).trim();
    }

    private static String removeAnnotations(final String toConvert) {
        String s = toConvert.trim();

        while (s.contains(QAParser.OPEN_SQUARE)) {
            int openIndex = s.indexOf(QAParser.OPEN_SQUARE);
            int closeIndex = s.indexOf(QAParser.CLOSE_SQUARE);

            if (openIndex == NOT_FOUND || closeIndex == NOT_FOUND)
                break;

            s = s.substring(0, openIndex) + s.substring(closeIndex + 1);
        }

        return s.trim();
    }

    private static boolean equal(final String correctAnswer, final String response) {
        return correctAnswer.equals(response) ||
                removeBrackets(correctAnswer).equals(removeBrackets(response));
    }

    private static boolean equalIgnoringAccents(final String correctAnswer,
                                                 final String response) {
        return convertToUnaccented(correctAnswer).equals(convertToUnaccented(response));
    }

    public static Optional<String> isCorrect(
            final String correctAnswerDefinition, final String response,
            final boolean strict
    ) {
        final Set<String> validCorrectAnswers =
                validOptionsForQADefinition(correctAnswerDefinition);

        for (String validCorrectAnswer : validCorrectAnswers) {
            if (equal(validCorrectAnswer, response) ||
                    (!strict && equalIgnoringAccents(validCorrectAnswer, response)))
                return Optional.of(validCorrectAnswer);
        }

        return Optional.empty();
    }
}
