package com.redsquare.flashfluency.logic;

public enum Pot {
    F, NEW, D, C, B, A;

    public static final int MAX_SCORE = 6;

    public int answersForPromotion() {
        return switch (this) {
            case A -> -1;
            case B, C -> 3;
            case D -> 4;
            case F -> 5;
            case NEW -> 1;
        };
    }

    public Pot promote() {
        return switch (this) {
            case A, B -> A;
            case C -> B;
            case D -> C;
            case F, NEW -> D;
        };
    }

    public Pot demote() {
        return switch (this) {
            case A -> B;
            case B -> C;
            case C -> D;
            case NEW, D, F -> F;
        };
    }

    public long daysDue() {
        return switch (this) {
            case F -> 1L;
            case NEW -> 0L;
            case D -> 2L;
            case C -> 4L;
            case B -> 8L;
            case A -> 16L;
        };
    }

    public int getScore() {
        return switch (this) {
            case F, NEW -> 0;
            case D -> MAX_SCORE - 3;
            case C -> MAX_SCORE - 2;
            case B -> MAX_SCORE - 1;
            case A -> MAX_SCORE;
        };
    }
}
