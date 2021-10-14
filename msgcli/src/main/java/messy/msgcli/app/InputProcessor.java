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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import messy.msgcli.app.FileFormatHelper.FileType;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.anews.ANewsMessage;
import messy.msgdata.formats.imf.ImfHeaderList;
import messy.msgdata.formats.imf.ImfMessage;
import messy.msgdata.formats.mbox.MboxMessage;
import messy.msgdata.formats.twitter.TwitterStatus;
import messy.msgio.formats.AbstractMessageFormatter;
import messy.msgio.formats.anews.ANewsMessageConverter;
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

  public void process(InputStream is, String inputName)
  {
    try
    {
      final PushbackInputStream input = new PushbackInputStream(is, FileFormatHelper.getNumBytesToLoad());
      final FileFormatHelper.FileType fileType = FileFormatHelper.identify(input);
      switch (fileType)
      {
      case BZIP2:
      case GZIP:
      case Z:
      {
        final InputStream wrappedInput = FileFormatHelper.wrapDecompressor(input, fileType);
        process(wrappedInput, inputName + "\t" + fileType.name());
        break;
      }
      case TAR:
      case ZIP:
        processArchiveInput(input, inputName, fileType);
        break;
      case JSON:
        final BufferedReader jsonIn = messy.msgio.utils.IOUtils.openAsBufferedReader(input, StandardCharsets.UTF_8);
        processJson(jsonIn);
        break;
      case MBOX:
        final BufferedReader mboxIn = messy.msgio.utils.IOUtils.openAsBufferedReader(input,
            StandardCharsets.ISO_8859_1);
        processMbox(mboxIn);
        break;
      default:
        processUnidentified(input, inputName);
        break;
      }
    }
    catch (final IOException ioe)
    {
      ioe.printStackTrace();
    }
  }

  protected boolean processSingleMessageAnews(List<String> lines, String inputName)
  {
    final ANewsMessage msg = ANewsMessageConverter.fromLines(lines);
    if (msg == null)
    {
      return false;
    }
    else
    {
      dump(ANewsMessageConverter.toMessage(msg));
      return true;
    }
  }

  protected boolean processSingleMessageImf(List<String> lines, String inputName)
  {
    // copy header lines to one list
    final Iterator<String> iter = lines.iterator();
    final List<String> headerLines = new ArrayList<>();
    while (iter.hasNext())
    {
      final String line = iter.next();
      if (line.isEmpty())
      {
        break;
      }
      headerLines.add(line);
    }

    // and body lines to another
    final List<String> bodyLines = new ArrayList<>();
    while (iter.hasNext())
    {
      bodyLines.add(iter.next());
    }

    final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(headerLines);
    final ImfMessage imfMsg = new ImfMessage(headerList, bodyLines);
    final Message msg = new ImfConverter().convert(imfMsg);

    if (msg == null)
    {
      return false;
    }
    else
    {
      dump(msg);
      return true;
    }
  }

  protected void processUnidentified(InputStream is, String inputName)
  {
    boolean success = false;

    if (FileFormatHelper.isLikelySingleMessageFile(inputName))
    {
      final ByteArrayOutputStream bout = new ByteArrayOutputStream();
      try
      {
        org.apache.commons.compress.utils.IOUtils.copy(is, bout);
        final byte[] array = bout.toByteArray();
        final List<String> lines = messy.msgio.utils.IOUtils.toLines(new ByteArrayInputStream(array));
        if (array.length > 0 && array[0] == (byte) 'A')
        {
          success = processSingleMessageAnews(lines, inputName);
        }
        if (!success)
        {
          success = processSingleMessageImf(lines, inputName);
        }
      }
      catch (final IOException e)
      {
        e.printStackTrace();
      }
    }
    if (!success)
    {
      System.err.println("Could not identify '" + inputName + "' to be in a supported format.");
    }
  }

  protected void processArchiveInput(InputStream is, String inputName, FileType type)
  {
    final ArchiveInputStream ain = FileFormatHelper.openArchive(is, inputName, type);
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
        if (!ain.canReadEntryData(entry))
        {
          System.err.println("Cannot decode, skipping '" + inputName + "\t" + name);
          continue;
        }
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
