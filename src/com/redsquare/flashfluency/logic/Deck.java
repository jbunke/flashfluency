package com.redsquare.flashfluency.logic;

import com.redsquare.flashfluency.cli.CLIOutput;
import com.redsquare.flashfluency.cli.ExceptionMessenger;
import com.redsquare.flashfluency.system.DeckFileParser;
import com.redsquare.flashfluency.system.FileIOHelper;
import com.redsquare.flashfluency.system.Settings;
import com.redsquare.flashfluency.system.exceptions.FFErrorMessages;
import com.redsquare.flashfluency.system.exceptions.FlashFluencyLogicException;

import java.io.*;
import java.util.*;
import java.util.function.Function;

public class Deck {
    public static final String TAG_IRREVERSIBLE = "irreversible", TAG_STRICT = "strict";

    private final String name;
    private String filepath;

    private String description;
    private final Set<String> tags;
    private final Map<String, FlashCard> flashCards;

    private Deck(String name, String filepath, String description,
                 Set<String> tags, Map<String, FlashCard> flashCards) {
        this.name = name;
        this.filepath = filepath;

        this.description = description;
        this.tags = tags;
        this.flashCards = flashCards;
    }

    public static Deck fromParsedDeckFile(String name, String filepath, String description,
                                          Set<String> tags, Map<String, FlashCard> flashCards) {
        return new Deck(name, filepath, description, tags, flashCards);
    }

    public static Deck createNew(String name, String filepath) {
        return new Deck(name, filepath, "", new HashSet<>(), new HashMap<>());
    }

    public void updateFilepath(final String filepath) {
        // delete the old file
        FileIOHelper.deleteFileFootprintFromSystem(this.filepath);

        // set correct filepath
        this.filepath = filepath;

        try {
            // save to new location
            saveToFile();
        } catch (IOException e) {
            ExceptionMessenger.deliver(
                    FFErrorMessages.MESSAGE_FAILED_WRITE_TO_DECK_FILE,
                    false, FFErrorMessages.CONSEQUENCE_DECK_DATA_NOT_SAVED);
        }
    }

