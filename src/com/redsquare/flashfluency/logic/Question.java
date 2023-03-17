package com.redsquare.flashfluency.logic;

import com.redsquare.flashfluency.cli.CLIInput;
import com.redsquare.flashfluency.cli.CLIOutput;
import com.redsquare.flashfluency.cli.ExceptionMessenger;
import com.redsquare.flashfluency.system.Settings;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;

public class Question {
    private final FlashCard flashCard;
    private boolean answered;
    private boolean correct;

    private Question(final FlashCard flashCard) {
        this.flashCard = flashCard;
        this.answered = false;
        this.correct = false;
    }

    public static Question create(final FlashCard flashCard) {
        return new Question(flashCard);
    }

    public String ask() {
        CLIOutput.writeFlashCardClue(fetchClue());
        CLIOutput.writeFlashCardAnswerPrompt();
        Lesson.setAskTime();
        return CLIInput.readInput();
    }

    public void answer(final String response, final boolean SR, final int elapsedTime) {
        final String correctAnswer = fetchAnswer();
        final boolean tookTooLongToAnswer = elapsedTime >= Settings.getSecondsTimeout();

        final boolean timedOut =
                Settings.isInTimedMode() && tookTooLongToAnswer;
        final boolean isStrictlyCorrect =
                MarkerHelper.isCorrectMarkingForAccents(correctAnswer, response);
        final boolean isCorrectWithConcessions =
                Settings.isNotMarkingForAccents() &&
                MarkerHelper.isCorrectNotMarkingForAccents(correctAnswer, response);

        final boolean initiallyMarkAsCorrect =
                !timedOut && (isStrictlyCorrect || isCorrectWithConcessions);
        CLIOutput.writeQuestionFeedback(
                initiallyMarkAsCorrect,
                timedOut, isStrictlyCorrect, isCorrectWithConcessions,
                correctAnswer, elapsedTime
        );

        boolean overrideMarkAsCorrect = false;
        if (!initiallyMarkAsCorrect && Settings.isOptionForMarkingMismatchAsCorrect()) {
            CLIOutput.writeOptionToMarkCorrectPrompt();
            overrideMarkAsCorrect = CLIInput.markAsCorrect();
        }

        final boolean correct = initiallyMarkAsCorrect || overrideMarkAsCorrect;
        mark(correct, SR);
    }

    private String fetchClue() {
        return MarkerHelper.removeBrackets(
                Settings.isInReverseMode()
                        ? flashCard.getAnswer()
                        : flashCard.getClue()
        );
    }

    private String fetchAnswer() {
        return Settings.isInReverseMode()
                ? flashCard.getClue()
                : flashCard.getAnswer();
    }

    private void mark(final boolean correct, final boolean SR) {
        try {
            if (isAnswered())
                throw FlashFluencyLogicException.questionHasAlreadyBeenAnswered();

            this.answered = true;
            this.correct = correct;

            if (SR)
                flashCard.adjustFromAnswer(correct);
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    public FlashCard getFlashCard() {
        return flashCard;
    }

    public boolean isAnswered() {
        return answered;
    }

    public boolean isCorrect() {
        return correct;
    }
}
