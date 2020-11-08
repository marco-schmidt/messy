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
package messy.msgio.formats.anews;

import java.util.ArrayList;
import java.util.List;
import messy.msgdata.formats.anews.ANewsMessage;

/**
 * Converts a list of strings to an {@link messy.msgdata.formats.anews.ANewsMessage} object.
 *
 * @author Marco Schmidt
 */
public final class ANewsMessageConverter
{
  private static final int NUM_HEADER_LINES = 5;

  /**
   * Prevent instantiation of this class.
   */
  private ANewsMessageConverter()
  {
  }

  /**
   * Convert the argument list of strings to an {@link ANewsMessage} object, <code>null</code> on failure.
   *
   * @param list
   *          strings with a complete message in the A News format
   * @return {@link ANewsMessage} object or <code>null</code> if the conversion did not succeed
   */
  public static ANewsMessage fromLines(List<String> list)
  {
    if (list == null || list.size() <= NUM_HEADER_LINES)
    {
      return null;
    }
    // first line: marker "A" followed by the message ID
    String line = list.get(0);
    if (!line.startsWith("A") || line.length() < 2)
    {
      return null;
    }
    String messageId = line.substring(1);
    ANewsMessage msg = new ANewsMessage();
    msg.setMessageId(messageId);
    // second line: comma-separated list of newsgroups
    msg.setNewsgroups(list.get(1));
    // third line: path, elements separated by !
    msg.setPath(list.get(2));
    // fourth line: date
    String dateString = list.get(3);
    msg.setDateString(dateString);
    msg.setDate(new java.util.Date()/* SentExtraction.extractDate(dateString) */);
    // fifth line: Subject
    msg.setSubject(list.get(4));
    // all remaining lines are part of the body
    int index = NUM_HEADER_LINES;
    List<String> bodyLines = new ArrayList<String>(list.size() - NUM_HEADER_LINES);
    while (index < list.size())
    {
      bodyLines.add(list.get(index++));
    }
    msg.setBodyLines(bodyLines);
    return msg;
  }
}
