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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import messy.msgcli.app.FileFormatHelper.FileType;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.imf.ImfHeaderList;
import messy.msgdata.formats.imf.ImfMessage;
import messy.msgdata.formats.mbox.MboxMessage;
import messy.msgdata.formats.twitter.TwitterStatus;
import messy.msgio.formats.AbstractMessageFormatter;
import messy.msgio.formats.imf.ImfConverter;
import messy.msgio.formats.imf.ImfParser;
import messy.msgio.formats.mbox.MboxReader;
import messy.msgio.formats.twitter.JsonTwitterParser;

/**
 * Process streams.
 *
 * @author Marco Schmidt
 */
public class InputProcessor
{
  private AbstractMessageFormatter messageFormatter;

  private void dump(Message msg)
  {
    System.out.println(getMessageFormatter().format(msg));
  }

  public AbstractMessageFormatter getMessageFormatter()
  {
    return messageFormatter;
  }

  private void processJson(BufferedReader in) throws IOException
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
        dump(msg);
      }
    }
  }

  private void processMbox(BufferedReader in) throws IOException
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
        dump(msg);
      }
    }
  }

  private BufferedReader openAsBufferedReader(InputStream in, Charset cs)
  {
    return new BufferedReader(new InputStreamReader(in, cs));
  }

  public void process(InputStream is, String inputName)
  {
    try
    {
      final PushbackInputStream input = new PushbackInputStream(is, FileFormatHelper.getNumBytesToLoad());
      final FileFormatHelper.FileType fileType = FileFormatHelper.identify(input);
      switch (fileType)
      {
      case GZIP:
      {
        final InputStream wrappedInput = FileFormatHelper.wrapDecompressor(input, fileType);
        process(wrappedInput, inputName + "\t" + fileType.name());
        break;
      }
      case TAR:
        processArchiveInput(input, inputName, fileType);
        break;
      case JSON:
        final BufferedReader jsonIn = openAsBufferedReader(input, StandardCharsets.UTF_8);
        processJson(jsonIn);
        break;
      case MBOX:
        final BufferedReader mboxIn = openAsBufferedReader(input, StandardCharsets.ISO_8859_1);
        processMbox(mboxIn);
        break;
      default:
        System.err.println("Could not identify '" + inputName + "' to be in a supported format.");
        break;
      }
    }
    catch (final IOException ioe)
    {
      ioe.printStackTrace();
    }
  }

  /**
   * Is the argument string a file name that consists of decimal digits only? A forward slash may separate a directory
   * name.
   */
  public static boolean isFileNameInteger(String name)
  {
    if (name == null)
    {
      return false;
    }
    int index = name.length() - 1;
    int numDigits = 0;
    while (index >= 0)
    {
      final char c = name.charAt(index--);
      if (c >= '0' && c <= '9')
      {
        numDigits++;
      }
      else
      {
        if (c == '/')
        {
          break;
        }
        else
        {
          return false;
        }
      }
    }
    return numDigits > 0;
  }

  private ArchiveInputStream openArchive(InputStream is, String inputName, FileType type)
  {
    return new TarArchiveInputStream(is, true);
  }

  protected void processArchiveInput(InputStream is, String inputName, FileType type)
  {
    final ArchiveInputStream ain = openArchive(is, inputName, type);
    try
    {
      ArchiveEntry entry;
      while ((entry = ain.getNextEntry()) != null)
      {
        if (entry.isDirectory())
        {
          continue;
        }
        final String name = entry.getName();
        // if (!ain.canReadEntryData(entry))
        // {
        // System.err.println("Cannot decode, skipping '" + inputName + "\t" + name);
        // continue;
        // }
        // if (isFileNameInteger(name))
        // {
        //
        // }
        // else
        // {
        // }
        process(ain, inputName + "\t" + name);
      }
    }
    catch (final IOException ioe)
    {
      ioe.printStackTrace();
    }
  }

  protected void process(String name)
  {
    try (FileInputStream in = new FileInputStream(name))
    {
      process(in, name);
    }
    catch (final IOException ioe)
    {
      System.err.println("Unable to open '" + name + "': " + ioe.getMessage());
    }
  }

  public void process(List<String> fileNames)
  {
    for (final String name : fileNames)
    {
      process(name);
    }
  }

  public void setMessageFormatter(AbstractMessageFormatter messageFormatter)
  {
    this.messageFormatter = messageFormatter;
  }
}
