/*
 * Copyright 2020 the original author or authors.
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
package messy.msgio.formats.mbox;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import messy.msgdata.formats.mbox.MboxMessage;

/**
 * Read <a target="_top" href="http://www.faqs.org/rfcs/rfc822.html">RFC 822</a> style messages as
 * {@link messy.msgdata.formats.mbox.MboxMessage} objects from a {@link java.io.Reader} in
 * <a target="_top" href="https://www.loc.gov/preservation/digital/formats/fdd/fdd000383.shtml">mbox(5)</a> format.
 *
 * Call {@link #next()} repeatedly until it returns <code>null</code>.
 *
 * @author Marco Schmidt
 */
public class MboxReader implements AutoCloseable
{
  /**
   * Is high level unquoting enabled by default?
   */
  public static final boolean DEFAULT_HIGH_LEVEL_FROM_UNQUOTING = true;
  private static final String FROM = "From ";
  private LineNumberReader in;
  private boolean unquoteHighLevelFroms = DEFAULT_HIGH_LEVEL_FROM_UNQUOTING;
  private String envelopeLine;

  public MboxReader(Reader reader)
  {
    this(reader, DEFAULT_HIGH_LEVEL_FROM_UNQUOTING);
  }

  public MboxReader(Reader reader, boolean unquoteHigh)
  {
    in = new LineNumberReader(reader);
    unquoteHighLevelFroms = unquoteHigh;
  }

  @Override
  public void close() throws IOException
  {
    if (in != null)
    {
      in.close();
      in = null;
    }
  }

  public int getLineNumber()
  {
    return in.getLineNumber();
  }

  public boolean isEnvelopeLine(String line)
  {
    if (!line.startsWith(FROM))
    {
      return false;
    }
    final String[] parts = line.split(" ");
    final String year = parts[parts.length - 1];
    try
    {
      final int yearNumber = Integer.parseInt(year);
      return yearNumber >= 1970 && yearNumber < 10000;
    }
    catch (final NumberFormatException nfe)
    {
      return false;
    }
  }

  public MboxMessage next() throws IOException
  {
    String line;
    // find envelope line starting with "From "
    if (envelopeLine == null)
    {
      while (true)
      {
        line = in.readLine();
        if (line == null)
        {
          return null;
        }
        if (isEnvelopeLine(line))
        {
          envelopeLine = line;
          break;
        }
      }
    }
    final MboxMessage msg = new MboxMessage();
    msg.setEnvelope(envelopeLine, in.getLineNumber());
    envelopeLine = null;

    readHeader(msg);
    readBody(msg);

    return msg;
  }

  private void readHeader(MboxMessage msg) throws IOException
  {
    String line;
    final List<String> lines = new ArrayList<String>();
    while ((line = in.readLine()) != null)
    {
      if ("".equals(line))
      {
        break;
      }
      lines.add(line);
    }
    msg.setHeaderLines(lines);
  }

  private void readBody(MboxMessage msg) throws IOException
  {
    final List<String> lines = new ArrayList<String>();
    String line;
    while ((line = in.readLine()) != null)
    {
      if (isEnvelopeLine(line))
      {
        envelopeLine = line;
        break;
      }
      int index = 0;
      while (index < line.length() && '>' == line.charAt(index))
      {
        index++;
      }
      if (index > 0 && line.startsWith(FROM, index))
      {
        boolean unquote;
        if (unquoteHighLevelFroms)
        {
          unquote = true;
        }
        else
        {
          unquote = index == 1;
        }
        if (unquote)
        {
          line = line.substring(1);
        }
      }
      lines.add(line);
    }
    msg.setBodyLines(lines);
  }
}
