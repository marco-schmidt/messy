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
package messy.msgio.formats.imf;

import java.util.Iterator;
import java.util.List;
import messy.msgdata.formats.imf.ImfHeaderField;
import messy.msgdata.formats.imf.ImfHeaderList;

/**
 * Parse Internet Message Format data.
 *
 * @author Marco Schmidt
 */
public class ImfParser
{
  /**
   * Character to separate an IMF header field name from its body.
   */
  public static final char HEADER_FIELD_SEPARATOR = ':';

  private void add(ImfHeaderList list, String fieldName, StringBuffer sb)
  {
    if (sb != null)
    {
      final ImfHeaderField header = new ImfHeaderField(fieldName, sb.toString());
      list.add(header);
    }
  }

  public ImfHeaderList createMessageHeaderList(List<String> headerLines)
  {
    if (headerLines == null)
    {
      return null;
    }
    final Iterator<String> iter = headerLines.iterator();
    final ImfHeaderList list = new ImfHeaderList();
    StringBuffer sb = null;
    String fieldName = null;
    while (iter.hasNext())
    {
      String s = iter.next();
      if (s.isEmpty())
      {
        continue;
      }
      final char c = s.charAt(0);
      if (Character.isWhitespace(c))
      {
        // leading whitespace => continuation of header
        if (sb != null)
        {
          s = s.substring(1);
          while (s.length() > 0 && Character.isWhitespace(s.charAt(0)))
          {
            s = s.substring(1);
          }
          sb.append(s);
        }
      }
      else
      {
        // new header
        // store old one if there is something in the StringBuffer
        add(list, fieldName, sb);
        fieldName = null;
        sb = null;
        // search for separation character
        final int index = s.indexOf(HEADER_FIELD_SEPARATOR);
        if (index > -1)
        {
          fieldName = s.substring(0, index);
          s = s.substring(index + 1);
          while (s.length() > 0 && s.charAt(0) == ' ')
          {
            s = s.substring(1);
          }
          sb = new StringBuffer(s);
        }
      }
    }
    add(list, fieldName, sb);
    return list;
  }
}
