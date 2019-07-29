package com.latch;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

public class LengthSplittingAppenderTest {

    private static final int MAX_MESSAGE_LENGTH = 50;
    private static final String BASE_STRING = "0123456789";
    private static final String LOREM_PATH = "logging_message.txt";

    private LoggingEvent shortLoggingEvent;
    private LoggingEvent equalLoggingEvent;
    private LoggingEvent longLoggingEvent;

    private LoggingEvent loremLoggingEvent;

    private LengthSplittingAppender splitter;

    @Before
    public void init() throws IOException {
        String shortMessage = String.join("", Collections.nCopies(1, BASE_STRING));
        String equalMessage = String.join("", Collections.nCopies(5, BASE_STRING));
        String longMessage = String.join("", Collections.nCopies(50, BASE_STRING));
        String loremMessage = readTextFromResource(LOREM_PATH);

        shortLoggingEvent = new LoggingEvent();
        equalLoggingEvent = new LoggingEvent();
        longLoggingEvent = new LoggingEvent();
        loremLoggingEvent = new LoggingEvent();

        shortLoggingEvent.setMessage(shortMessage);
        equalLoggingEvent.setMessage(equalMessage);
        longLoggingEvent.setMessage(longMessage);
        loremLoggingEvent.setMessage(loremMessage);

        splitter = new LengthSplittingAppender();
        splitter.setMaxMessageLength(MAX_MESSAGE_LENGTH);
    }

    @Test
    public void testShouldSplit() {
        Assert.assertFalse(splitter.shouldSplit(shortLoggingEvent));
        Assert.assertFalse(splitter.shouldSplit(equalLoggingEvent));
        Assert.assertTrue(splitter.shouldSplit(longLoggingEvent));
        Assert.assertTrue(splitter.shouldSplit(loremLoggingEvent));
    }

    @Test
    public void testSplitOfMaxMessageMultiple() {
        List<ILoggingEvent> splitEvents = splitter.split(longLoggingEvent);
        int logMessageLength = longLoggingEvent.getFormattedMessage().length();
        int expectedEventLength = logMessageLength / MAX_MESSAGE_LENGTH +
                ((logMessageLength % MAX_MESSAGE_LENGTH == 0) ? 0 : 1);

        Assert.assertEquals(
                expectedEventLength,
                splitEvents.size());

        for (ILoggingEvent splitEvent : splitEvents) {
            String expectedSplitMessage = String.join("", Collections.nCopies(
                    MAX_MESSAGE_LENGTH / expectedEventLength,
                    BASE_STRING));

            Assert.assertEquals(
                    expectedSplitMessage,
                    splitEvent.getMessage());
        }

        Assert.assertEquals(
                longLoggingEvent.getFormattedMessage(),
                recreateMessage(splitEvents)
        );
    }

    @Test
    public void testSplitOfLorem() {
        List<ILoggingEvent> splitEvents = splitter.split(loremLoggingEvent);
        int logMessageLength = loremLoggingEvent.getFormattedMessage().length();

        int expectedEventLength =
                logMessageLength / MAX_MESSAGE_LENGTH +
                        ((logMessageLength % MAX_MESSAGE_LENGTH == 0) ? 0 : 1);

        Assert.assertEquals(
                expectedEventLength,
                splitEvents.size());

        Assert.assertEquals(
                loremLoggingEvent.getFormattedMessage(),
                recreateMessage(splitEvents)
        );
    }

    private String recreateMessage(List<ILoggingEvent> splitEvents) {
        StringBuilder sb = new StringBuilder();

        for (ILoggingEvent splitEvent : splitEvents) {
            sb.append(splitEvent.getFormattedMessage());
        }
        
        return sb.toString();
    }

    private String readTextFromResource(String path) throws IOException {
        return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
    }
}
