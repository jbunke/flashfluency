package com.redsquare.flashfluency.logic;

import com.redsquare.flashfluency.cli.CLIOutput;
import com.redsquare.flashfluency.cli.ExceptionMessenger;
import com.redsquare.flashfluency.system.DeckFileParser;
import com.redsquare.flashfluency.system.Settings;
import com.redsquare.flashfluency.system.exceptions.FFErrorMessages;

import java.io.*;
import java.util.*;
import java.util.function.Function;

public class Deck {
    private final String name;
    private final String filepath;

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

    public void addFlashCard(final FlashCard flashCard) {
        flashCards.put(flashCard.getClue(), flashCard);
    }

    public void addTag(final String tag) {
        tags.add(tag);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void saveToFile() throws IOException {
        DeckFileParser.savetoFile(filepath, description, tags, flashCards);
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

    public Set<String> getFlashCardAnswers() {
        Set<String> answers = new HashSet<>();

        flashCards.keySet().forEach(x -> answers.add(flashCards.get(x).getAnswer()));
        return answers;
    }

    public FlashCard getFlashCard(String key) {
        return flashCards.get(key);
    }

    public void importCards(String filepath) {
        System.out.println(File.separator);

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
                addFlashCard(flashCard);
                CLIOutput.writeImportedFlashCard(flashCard);
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

    public void removeTag(final String tag) {
        tags.remove(tag);
    }
}
