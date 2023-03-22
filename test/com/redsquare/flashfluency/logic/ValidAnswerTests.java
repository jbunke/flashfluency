package com.redsquare.flashfluency.logic;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class ValidAnswerTests {
    @Test
    public void abcTest() {
        final String ANSWER = "abc{de|fg}h|ij{k|lm}{opq|rs}tu|vw(x){y|z}";
        final String[] MODEL_ANSWERS = new String[] {
                "abcdeh",
                "abcfgh",
                "ijkopqtu",
                "ijkrstu",
                "ijlmopqtu",
                "ijlmrstu",
                "vwxy",
                "vwxz"
        };

        Set<String> validAnswers = QAParser.validOptionsForQADefinition(ANSWER);

        for (String validAnswer : validAnswers)
            System.out.println(validAnswer);

        System.out.println();

        for (final String MODEL_ANSWER : MODEL_ANSWERS) {
            System.out.println(MODEL_ANSWER);
            Assert.assertTrue(validAnswers.contains(MODEL_ANSWER));
        }
    }
}
