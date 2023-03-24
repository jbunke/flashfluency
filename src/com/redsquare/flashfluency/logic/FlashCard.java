package com.redsquare.flashfluency.logic;

import com.redsquare.flashfluency.system.Settings;

import java.time.LocalDate;

public class FlashCard {
    private String clue, answer;
    private final String code;

    private boolean introduced;
    private LocalDate due;

    private Pot pot;
    private int potCounter;

    private int lessonCounter;

    private int correctInTests, attemptedInTests;

    private FlashCard(
            final String clue, final String answer, final boolean introduced,
            final LocalDate due, final Pot pot, final int potCounter,
            final int correctInTests, final int attemptedInTests, final String code
    ) {
        // new flash card constructor
        this.clue = clue;
        this.answer = answer;
        this.code = code;

        this.introduced = introduced;
        this.due = due;
        this.pot = pot;
        this.potCounter = potCounter;

        this.correctInTests = correctInTests;
        this.attemptedInTests = attemptedInTests;

        initializeLessonCounter();
    }

    public static FlashCard createNew(String clue, String answer) {
        return new FlashCard(clue, answer, false, LocalDate.now(),
                Pot.NEW, Pot.NEW.answersForPromotion(),
                0, 0, generateNewCode());
    }

    public static FlashCard fromParsedDeckFile(
            final String clue, final String answer, final boolean introduced,
            final LocalDate due, final Pot pot, final int potCounter,
            final int correctInTests, final int attemptedInTests, final String code
    ) {
        return new FlashCard(clue, answer, introduced, due, pot, potCounter,
                correctInTests, attemptedInTests, code);
    }

    public static String generateNewCode() {
        final int DIGITS = 8;
        final StringBuilder code = new StringBuilder();
        final double NUM_PROB = 0.3;

        for (int i = 0; i < DIGITS; i++) {
            final int spread;
            final char min, max;

            if (MathHelper.p(NUM_PROB)) {
                min = '0';
                max = '9';
            } else {
                min = 'A';
                max = 'Z';
            }

            spread = (max - min) + 1;
            final char digit = (char)(min + MathHelper.boundedRandom(spread));
            code.append(digit);
        }

        return code.toString();
    }

    public void initializeLessonCounter() {
        lessonCounter = introduced ?
                Settings.getLessonCounterReview() :
                Settings.getLessonCounterNew();
    }

    public void adjustFromAnswer(final boolean correct) {
        if (!isIntroduced())
            introduce();

        if (correct)
            correctAdjustment();
        else
            incorrectAdjustment();
    }

    public void updateRecord(final boolean correct) {
        attemptedInTests++;
        if (correct)
            correctInTests++;
    }

    private void correctAdjustment() {
        potCounter--;
        lessonCounter--;
        
        if (potCounter <= 0) {
            pot = pot.promote();
            potCounter = pot.answersForPromotion();
        }

        setDueFromPot();
    }

    private void incorrectAdjustment() {
        pot = pot.demote();
        potCounter = pot.answersForPromotion();
        initializeLessonCounter();
        setDueToday();
    }

    public String getClue() {
        return clue;
    }

    public String getAnswer() {
        return answer;
    }

    public String getCode() {
        return code;
    }

    public void setClue(final String clue) {
        this.clue = clue;
    }

    public void setAnswer(final String answer) {
        this.answer = answer;
    }

    public boolean isIntroduced() {
        return introduced;
    }

    public void introduce() {
        introduced = true;
    }

    public void setDueToday() {
        due = LocalDate.of(LocalDate.now().getYear(),
                LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
    }

    public void setDueFromPot() {
        due = LocalDate.now().plusDays(pot.daysDue());
    }

    public void reset() {
        introduced = false;
        pot = Pot.NEW;
        potCounter = pot.answersForPromotion();

        setDueToday();
    }

    public LocalDate getDue() {
        return due;
    }

    public boolean isDue() {
        return due.isBefore(LocalDate.now()) || due.isEqual(LocalDate.now());
    }

    public Pot getPot() {
        return pot;
    }

    public int getPotCounter() {
        return potCounter;
    }

    public int getLessonCounter() {
        return lessonCounter;
    }

    public int getCorrectInTests() {
        return correctInTests;
    }

    public int getAttemptedInTests() {
        return attemptedInTests;
    }

    public int getRecordPercentage() {
        if (attemptedInTests == 0)
            return 0;
        else
            return (int)(100 * (correctInTests / (float)attemptedInTests));
    }

    @Override
    public String toString() {
        return "[ " + clue + " ] / [ " + answer + " ]";
    }
}
