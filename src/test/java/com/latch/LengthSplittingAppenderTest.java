package com.latch;

import ch.qos.logback.classic.LoggerContext;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.slf4j.LoggerFactory;

public class LengthSplittingAppenderTest {

    private static final int MAX_MESSAGE_LENGTH = 50;
    private static final String BASE_STRING = "0123456789";
    private static final String LOREM_PATH = "logging_message.txt";
    private static final String MESSAGE_LENGTH_KEY = "max-message-length";

    private final LengthSplittingAppender splitter;

    public LengthSplittingAppenderTest() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.putProperty(MESSAGE_LENGTH_KEY, Integer.toString(MAX_MESSAGE_LENGTH));

        this.splitter = new LengthSplittingAppender();
        Assert.assertEquals(MAX_MESSAGE_LENGTH, splitter.getMaxMessageLength());
    }

    @Test
    public void testEmpty() {
        LoggingEvent event = new LoggingEvent();
        event.setMessage("");
        Assert.assertFalse(splitter.shouldSplit(event));
    }

    @Test
    public void testLessThanMax() {
        LoggingEvent event = new LoggingEvent();
        event.setMessage(
                String.join("", Collections.nCopies(1, BASE_STRING)));
        Assert.assertFalse(splitter.shouldSplit(event));
    }

    @Test
    public void testEqualToMax() {
        LoggingEvent event = new LoggingEvent();
        event.setMessage(
                String.join("", Collections.nCopies(5, BASE_STRING)));
        Assert.assertEquals(MAX_MESSAGE_LENGTH, 5 * BASE_STRING.length());
        Assert.assertFalse(splitter.shouldSplit(event));
    }

    @Test
    public void testGreaterThanMaxAndMultipleOfMax() {
        LoggingEvent event = new LoggingEvent();
        event.setMessage(
                String.join("", Collections.nCopies(50, BASE_STRING)));
        Assert.assertTrue(splitter.shouldSplit(event));

        List<ILoggingEvent> splitEvents = splitter.split(event);

        Assert.assertEquals(
                event.getFormattedMessage().length() / MAX_MESSAGE_LENGTH,
                splitEvents.size());
    }

    @Test
    public void testGreaterThanMaxAndNotMultipleOfMax() {
        LoggingEvent event = new LoggingEvent();
        event.setMessage(
                String.join("", Collections.nCopies(51, BASE_STRING)));
        Assert.assertTrue(splitter.shouldSplit(event));

        List<ILoggingEvent> splitEvents = splitter.split(event);

        Assert.assertEquals(
                event.getFormattedMessage().length() / MAX_MESSAGE_LENGTH + 1,
                splitEvents.size());
    }

    @Test
    public void testSplitIntegrity() throws IOException {
        String loremIpsum = readTextFromResource(LOREM_PATH);
        LoggingEvent event = new LoggingEvent();
        event.setMessage(loremIpsum);

        List<ILoggingEvent> splitEvents = splitter.split(event);

        Assert.assertEquals(event.getFormattedMessage(), recreateMessage(splitEvents));
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
