/*
 * Copyright 2020, 2021 the original author or authors.
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import messy.msgdata.formats.Message;
import messy.msgio.formats.AbstractMessageFormatter;
import messy.msgio.formats.JsonMessageFormatter;
import messy.msgio.formats.TsvMessageFormatter;
import messy.msgio.utils.StringUtils;

/**
 * Command-line application to provide messy conversion functionality.
 */
public final class App
{
  private App()
  {
    // prevent instantiation
  }

  /**
   * Environment variable to specify the output format.
   */
  public static final String MESSY_OUTPUT_FORMAT = "MESSY_OUTPUT_FORMAT";
  /**
   * Environment variable to specify message items to be included in output.
   */
  public static final String MESSY_OUTPUT_ITEMS = "MESSY_OUTPUT_ITEMS";

  protected enum OutputFormat
  {
    JSON, TSV
  };

  private static volatile Map<String, String> environment;
  private static List<String> fileNames;
  private static List<Message.Item> outputItems;
  private static OutputFormat outputFormat = OutputFormat.JSON;

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

  private static void initConfiguration(String[] args)
  {
    // initialize environment map from environment unless someone (like a unit test setup method)
    // has done so already before explicitly calling main
    if (environment == null)
    {
      setEnvironment(System.getenv());
    }

    setOutputFormat(OutputFormat.valueOf(getEnv(MESSY_OUTPUT_FORMAT, OutputFormat.TSV.name())));
    setOutputItems(initConfigurationOutputItems(getEnv(MESSY_OUTPUT_ITEMS, null)));

    fileNames = Arrays.asList(args);
  }

  public static void main(String[] args)
  {
    initConfiguration(args);
    final AbstractMessageFormatter messageFormatter = createMessageFormatter();
    messageFormatter.setDateFormatter(createDateFormatter());
    messageFormatter.setItems(getOutputItems());
    final InputProcessor ip = new InputProcessor();
    ip.setMessageFormatter(messageFormatter);

    if (fileNames.isEmpty())
    {
      ip.process(System.in, "-");
    }
    else
    {
      ip.process(fileNames);
    }
  }
}
