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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.imf.ImfHeaderList;
import messy.msgdata.formats.imf.ImfMessage;
import messy.msgdata.formats.mbox.MboxMessage;
import messy.msgdata.formats.twitter.TwitterStatus;
import messy.msgio.formats.imf.ImfConverter;
import messy.msgio.formats.imf.ImfParser;
import messy.msgio.formats.mbox.MboxReader;
import messy.msgio.formats.twitter.JsonTwitterParser;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

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

  private static OutputFormat outputFormat = OutputFormat.TSV;

  public static OutputFormat getOutputFormat()
  {
    return outputFormat;
  }

  public static void setOutputFormat(OutputFormat outputFormat)
  {
    App.outputFormat = outputFormat;
  }

  protected static DateFormat createFormatter()
  {
    final SimpleDateFormat result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT);
    result.setTimeZone(TimeZone.getTimeZone("UTC"));
    return result;
  }

  protected static String escape(String s)
  {
    String result;
    if (s == null)
    {
      result = null;
    }
    else
    {
      if (s.isEmpty())
      {
        result = s;
      }
      else
      {
        boolean modified = false;
        final char[] a = s.toCharArray();
        for (int i = 0; i < a.length; i++)
        {
          final char c = a[i];
          if (c == 9 || c == 10 || c == 13)
          {
            a[i] = ' ';
            modified = true;
          }
        }
        if (modified)
        {
          result = new String(a);
        }
        else
        {
          result = s;
        }
      }
    }
    return result;
  }

  protected static String format(String s)
  {
    return s == null ? "" : s;
  }

  protected static String format(DateFormat formatter, Date d)
  {
    return d == null ? "" : formatter.format(d);
  }

  protected static String format(Message msg, DateFormat formatter)
  {
    if (outputFormat == OutputFormat.TSV)
    {
      return formatTsv(msg, formatter);
    }
    else
    {
      return formatJson(msg, formatter);
    }
  }

  protected static String formatJson(Message msg, DateFormat formatter)
  {
    final JSONObject res = new JSONObject();

    res.put("sent", format(formatter, msg.getSent()));
    res.put("msg_id", format(msg.getMessageId()));
    res.put("author_id", format(msg.getAuthorId()));
    res.put("author_name", format(msg.getAuthorName()));
    res.put("subject", format(escape(msg.getSubject())));
    res.put("text", format(escape(msg.getText())));

    return JSONValue.toJSONString(res, JSONValue.COMPRESSION);
  }

  protected static String formatTsv(Message msg, DateFormat formatter)
  {
    final StringBuilder sb = new StringBuilder();
    final String sep = "\t";

    sb.append(format(formatter, msg.getSent()));
    sb.append(sep);
    sb.append(format(msg.getLanguageCode()));
    sb.append(sep);
    sb.append(format(msg.getCountryCode()));
    sb.append(sep);
    sb.append(format(msg.getMessageId()));
    sb.append(sep);
    sb.append(format(msg.getAuthorId()));
    sb.append(sep);
    sb.append(format(msg.getAuthorName()));
    sb.append(sep);
    sb.append(format(escape(msg.getSubject())));
    sb.append(sep);
    sb.append(format(escape(msg.getText())));

    return sb.toString();
  }

  private static void dump(Message msg, DateFormat formatter)
  {
    System.out.println(format(msg, formatter));
  }

  private static void processJson(BufferedReader in, DateFormat formatter) throws IOException
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

  private static void processMbox(BufferedReader in, DateFormat formatter) throws IOException
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
    final DateFormat formatter = createFormatter();
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
        processJson(in, formatter);
      }
      else
      {
        processMbox(in, formatter);
      }
    }
    catch (final IOException ioe)
    {
      ioe.printStackTrace();
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
    processStandardInput();
  }
}
