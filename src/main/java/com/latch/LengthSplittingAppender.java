package com.latch;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

public class LengthSplittingAppender extends SplittingAppenderBase<ILoggingEvent> {

    private static final int DEFAULT_MAX_LENGTH = 50000;
    private static final String DEFAULT_SEQUENCE_IDENTIFIER = "seq";
    private static final String MESSAGE_LENGTH_KEY = "max-message-length";
    private static final String SEQUENCE_KEY = "sequence-key";

    private int maxMessageLength;
    private String sequenceKey;

    public LengthSplittingAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        this.setContext(loggerContext);

        try {
            maxMessageLength = Integer.parseInt(getPropertyOrDefault(MESSAGE_LENGTH_KEY,
                    Integer.toString(DEFAULT_MAX_LENGTH)));
        } catch (NumberFormatException e) {
            addError(
                    String.format(
                            "Invalid integer provided, reverting to default size %s",
                            DEFAULT_MAX_LENGTH),
                    e);
            maxMessageLength = DEFAULT_MAX_LENGTH;
        }

        sequenceKey = getPropertyOrDefault(SEQUENCE_KEY, DEFAULT_SEQUENCE_IDENTIFIER);
    }

    @Override
    public boolean shouldSplit(ILoggingEvent event) {
        return event.getFormattedMessage().length() > maxMessageLength;
    }

    @Override
    public List<ILoggingEvent> split(ILoggingEvent event) {
        List<String> logMessages = Lists.newArrayList(
                Splitter.fixedLength(maxMessageLength)
                        .split(event.getFormattedMessage()));

        List<ILoggingEvent> splitLogEvents = new ArrayList<>(logMessages.size());

        for (int i = 0; i < logMessages.size(); i++) {

            MDC.put(sequenceKey, Integer.toString(i));

            LoggingEvent loggingEventPartition = cloneLogEvent(event);
            loggingEventPartition.setMessage(logMessages.get(i));

            splitLogEvents.add(loggingEventPartition);
            MDC.clear();
        }

        return splitLogEvents;
    }

    public void setMaxMessageLength(int maxMessageLength) {
        this.maxMessageLength = maxMessageLength;
    }

    private LoggingEvent cloneLogEvent(ILoggingEvent event) {
        LoggingEvent logEventPartition = new LoggingEvent();

        logEventPartition.setLevel(event.getLevel());
        logEventPartition.setLoggerName(event.getLoggerName());
        logEventPartition.setTimeStamp(event.getTimeStamp());
        logEventPartition.setLoggerContextRemoteView(event.getLoggerContextVO());
        logEventPartition.setThreadName(event.getThreadName());

        Marker eventMarker = event.getMarker();
        if (eventMarker != null) {
            logEventPartition.setMarker(eventMarker);
        }

        if (event.hasCallerData()) {
            logEventPartition.setCallerData(event.getCallerData());
        }

        return logEventPartition;
    }

    private String getPropertyOrDefault(String propertyKey, String defaultValue) {
        try {
            return getContext().getProperty(propertyKey);
        }
        catch (Exception e) {
            addWarn(
                    String.format(
                            "Could not load %s, reverting to default size %s",
                            propertyKey,
                            defaultValue),
                    e);
            return defaultValue;
        }
    }
}
