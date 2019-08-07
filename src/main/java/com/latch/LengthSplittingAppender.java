package com.latch;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

public class LengthSplittingAppender extends SplittingAppenderBase<ILoggingEvent> {

    private static final int DEFAULT_MAX_LENGTH = 50000;
    private static final String DEFAULT_SEQUENCE_IDENTIFIER = "seq";
    private static final String MESSAGE_LENGTH_KEY = "max-message-length";
    private static final String SEQUENCE_KEY = "sequence-key";

    private final int maxMessageLength;
    private final String sequenceKey;
    private final Splitter splitter;

    LengthSplittingAppender() {
        this.setContext((LoggerContext) LoggerFactory.getILoggerFactory());

        this.maxMessageLength = Integer.parseInt(
                getPropertyOrDefault(
                    MESSAGE_LENGTH_KEY,
                    Integer.toString(DEFAULT_MAX_LENGTH)));
        this.sequenceKey =
                getPropertyOrDefault(SEQUENCE_KEY, DEFAULT_SEQUENCE_IDENTIFIER);
        this.splitter = Splitter.fixedLength(maxMessageLength);
    }

    @Override
    public boolean shouldSplit(ILoggingEvent event) {
        return event.getFormattedMessage().length() > maxMessageLength;
    }

    @Override
    public List<ILoggingEvent> split(ILoggingEvent event) {
        List<String> logMessages = Lists.newArrayList(splitter.split(event.getFormattedMessage()));

        List<ILoggingEvent> splitLogEvents = new ArrayList<>(logMessages.size());
        for (int i = 0; i < logMessages.size(); i++) {

            MDC.put(sequenceKey, Integer.toString(i));

            LoggingEvent loggingEventPartition = LoggingEventCloner.clone(event);
            loggingEventPartition.setMessage(logMessages.get(i));

            splitLogEvents.add(loggingEventPartition);
            MDC.clear();
        }

        return splitLogEvents;
    }


    private String getPropertyOrDefault(String propertyKey, String defaultValue) {
        String property = getContext().getProperty(propertyKey);
        if (property == null) {
            addWarn(
                    String.format(
                            "Could not load %s, reverting to default size %s",
                            propertyKey,
                            defaultValue)
            );
            return defaultValue;
        }
        return property;
    }

    @VisibleForTesting
    int getMaxMessageLength() {
        return maxMessageLength;
    }
}
