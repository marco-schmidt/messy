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
package messy.msgdata.formats.imf;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Data class to store a single message as two lists of String objects for body and header section.
 *
 * @author Marco Schmidt
 */
public class RawMessage
{
  private List<String> headerLines;
  private List<String> bodyLines;

  public RawMessage(List<String> headerLines, List<String> bodyLines)
  {
    setBodyLines(bodyLines);
    setHeaderLines(headerLines);
  }

  public List<String> getHeaderLines()
  {
    return headerLines;
  }

  public void setHeaderLines(List<String> list)
  {
    headerLines = list;
  }

  public List<String> getBodyLines()
  {
    return bodyLines;
  }

  public void setBodyLines(List<String> list)
  {
    bodyLines = list;
  }

  private byte[] getLinesAsBytes(List<String> lines)
  {
    final ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
    final Iterator<String> iter = lines.iterator();
    while (iter.hasNext())
    {
      final String s = iter.next();
      final char[] array = s.toCharArray();
      for (int i = 0; i < array.length; i++)
      {
        bout.write(array[i] & 0xff);
      }
      bout.write(13);
      bout.write(10);
    }
    return bout.toByteArray();
  }

  public byte[] getBodyLinesAsBytes()
  {
    return getLinesAsBytes(bodyLines);
  }

  public byte[] getHeaderLinesAsBytes()
  {
    return getLinesAsBytes(headerLines);
  }
}
