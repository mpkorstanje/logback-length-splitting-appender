package com.latch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LengthSplittingAppender extends SplittingAppenderBase<ILoggingEvent> {

    private int maxLength;
    private String sequenceKey;

    private Splitter splitter;

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        splitter = Splitter.fixedLength(maxLength);
    }

    public String getSequenceKey() {
        return sequenceKey;
    }

    public void setSequenceKey(String sequenceKey) {
        this.sequenceKey = sequenceKey;
    }

    @Override
    public boolean shouldSplit(ILoggingEvent event) {
        return event.getFormattedMessage().length() > maxLength;
    }

    @Override
    public List<ILoggingEvent> split(ILoggingEvent event) {
        List<String> logMessages = Lists.newArrayList(splitter.split(event.getFormattedMessage()));

        List<ILoggingEvent> splitLogEvents = new ArrayList<>(logMessages.size());
        for (int i = 0; i < logMessages.size(); i++) {

            LoggingEvent partition = LoggingEventCloner.clone(event);
            Map<String, String> seqMDCPropertyMap = new HashMap<>(event.getMDCPropertyMap());
            seqMDCPropertyMap.put(sequenceKey, Integer.toString(i));
            partition.setMDCPropertyMap(seqMDCPropertyMap);
            partition.setMessage(logMessages.get(i));

            splitLogEvents.add(partition);
        }

        return splitLogEvents;
    }
}
