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
package messy.msgio.formats.hamster;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Read messages from <a href="https://de.wikipedia.org/wiki/Hamster_(Software)">Hamster</a> data.dat files.
 *
 * @author Marco Schmidt
 */
public class HamsterReader
{
  /**
   * Bit mask with lowest 31 bits set.
   */
  private static final long MASK_MESSAGE_LENGTH = Integer.MAX_VALUE;
  private final InputStream in;
  private final int maxMessageLength;
  private final boolean skipDeleted;

  public HamsterReader(InputStream is, int maxMessageLength, boolean skipDeleted)
  {
    this.in = is;
    this.maxMessageLength = maxMessageLength;
    this.skipDeleted = skipDeleted;
  }

  public HamsterReader(InputStream is, int maxMessageLength)
  {
    this(is, maxMessageLength, false);
  }

  public HamsterReader(InputStream is)
  {
    this(is, 16 * 1024 * 1024);
  }

  public static boolean isDataFileName(String name)
  {
    if (name == null)
    {
      return false;
    }
    final String expected = "data.dat";
    if (name.length() < expected.length())
    {
      return false;
    }
    final String lower = name.toLowerCase(Locale.ROOT);
    if (expected.equals(lower))
    {
      return true;
    }
    if (lower.endsWith(expected))
    {
      final char c = lower.charAt(lower.length() - expected.length() - 1);
      return c == '/' || c == '\\';
    }
    return false;
  }

  protected long readMessageLength() throws IOException
  {
    final long i1 = in.read();
    final long i2 = in.read();
    final long i3 = in.read();
    final long i4 = in.read();
    if (i1 == -1 || i2 == -1 || i3 == -1 || i4 == -1)
    {
      return -1;
    }
    return i1 | i2 << 8 | i3 << 16 | i4 << 24;
  }

  protected void skip(long numBytes) throws IOException
  {
    long left = numBytes;
    while (left > 0)
    {
      final long numSkipped = in.skip(left);
      left -= numSkipped;
    }
  }

  /**
   * Read next message.
   *
   * @return message as byte array or null at the end of input
   * @throws IOException
   *           on I/O error
   */
  public byte[] readNext() throws IOException
  {
    byte[] result = null;
    long messageLength;
    while ((messageLength = readMessageLength()) >= 0)
    {
      // if top bit is set message is marked as deleted
      final long maskedMessageLength = messageLength & MASK_MESSAGE_LENGTH;
      final boolean deleted = messageLength != maskedMessageLength;

      // determine read or skip
      boolean skip = skipDeleted && deleted;
      if (!skip)
      {
        skip = maskedMessageLength > maxMessageLength;
      }

      if (skip)
      {
        skip(maskedMessageLength);
      }
      else
      {
        result = readNext((int) maskedMessageLength);
        break;
      }
    }
    return result;
  }

  private byte[] readNext(final int length) throws IOException
  {
    int left = length;
    final byte[] result = new byte[length];
    int offset = 0;
    while (left > 0)
    {
      final int numRead = in.read(result, offset, left);
      if (numRead < 0)
      {
        return null;
      }
      left -= numRead;
      offset += numRead;
    }
    return result;
  }
}
