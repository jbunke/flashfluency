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
        CLIOutput.writeFlashCardClue(flashCard.getClue());
        CLIOutput.writeFlashCardAnswerPrompt();
        return CLIInput.readInput();
    }

    public void answer(final String response, final boolean SR) {
        final String correctAnswer = flashCard.getAnswer();

        if (MarkerHelper.isCorrectMarkingForAccents(correctAnswer, response)) {
            CLIOutput.writeCorrectAnswer();
            mark(true, SR);
        } else if (!Settings.isMarkingForAccents() &&
                MarkerHelper.isCorrectNotMarkingForAccents(correctAnswer, response)) {
            CLIOutput.writeCorrectAnswerAccentDiscrepancy(correctAnswer);
            mark(true, SR);
        } else if (Settings.isOptionForMarkingMismatchAsCorrect()) {
            CLIOutput.writeWrongAnswerWithOptionToMarkCorrect(correctAnswer);
            mark(CLIInput.markAsCorrect(), SR);
        } else {
            CLIOutput.writeWrongAnswer(correctAnswer);
            mark(false, SR);
        }
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
