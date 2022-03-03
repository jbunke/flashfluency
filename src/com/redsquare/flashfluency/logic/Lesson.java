package com.redsquare.flashfluency.logic;

import com.redsquare.flashfluency.cli.CLIOutput;
import com.redsquare.flashfluency.cli.ContextManager;
import com.redsquare.flashfluency.cli.ExceptionMessenger;
import com.redsquare.flashfluency.system.exceptions.FFErrorMessages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Lesson {
    private static final String RETIRE_SEQUENCE = "???";

    private final Deck deck;

    private final List<Question> questions;
    private final boolean SR; // spaced repetition

    private Lesson(Deck deck) {
        this.deck = deck;
        // SR 'learn' command lesson constructor
        this.SR = true;
        this.questions = new ArrayList<>();
        setInitialSRQuestions(deck);
    }

    private Lesson(Deck deck, final int NUM_Qs) {
        this.deck = deck;
        // Non-SR 'test' command lesson constructor
        this.SR = false;
        this.questions = new ArrayList<>();
        setInitialTestQuestions(deck, NUM_Qs);
    }

    public static void learn(Deck deck) {
        Lesson l =  new Lesson(deck);
        l.takeLesson();
    }

    public static void testAll(Deck deck) {
        Lesson l =  new Lesson(deck, deck.getNumOfFlashCards());
        l.takeLesson();
    }

    public static void testSubset(Deck deck, final String NUM_Qs) {
        Lesson l = new Lesson(deck, Integer.parseInt(NUM_Qs));
        l.takeLesson();
    }

    private void setInitialTestQuestions(final Deck deck, final int NUM_Qs) {
        Set<FlashCard> toTest = deck.getCardsForTest(NUM_Qs);

        toTest.forEach(x -> {
            x.initializeLessonCounter();
            questions.add(Question.create(x));
        });
    }

    private void setInitialSRQuestions(final Deck deck) {
        Set<FlashCard> toReview = deck.getCardsThatAreDue();

        for (FlashCard r : toReview) {
            r.initializeLessonCounter();
            questions.add(Question.create(r));
        }

        Set<FlashCard> toIntroduce = deck.getCardsToIntroduce(toReview);

        for (FlashCard i : toIntroduce) {
            i.initializeLessonCounter();
            questions.add(Question.create(i));
        }
    }

    private void takeLesson() {
        ContextManager.lessonStarted();
        CLIOutput.writeLessonIntro(this);

        // lesson phase
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String response = q.ask();
            if (response.trim().equals(RETIRE_SEQUENCE)) {
                CLIOutput.writeRetiredLesson();
                break;
            }
            questions.get(i).answer(response, SR);

            if (SR && q.getFlashCard().getLessonCounter() > 0) {
                questions.add(Question.create(q.getFlashCard()));
                CLIOutput.writeCardRepeatNotification(q.getFlashCard().getLessonCounter());
            }
        }

        try {
            ContextManager.lessonFinished();
            deck.saveToFile();
            CLIOutput.writeLessonReview(this);
        } catch (IOException e) {
            ExceptionMessenger.deliver(
                    FFErrorMessages.MESSAGE_FAILED_WRITE_TO_DECK_FILE,
                    false, FFErrorMessages.CONSEQUENCE_DECK_DATA_NOT_SAVED);
        }
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public boolean isSR() {
        return SR;
    }
}