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

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Identify file formats using byte array signatures.
 *
 * @author Marco Schmidt
 */
public final class FileFormatHelper
{
  enum FileType
  {
    JSON, MBOX, UNKNOWN
  }

  private static int bytesToLoad;
  private static List<FileSignature> signatures;
  static
  {
    signatures = new ArrayList<>();
    // note: add longer signatures at the beginning in case a signature
    // is the prefix of another prefix
    signatures.add(new FileSignature(new byte[]
    {
        (byte) 'F', (byte) 'r', (byte) 'o', (byte) 'm', (byte) ' ',
    }, 0, FileType.MBOX));
    signatures.add(new FileSignature(new byte[]
    {
        (byte) '{'
    }, 0, FileType.JSON));
    bytesToLoad = findNumBytesToLoad(signatures);
  }

  private FileFormatHelper()
  {
    // prevent instantiation
  }

  private static int findNumBytesToLoad(List<FileSignature> list)
  {
    int max = 0;
    for (final FileSignature signature : list)
    {
      final int candidate = signature.getOffset() + signature.getSignature().length;
      max = Math.max(candidate, max);
    }
    return max;
  }

  protected static FileType identify(PushbackInputStream input)
  {
    try
    {
      // fill buffer
      final byte[] buffer = new byte[bytesToLoad];
      int leftToRead = bytesToLoad;
      final int index = 0;
      while (index < leftToRead)
      {
        final int numRead = input.read(buffer, index, leftToRead);
        if (numRead < 0)
        {
          break;
        }
        leftToRead -= numRead;
      }
      final int bufferSize = bytesToLoad - leftToRead;

      // put data back into input stream for regular decoding
      input.unread(buffer, 0, bufferSize);

      // try to match all signatures against buffer
      final Iterator<FileSignature> iter = signatures.iterator();
      while (iter.hasNext())
      {
        final FileSignature signature = iter.next();
        if (signature.matches(buffer, bufferSize))
        {
          return signature.getFileType();
        }
      }
    }
    catch (final IOException ioe)
    {
      return FileType.UNKNOWN;
    }

    return FileType.UNKNOWN;
  }
}
