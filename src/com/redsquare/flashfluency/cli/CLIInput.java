package com.redsquare.flashfluency.cli;

import java.util.Scanner;

public class CLIInput {
    private static final Scanner IN = new Scanner(System.in);

    private static final String TYPE_TO_MARK_CORRECT = "c";

    public static String readInput() {
        return IN.nextLine();
    }

    public static void readCommand() {
        String toParse = readInput();
        CommandParser.parse(toParse);
    }

    public static boolean markAsCorrect() {
        String decision = readInput().trim().toLowerCase();
        return decision.equals(TYPE_TO_MARK_CORRECT);
    }

    public static String getTypeToMarkCorrect() {
        return TYPE_TO_MARK_CORRECT.toUpperCase();
    }
}
