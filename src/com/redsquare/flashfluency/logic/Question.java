package com.redsquare.flashfluency.logic;

import com.redsquare.flashfluency.cli.CLIInput;
import com.redsquare.flashfluency.cli.CLIOutput;
import com.redsquare.flashfluency.cli.ExceptionMessenger;
import com.redsquare.flashfluency.system.Settings;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;

import java.util.Optional;
import java.util.Set;

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
        final String correctAnswerDefinition = fetchAnswerDefinition();
        final boolean tookTooLongToAnswer = elapsedTime >= Settings.getSecondsTimeout();

        final boolean timedOut =
                Settings.isInTimedMode() && tookTooLongToAnswer;
        final Optional<String> isStrictlyCorrect =
                QAParser.isCorrect(correctAnswerDefinition, response, true);
        final Optional<String> isCorrectWithConcessions = Settings.isNotMarkingForAccents()
                ? QAParser.isCorrect(correctAnswerDefinition, response, false)
                : Optional.empty();

        final boolean initiallyMarkAsCorrect =
                !timedOut && (isStrictlyCorrect.isPresent() ||
                                isCorrectWithConcessions.isPresent());
        CLIOutput.writeQuestionFeedback(
                initiallyMarkAsCorrect,
                timedOut, isStrictlyCorrect, isCorrectWithConcessions,
                QAParser.validOptionsForQADefinition(correctAnswerDefinition),
                elapsedTime);

        boolean overrideMarkAsCorrect = false;
        if (!initiallyMarkAsCorrect && Settings.isOptionForMarkingMismatchAsCorrect()) {
            CLIOutput.writeOptionToMarkCorrectPrompt();
            overrideMarkAsCorrect = CLIInput.markAsCorrect();
        }

        final boolean correct = initiallyMarkAsCorrect || overrideMarkAsCorrect;
        mark(correct, SR);
    }

    private String fetchClue() {
        final String adaptedClueDefinition =
                QAParser.removeBrackets(Settings.isInReverseMode()
                        ? flashCard.getAnswer()
                        : flashCard.getClue());

        if (Settings.isSpecificCluePath()) {
            final Set<String> validClues =
                    QAParser.validOptionsForQADefinition(adaptedClueDefinition);

            return MathHelper.randomElementFromSet(validClues);
        } else
            return adaptedClueDefinition;
    }

    private String fetchAnswerDefinition() {
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
            else
                flashCard.updateRecord(correct);
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
