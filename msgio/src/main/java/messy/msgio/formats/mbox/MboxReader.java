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
   * Mbox subtypes, identified from envelope lines.
   */
  public enum MboxType
  {
    /**
     * Full UseNet backup mbox files, 'From ' not quoted.
     */
    FUNBACKUP_TRAILER,
    /**
     * Usenet Historical Collection mbox files, envelope lines with a large integer number, 'From ' not quoted.
     */
    LARGE_INTEGER,
    /**
     * Mbox with quoted 'From '.
     */
    REGULAR
  };

  /**
   * Is high level unquoting enabled by default?
   */
  public static final boolean DEFAULT_HIGH_LEVEL_FROM_UNQUOTING = true;
  private static final String FROM = "From ";
  private LineNumberReader in;
  private boolean unquoteHighLevelFroms = DEFAULT_HIGH_LEVEL_FROM_UNQUOTING;
  private String envelopeLine;
  private MboxType mboxType;

  public MboxReader(Reader reader)
  {
    this(reader, DEFAULT_HIGH_LEVEL_FROM_UNQUOTING);
  }

  public MboxReader(Reader reader, boolean unquoteHigh)
  {
    in = new LineNumberReader(reader);
    unquoteHighLevelFroms = unquoteHigh;
  }

  public static boolean identifyEnvelopeFunbackup(String line)
  {
    return line.endsWith(" FUNBACKUP");
  }

  public static boolean identifyEnvelopeLargeInteger(String line)
  {
    if (line == null || line.length() < FROM.length() + 1)
    {
      return false;
    }
    final String s = line.substring(FROM.length());
    final char[] array = s.toCharArray();
    int index = 0;
    if (array[0] == '-')
    {
      index++;
    }
    while (index < array.length)
    {
      final char c = array[index++];
      if (c < '0' || c > '9')
      {
        break;
      }
    }
    return index == array.length;
  }

  public static MboxType identifyEnvelope(String line)
  {
    if (line == null || !line.startsWith(FROM))
    {
      return null;
    }
    if (identifyEnvelopeLargeInteger(line))
    {
      return MboxType.LARGE_INTEGER;
    }
    if (identifyEnvelopeFunbackup(line))
    {
      return MboxType.FUNBACKUP_TRAILER;
    }
    return MboxType.REGULAR;
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

    // first call to this method, identify mbox type
    if (mboxType == null)
    {
      mboxType = identifyEnvelope(line);
      if (mboxType != MboxType.REGULAR)
      {
        unquoteHighLevelFroms = false;
      }
    }

    switch (mboxType)
    {
    case FUNBACKUP_TRAILER:
      return identifyEnvelopeFunbackup(line);
    case LARGE_INTEGER:
      return identifyEnvelopeLargeInteger(line);
    default:
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
