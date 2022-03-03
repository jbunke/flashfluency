package com.redsquare.flashfluency.logic;

public enum Pot {
    F, NEW, D, C, B, A;

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
}
