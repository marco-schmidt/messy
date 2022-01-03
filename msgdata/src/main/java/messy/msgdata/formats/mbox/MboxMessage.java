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
package messy.msgdata.formats.mbox;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import messy.msgdata.formats.RawMessage;

/**
 * Data class to store elements of an <a target="_top" href="http://www.faqs.org/rfcs/rfc822.html">RFC 822</a> message
 * encoded in <a target="_top" href="http://qmail.org/man/man5/mbox.html">mbox(5)</a> format.
 *
 * Elements include: envelope sender, envelope timestamp, header lines and body lines.
 *
 * @author Marco Schmidt
 */
public class MboxMessage extends RawMessage
{
  private static final String FROM = "From ";
  private String envSender;
  private Date envDate;

  public MboxMessage()
  {
    this(new ArrayList<>(), new ArrayList<>());
  }

  public MboxMessage(List<String> headerLines, List<String> bodyLines)
  {
    super(headerLines, bodyLines);
  }

  public Date getDate()
  {
    return envDate == null ? null : new Date(envDate.getTime());
  }

  public String getSender()
  {
    return envSender;
  }

  private SimpleDateFormat createFormatter()
  {
    final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH);
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    return sdf;
  }

  public void setEnvelope(String line, int lineNumber)
  {
    if (!line.startsWith(FROM) || line.length() < 23)
    {
      return;
    }

    // parse envelope sender
    int index = FROM.length();
    do
    {
      final char c = line.charAt(index++);
      if (c == ' ' || c == '\t')
      {
        envSender = line.substring(FROM.length(), index - 1);
        break;
      }
    }
    while (index < line.length());

    // parse envelope timestamp
    if (line.length() >= index + 24)

    {
      final String dateString = line.substring(index, index + 24);
      try
      {
        final SimpleDateFormat parser = createFormatter();
        envDate = parser.parse(dateString);
      }
      catch (final ParseException pe)
      {
        return;
      }
    }
  }

}
