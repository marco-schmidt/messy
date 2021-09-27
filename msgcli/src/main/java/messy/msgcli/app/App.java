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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.imf.ImfHeaderList;
import messy.msgdata.formats.imf.ImfMessage;
import messy.msgdata.formats.mbox.MboxMessage;
import messy.msgdata.formats.twitter.TwitterStatus;
import messy.msgio.formats.AbstractMessageFormatter;
import messy.msgio.formats.JsonMessageFormatter;
import messy.msgio.formats.TsvMessageFormatter;
import messy.msgio.formats.imf.ImfConverter;
import messy.msgio.formats.imf.ImfParser;
import messy.msgio.formats.mbox.MboxReader;
import messy.msgio.formats.twitter.JsonTwitterParser;

/**
 * Command-line application to provide messy conversion functionality.
 */
public final class App
{
  private App()
  {
    // prevent instantiation
  }

  private enum FileType
  {
    JSON, MBOX, UNKNOWN
  };

  protected enum OutputFormat
  {
    JSON, TSV
  };

  private static List<Message.Item> outputItems;
  private static OutputFormat outputFormat = OutputFormat.TSV;

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

  protected static DateFormat createDateFormatter()
  {
    final SimpleDateFormat result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT);
    result.setTimeZone(TimeZone.getTimeZone("UTC"));
    return result;
  }

  private static void dump(Message msg, AbstractMessageFormatter formatter)
  {
    System.out.println(formatter.format(msg));
  }

  private static void processJson(BufferedReader in, AbstractMessageFormatter formatter) throws IOException
  {
    String line;
    while ((line = in.readLine()) != null)
    {
      final TwitterStatus status = JsonTwitterParser.parseStatus(line);
      if (status == null)
      {
        System.err.println("Could not decode JSON tweet:'" + line + "'.");
      }
      else
      {
        final Message msg = JsonTwitterParser.toMessage(status);
        dump(msg, formatter);
      }
    }
  }

  private static void processMbox(BufferedReader in, AbstractMessageFormatter formatter) throws IOException
  {
    MboxMessage mboxMsg;
    final MboxReader reader = new MboxReader(in);
    while ((mboxMsg = reader.next()) != null)
    {
      final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(mboxMsg.getHeaderLines());
      final ImfMessage imfMsg = new ImfMessage(headerList, mboxMsg.getBodyLines());
      final Message msg = new ImfConverter().convert(imfMsg);
      if (msg == null)
      {
        System.err.println("Null msg in " + reader.getLineNumber());
      }
      else
      {
        dump(msg, formatter);
      }
    }
    reader.close();
  }

  private static void processStandardInput()
  {
    final DateFormat dateFormatter = createDateFormatter();
    final AbstractMessageFormatter messageFormatter = createMessageFormatter();
    messageFormatter.setDateFormatter(dateFormatter);
    messageFormatter.setItems(getOutputItems());
    final InputStream is = System.in;
    final PushbackInputStream input = new PushbackInputStream(is, 1024 * 1024);
    final FileType fileType = identify(input);
    if (fileType == FileType.UNKNOWN)
    {
      System.err.println("Could not identify input to be in a supported format.");
      return;
    }
    try (BufferedReader in = new BufferedReader(
        new InputStreamReader(input, fileType == FileType.JSON ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1)))
    {
      if (fileType == FileType.JSON)
      {
        processJson(in, messageFormatter);
      }
      else
      {
        processMbox(in, messageFormatter);
      }
    }
    catch (final IOException ioe)
    {
      ioe.printStackTrace();
    }
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

  private static FileType identify(PushbackInputStream input)
  {
    FileType result = FileType.UNKNOWN;
    try
    {
      final int firstByte = input.read();
      if (firstByte == '{')
      {
        result = FileType.JSON;
      }
      else
        if (firstByte == 'F')
        {
          result = FileType.MBOX;
        }
      input.unread(firstByte);
    }
    catch (final IOException ioe)
    {
    }
    return result;
  }

  public static void main(String[] args)
  {
    setOutputItems(Arrays.asList(Message.Item.values()));
    processStandardInput();
  }
}
