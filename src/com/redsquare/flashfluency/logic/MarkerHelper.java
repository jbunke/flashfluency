package com.redsquare.flashfluency.logic;

import com.redsquare.flashfluency.system.Settings;

import java.util.function.BiFunction;

public class MarkerHelper {
    private static final String TERM_SEPARATOR = "\\|";

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

    private static String[] separateAnswerTerms(final String correctAnswer) {
        return correctAnswer.split(TERM_SEPARATOR);
    }

    public static String removeBrackets(final String toConvert) {
        return toConvert.replace("(", "").replace(")","").trim();
    }

    private static String removeBracketedTerms(final String toConvert) {
        String s = toConvert.trim();

        while (s.contains("(")) {
            int openIndex = s.indexOf('(');
            int closeIndex = s.indexOf(')');

            if (openIndex == -1 || closeIndex == -1)
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

    private static boolean equalIgnoringBracketedTerms(final String correctAnswer,
                                                       final String response) {
        return removeBracketedTerms(correctAnswer).equals(response);
    }

    private static boolean isCorrect(final String correctAnswer, final String response,
                                     final BiFunction<String, String, Boolean> f) {
        String[] correctTerms = separateAnswerTerms(correctAnswer);
        boolean isCorrect = f.apply(correctAnswer, response);

        if (Settings.isIgnoringBracketed())
            isCorrect |= equalIgnoringBracketedTerms(correctAnswer, response);

        for (String correctTerm : correctTerms) {
            correctTerm = correctTerm.trim();
            isCorrect |= f.apply(correctTerm, response);

            if (Settings.isIgnoringBracketed())
                isCorrect |= equalIgnoringBracketedTerms(correctTerm, response);
        }

        return isCorrect;
    }

    public static boolean isCorrectMarkingForAccents(final String correctAnswer, final String response) {
        return isCorrect(correctAnswer, response, MarkerHelper::equal);
    }

    public static boolean isCorrectNotMarkingForAccents(final String correctAnswer, final String response) {
        return isCorrect(correctAnswer, response, MarkerHelper::equalIgnoringAccents);
    }
}
