package com.redsquare.flashfluency.logic;

public class MarkerHelper {
    private static char baseGlyph(final char toConvert) {
        return switch (toConvert) {
            case 'ã', 'á', 'â', 'à', 'ä', 'a' -> 'a';
            case 'ç', 'č', 'ć' -> 'c';
            case 'đ' -> 'd';
            case 'é', 'è', 'ê', 'ë', 'ě' -> 'e';
            case 'ğ' -> 'g';
            case 'í', 'ì', 'î', 'ï', 'ı' -> 'i';
            case 'ñ' -> 'n';
            case 'ó', 'ò', 'õ', 'ô', 'ö', 'ő' -> 'o';
            case 'ř' -> 'r';
            case 'š', 'ş' -> 's';
            case 'ü', 'ú', 'ů', 'ű' -> 'u';
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

    public static boolean correctIgnoringAccents(final String correctAnswer,
                                                 final String response) {
        return convertToUnaccented(correctAnswer).equals(convertToUnaccented(response));
    }

    public static boolean correctIgnoringBracketedTerms(final String correctAnswer, final String response) {
        return removeBracketedTerms(correctAnswer).equals(response);
    }
}
