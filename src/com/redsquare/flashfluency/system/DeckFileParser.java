package com.redsquare.flashfluency.system;

import com.redsquare.flashfluency.cli.ExceptionMessenger;
import com.redsquare.flashfluency.logic.Deck;
import com.redsquare.flashfluency.logic.FlashCard;
import com.redsquare.flashfluency.logic.Pot;
import com.redsquare.flashfluency.system.exceptions.InvalidDeckFileFormatException;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class DeckFileParser {
    private static final String KEYWORD_DESCRIPTION = "description",
            KEYWORD_TAGS = "tags", KEYWORD_FLASH_CARDS = "flash_cards";
    // TODO: Consider possibility of making separators escape characters to permit
    // TODO: their usage between fields as legitimate data characters.
    private static final String FIELD_SEPARATOR = ";",
            TAG_SEPARATOR = ",", DATE_SEPARATOR = "-";

    // line indices
    private static final int DESCRIPTION_INDEX = 0, TAGS_INDEX = 1,
            FLASH_CARDS_INDEX = 2, MIN_NUM_LINES = 3;

    // field indices
    private static final int CLUE = 0, ANSWER = 1, INTRODUCED = 2,
            DUE = 3, POT = 4, POT_COUNTER = 5, NUM_FIELDS = 6;

    // date indices
    private static final int DAY = 0, MONTH = 1, YEAR = 2;

    public static void parse(FFDeckFile deckFile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(deckFile.getFilepath()));

            List<String> lines = br.lines().toList();

            if (lines.size() < MIN_NUM_LINES)
                throw InvalidDeckFileFormatException.tooFewLinesInFileForNecessaryParameters(deckFile.getFilepath());

            String description = parseDescription(lines.get(DESCRIPTION_INDEX), deckFile);
            Set<String> tags = parseTags(lines.get(TAGS_INDEX), deckFile);
            Map<String, FlashCard> flashCards = parseFlashCards(lines, deckFile);

            deckFile.setAssociatedDeck(Deck.fromParsedDeckFile(
                    deckFile.getName(), deckFile.getFilepath(), description, tags, flashCards));
        } catch (FileNotFoundException e) {
            ExceptionMessenger.deliver(
                    "Failed to read from file: " + deckFile.getFilepath(), false,
                    InvalidDeckFileFormatException.CONSEQUENCE_DECK_FILE_COULD_NOT_BE_PARSED
            );

            deckFile.setAssociatedDeck(Deck.createNew(deckFile.getName(), deckFile.getFilepath()));
        } catch (InvalidDeckFileFormatException e) {
            ExceptionMessenger.deliver(e);

            deckFile.setAssociatedDeck(Deck.createNew(deckFile.getName(), deckFile.getFilepath()));
        }
    }

    private static String parseDescription(String l, FFDeckFile deckFile) throws InvalidDeckFileFormatException {
        if (l.startsWith(KEYWORD_DESCRIPTION + Settings.SETTING_SEPARATOR))
            return l.substring((KEYWORD_DESCRIPTION + Settings.SETTING_SEPARATOR).length());
        else
            throw InvalidDeckFileFormatException.descriptionImproperlyFormatted(deckFile.getFilepath());
    }

    private static Set<String> parseTags(String l, FFDeckFile deckFile) throws InvalidDeckFileFormatException {
        if (l.startsWith(KEYWORD_TAGS + Settings.SETTING_SEPARATOR)) {
            String tags = l.substring((KEYWORD_TAGS + Settings.SETTING_SEPARATOR).length());
            return new HashSet<>(Arrays.stream(tags.split(TAG_SEPARATOR)).toList());
        } else
            throw InvalidDeckFileFormatException.tagsImproperlyFormatted(deckFile.getFilepath());
    }

    private static Map<String, FlashCard> parseFlashCards(List<String> lines, FFDeckFile deckFile) throws InvalidDeckFileFormatException {
        if (lines.get(FLASH_CARDS_INDEX).startsWith(KEYWORD_FLASH_CARDS + Settings.SETTING_SEPARATOR)) {
            Map<String, FlashCard> flashCards = new HashMap<>();

            for (int i = FLASH_CARDS_INDEX + 1; i < lines.size(); i++) {
                String l = lines.get(i).trim();

                if (l.equals(""))
                    break;

                String[] fields = l.split(FIELD_SEPARATOR);

                if (fields.length != NUM_FIELDS)
                    throw InvalidDeckFileFormatException.flashCardsImproperlyFormatted(deckFile.getFilepath());

                String clue = fields[CLUE], answer = fields[ANSWER];
                boolean introduced = Boolean.parseBoolean(fields[INTRODUCED]);
                String[] date = fields[DUE].split(DATE_SEPARATOR);
                LocalDate due = LocalDate.of(Integer.parseInt(date[YEAR]),
                        Integer.parseInt(date[MONTH]), Integer.parseInt(date[DAY]));
                Pot pot = Pot.valueOf(fields[POT]);
                int potCounter = Integer.parseInt(fields[POT_COUNTER]);

                flashCards.put(clue,
                        FlashCard.fromParsedDeckFile(clue, answer, introduced, due, pot, potCounter));
            }

            return flashCards;
        } else
            throw InvalidDeckFileFormatException.flashCardsImproperlyFormatted(deckFile.getFilepath());
    }

    public static void savetoFile(String filepath, String description,
                                  Set<String> tags, Map<String, FlashCard> flashCards) throws IOException {
        String dir = filepath.substring(0, filepath.lastIndexOf('/'));
        File dirLocation = new File(dir);

        if (!dirLocation.exists() && !dirLocation.mkdirs())
            throw new IOException();

        BufferedWriter bw = new BufferedWriter(new FileWriter(filepath, false));

        StringBuilder sb = new StringBuilder();
        for (String tag : tags) {
            sb.append(tag).append(TAG_SEPARATOR);
        }
        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);

        List<String> fcs = new ArrayList<>();

        flashCards.keySet().forEach(x -> {
            FlashCard f = flashCards.get(x);
            String fc = f.getClue() + FIELD_SEPARATOR + f.getAnswer() +
                    FIELD_SEPARATOR + f.isIntroduced() + FIELD_SEPARATOR +
                    getStringFromLocalDate(f.getDue()) + FIELD_SEPARATOR +
                    f.getPot() + FIELD_SEPARATOR + f.getPotCounter();
            fcs.add(fc);
        });

        bw.write(KEYWORD_DESCRIPTION + Settings.SETTING_SEPARATOR + description + Settings.NEW_LINE);
        bw.write(KEYWORD_TAGS + Settings.SETTING_SEPARATOR + sb + Settings.NEW_LINE);
        bw.write(KEYWORD_FLASH_CARDS + Settings.SETTING_SEPARATOR + Settings.NEW_LINE);

        for (String fc : fcs) {
            bw.write(fc + Settings.NEW_LINE);
        }

        bw.close();
    }

    private static String getStringFromLocalDate(LocalDate date) {
        return date.getDayOfMonth() + DATE_SEPARATOR +
                date.getMonthValue() + DATE_SEPARATOR + date.getYear();

    }
}