    public void addFlashCard(final FlashCard flashCard, final boolean isImported) {
        try {
            final String clue = flashCard.getClue();

            if (flashCards.containsKey(clue))
                throw FlashFluencyLogicException
                        .attemptedToAddFlashCardWithDuplicateClue(clue);

            flashCards.put(flashCard.getClue(), flashCard);

            if (isImported)
                CLIOutput.writeImportedFlashCard(flashCard);
            else
                CLIOutput.writeAddedFlashCard(flashCard);
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    public void removeFlashCard(final FlashCard flashCard) {
        try {
            if (!flashCards.containsValue(flashCard))
                throw FlashFluencyLogicException
                        .attemptedToRemoveFlashCardNotInDeck();

            flashCards.remove(flashCard.getClue());
            CLIOutput.writeRemovedFlashCard(flashCard);
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    public void addTag(final String tag) {
        try {
            if (tags.contains(tag))
                throw FlashFluencyLogicException.attemptedToAddExistingTagToDeck(tag);

            tags.add(tag);
            CLIOutput.writeAddedTag(tag);
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    public void removeTag(final String tag) {
        try {
            if (!tags.contains(tag))
                throw FlashFluencyLogicException.attemptedToRemoveTagNotInDeck(tag);

            tags.remove(tag);
            CLIOutput.writeRemovedTag(tag);
        } catch (FlashFluencyLogicException e) {
            ExceptionMessenger.deliver(e);
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void saveToFile() throws IOException {
        DeckFileParser.saveToFile(filepath, description, tags, flashCards);
    }

    private Set<FlashCard> filterFlashCards(Function<FlashCard, Boolean> f) {
        Set<FlashCard> compliant = new HashSet<>();

        flashCards.keySet().forEach(x -> {
            FlashCard flashCard = flashCards.get(x);
            if (f.apply(flashCard))
                compliant.add(flashCard);
        });

        return compliant;
    }

    private Set<FlashCard> subset(final List<FlashCard> input, final int LIMIT) {
        Set<FlashCard> output = new HashSet<>();

        for (int i = 0; i < input.size() && i < LIMIT; i++) {
            output.add(input.get(i));
        }

        return output;
    }

    public Set<FlashCard> getCardsForTest(final int NUM_Qs) {
        List<FlashCard> flashCardList = new ArrayList<>(filterFlashCards(x -> true));
        Collections.shuffle(flashCardList);
        return subset(flashCardList, NUM_Qs);
    }

    public Set<FlashCard> getCardsThatAreDue() {
        List<FlashCard> toReview = new ArrayList<>(filterFlashCards(FlashCard::isDue));
        Collections.shuffle(toReview);
        return subset(toReview, Settings.getLessonIntroLimit());
    }

    public Set<FlashCard> getCardsToIntroduce(final Set<FlashCard> alreadyInLesson) {
        List<FlashCard> notIntroduced = new ArrayList<>(
                filterFlashCards(x -> !(x.isIntroduced() || alreadyInLesson.contains(x)))
        );
        Collections.shuffle(notIntroduced);
        return subset(notIntroduced, Settings.getLessonIntroLimit() - alreadyInLesson.size());
    }

    public int getPercentageScore() {
        int total = 0;

        for (String clue : getFlashCardClues()) {
            FlashCard fc = getFlashCard(clue);
            total += fc.getPot().getScore();
        }

        return (int)((total * 100) / (float)(getNumOfFlashCards() * Pot.MAX_SCORE));
    }

    public int getNumOfFlashCards() {
        return flashCards.keySet().size();
    }

    public int getNumFlashCardsInPot(final Pot pot) {
        return filterFlashCards(x -> x.getPot() == pot).size();
    }

    public int getNumDueFlashCards() {
        return filterFlashCards(FlashCard::isDue).size();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Set<String> getFlashCardClues() {
        return flashCards.keySet();
    }

    public Optional<FlashCard> getFlashCardFromCode(final String code) {
        for (final String flashCardClue : getFlashCardClues()) {
            final FlashCard flashCard = flashCards.get(flashCardClue);

            if (flashCard.getCode().toUpperCase().equals(code.toUpperCase().trim()))
                return Optional.of(flashCard);
        }

        return Optional.empty();
    }

    public FlashCard getFlashCard(String key) {
        return flashCards.get(key);
    }

    public void importCards(String filepath) {
        filepath = filepath.replace("/", File.separator).replace("\\", File.separator);

        try {
            BufferedReader br = new BufferedReader(new FileReader(filepath));

            for (String line : br.lines().toList()) {
                final int CLUE = 0, ANSWER = 1, TOTAL = 2;
                final String SEPARATOR = ",";

                String[] fields = line.split(SEPARATOR);

                if (fields.length != TOTAL)
                    continue;

                FlashCard flashCard = FlashCard.createNew(fields[CLUE], fields[ANSWER]);
                addFlashCard(flashCard, true);
            }
        } catch (FileNotFoundException e) {
            ExceptionMessenger.deliver("The file \"" + filepath +
                    "\" could not be found.", false, "No flash cards were imported.");
        }
    }

    public void saveDeck() {
        try {
            saveToFile();
            CLIOutput.writeSavedDeck(this, filepath);
        } catch (IOException e) {
            ExceptionMessenger.deliver(
                    FFErrorMessages.MESSAGE_FAILED_WRITE_TO_DECK_FILE,
                    false, FFErrorMessages.CONSEQUENCE_DECK_DATA_NOT_SAVED);
        }
    }

    public void clearDeck() {
        Set<String> keys = new HashSet<>(flashCards.keySet());
        keys.forEach(flashCards::remove);
        CLIOutput.writeClearedDeck(this);
    }

    public void resetMemorizationData() {
        Set<String> keys = new HashSet<>(flashCards.keySet());
        keys.forEach(x -> flashCards.get(x).reset());
        CLIOutput.writeResetDeckMemorizationData(this);
    }

    public void prepForLesson(final boolean isSR) {
        final boolean UPDATE_COND_IRREVERSIBLE = Settings.isInReverseMode(),
                UPDATE_COND_STRICT = Settings.isNotMarkingForAccents() ||
                        Settings.isOptionForMarkingMismatchAsCorrect() ||
                        Settings.isIgnoringBracketed();

        // is "irreversible" check
        if (tags.contains(TAG_IRREVERSIBLE) && UPDATE_COND_IRREVERSIBLE)
            Settings.irreversibleDeckSettingsUpdate(isSR);

        // is "strict" check
        if (tags.contains(TAG_STRICT) && UPDATE_COND_STRICT)
            Settings.strictDeckSettingsUpdate(isSR);
    }
}
