# logback-length-splitting-appender
split long Logback logs by a configurable length

## Background

This Logback Appender is useful for avoiding truncated logs in 3rd party systems like Datadog and AWS Cloudwatch.

It provides the following configurable properties:
* max length (only applies to log message, not markers)
* sequence key (useful for reconstructing messages in systems like MapReduce)

## Usage

Import Logback classic and this appender.
```
  </dependencies>
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>1.2.3</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>com.latch</groupId>
      <artifactId>logback-length-splitting-appender</artifactId>
      <version>0.4.0</version>
    </dependency>
  </dependencies>
```

Point the splitting appender at your existing appenders with encoders within your `logback.xml`
```
<configuration debug="true">
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} %X{seq} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="SPLITTER" class="com.latch.LengthSplittingAppender">
        <appender-ref ref="STDOUT"/>
        <maxLength>5</maxLength>
        <sequenceKey>seq</sequenceKey>
    </appender>

    <root level="debug">
        <appender-ref ref="SPLITTER" />
    </root>
</configuration>
```

In this example, the `maxLength` ensures that no log message body exceeds 5 characters (10 bytes).
