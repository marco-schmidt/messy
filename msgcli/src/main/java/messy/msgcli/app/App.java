/*
 * Copyright 2020, 2021, 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package messy.msgcli.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import messy.msgdata.formats.Message;
import messy.msgio.AppState;
import messy.msgio.formats.AbstractMessageFormatter;
import messy.msgio.formats.JsonMessageFormatter;
import messy.msgio.formats.TsvMessageFormatter;
import messy.msgio.utils.StringUtils;
import messy.msgsearch.elk.ElkOutputProcessor;
import net.logstash.logback.encoder.LogstashEncoder;
import net.logstash.logback.fieldnames.LogstashFieldNames;

/**
 * Command-line application to provide messy conversion functionality.
 */
public final class App
{
  /**
   * Name of the application.
   */
  public static final String APP_NAME = "msgcli";
  /**
   * Program argument to indicate that file names are to be read from standard input.
   */
  public static final String SWITCH_READ_FILE_NAMES_STDIN = "-@";
  /**
   * Environment variable to specify granularity of logging.
   */
  public static final String MESSY_LOG_LEVEL = "MESSY_LOG_LEVEL";
  /**
   * Environment variable to specify the output format.
   */
  public static final String MESSY_OUTPUT_FORMAT = "MESSY_OUTPUT_FORMAT";
  /**
   * Environment variable to specify message items to be included in output.
   */
  public static final String MESSY_OUTPUT_ITEMS = "MESSY_OUTPUT_ITEMS";
  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

  private static volatile Map<String, String> environment;
  private static List<String> fileNames;
  private static List<Message.Item> outputItems;
  private static OutputFormat outputFormat = OutputFormat.JSON;

  private App()
  {
    // prevent instantiation
  }

  protected enum OutputFormat
  {
    ELASTIC, JSON, TSV
  };

  public static OutputFormat getOutputFormat()
  {
    return outputFormat;
  }

  public static void setOutputFormat(OutputFormat outputFormat)
  {
    App.outputFormat = outputFormat;
  }

  public static List<Message.Item> getOutputItems()
  {
    final List<Message.Item> result = new ArrayList<>();
    result.addAll(outputItems);
    return result;
  }

  public static void setOutputItems(List<Message.Item> outputItems)
  {
    App.outputItems = new ArrayList<>();
    App.outputItems.addAll(outputItems);
  }

  public static void setEnvironment(Map<String, String> newEnv)
  {
    environment = new HashMap<>(newEnv);
  }

  protected static DateFormat createDateFormatter()
  {
    final SimpleDateFormat result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT);
    result.setTimeZone(TimeZone.getTimeZone("UTC"));
    return result;
  }

  private static AbstractMessageFormatter createMessageFormatter()
  {
    switch (outputFormat)
    {
    case TSV:
      return new TsvMessageFormatter();
    default:
      return new JsonMessageFormatter();
    }
  }

  private static String getEnv(String key, String defaultValue)
  {
    if (environment == null)
    {
      return defaultValue;
    }
    String result = environment.get(key);
    if (result == null)
    {
      result = defaultValue;
    }
    return result;
  }

  protected static List<Message.Item> initConfigurationOutputItems(String outputItems)
  {
    List<Message.Item> result;
    if (outputItems == null)
    {
      result = Arrays.asList(Message.Item.values());
    }
    else
    {
      result = new ArrayList<>();
      final List<String> itemNames = StringUtils.splitAndNormalize(outputItems, ",");
      for (final String name : itemNames)
      {
        result.add(Message.Item.valueOf(name.toUpperCase(Locale.ROOT)));
      }
    }
    return result;
  }

  protected static List<String> parseArguments(String[] args)
  {
    final List<String> argsList = Arrays.asList(args);
    final List<String> fileNames = new ArrayList<>();
    final Iterator<String> iter = argsList.iterator();
    boolean readFileNamesFromStdin = false;
    while (iter.hasNext())
    {
      final String arg = iter.next();
      if (App.SWITCH_READ_FILE_NAMES_STDIN.equals(arg))
      {
        readFileNamesFromStdin = true;
      }
      else
      {
        fileNames.add(arg);
      }
    }
    if (readFileNamesFromStdin)
    {
      fileNames.addAll(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)).lines()
          .collect(Collectors.toList()));
    }
    return fileNames;
  }

  private static void initConfiguration(String[] args)
  {
    // initialize environment map from environment unless someone (like a unit test setup method)
    // has done so already before explicitly calling main
    if (environment == null)
    {
      setEnvironment(System.getenv());

      Runtime.getRuntime().addShutdownHook(new Thread()
      {
        @Override
        public void run()
        {
          if (!AppState.isMainFinished())
          {
            LOGGER.info("Received shutdown notice. Application is terminating.");
          }
          AppState.setActive(false);
        }
      });
    }

    initLogging();

    setOutputFormat(OutputFormat.valueOf(getEnv(MESSY_OUTPUT_FORMAT, OutputFormat.JSON.name())));
    setOutputItems(initConfigurationOutputItems(getEnv(MESSY_OUTPUT_ITEMS, null)));

    fileNames = parseArguments(args);
  }

  protected static void initLogging()
  {
    final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
        .getLogger(Logger.ROOT_LOGGER_NAME);
    final LoggerContext loggerContext = rootLogger.getLoggerContext();
    loggerContext.reset();

    final LogstashEncoder encoder = new LogstashEncoder();
    encoder.setContext(loggerContext);
    encoder.setVersion(null);
    final LogstashFieldNames names = new LogstashFieldNames();
    encoder.setFieldNames(names);
    encoder.setCustomFields("{\"app_name\":\"" + APP_NAME + "\"}");
    encoder.start();

    final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
    appender.setContext(loggerContext);
    appender.setEncoder(encoder);
    appender.setTarget("System.err");
    appender.start();

    rootLogger.addAppender(appender);
    rootLogger.setLevel(Level.toLevel(getEnv(MESSY_LOG_LEVEL, "INFO")));
  }

  public static void main(String[] args)
  {
    initConfiguration(args);
    final AbstractMessageFormatter messageFormatter = createMessageFormatter();
    messageFormatter.setDateFormatter(createDateFormatter());
    messageFormatter.setItems(getOutputItems());
    final InputProcessor ip = new InputProcessor();
    if (outputFormat == OutputFormat.ELASTIC)
    {
      final ElkOutputProcessor op = new ElkOutputProcessor();
      op.setMessageFormatter(messageFormatter);
      op.connect();
      ip.setOutputProcessor(op);
    }
    else
    {
      ip.getOutputProcessor().setMessageFormatter(messageFormatter);
    }

    if (fileNames.isEmpty())
    {
      ip.process(System.in, "-");
    }
    else
    {
      ip.process(fileNames);
    }
    ip.close();
    AppState.setMainFinished(true);
  }
}
