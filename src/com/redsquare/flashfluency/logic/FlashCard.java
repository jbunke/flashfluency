package com.redsquare.flashfluency.logic;

import com.redsquare.flashfluency.system.Settings;

import java.time.LocalDate;

public class FlashCard {
    private final String clue;
    private final String answer;

    private boolean introduced;
    private LocalDate due;

    private Pot pot;
    private int potCounter;

    private int lessonCounter;

    private FlashCard(String clue, String answer, boolean introduced, LocalDate due, Pot pot, int potCounter) {
        // new flash card constructor
        this.clue = clue;
        this.answer = answer;

        this.introduced = introduced;
        this.due = due;
        this.pot = pot;
        this.potCounter = potCounter;

        initializeLessonCounter();
    }

    public static FlashCard createNew(String clue, String answer) {
        return new FlashCard(clue, answer, false, LocalDate.now(),
                Pot.NEW, Pot.NEW.answersForPromotion());
    }

    public static FlashCard fromParsedDeckFile(String clue, String answer, boolean introduced,
                                               LocalDate due, Pot pot, int potCounter) {
        return new FlashCard(clue, answer, introduced, due, pot, potCounter);
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

    @Override
    public String toString() {
        return "[ " + clue + " ] / [ " + answer + " ]";
    }
}
