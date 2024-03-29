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
package messy.msgdata.formats.anews;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A data class to store one NetNews message in <a href="https://en.wikipedia.org/wiki/A_News">A News format</a>.
 *
 * Package msgio's ANewsMessageConverter class can be used to create an object of this class from a list of strings.
 *
 * @author Marco Schmidt
 */
public class ANewsMessage
{
  /**
   * String separating parts in a path string.
   */
  public static final String PATH_SEPARATOR = "!";
  private String messageId;
  private String newsgroups;
  private String path;
  private Date date;
  private String dateString;
  private String subject;
  private List<String> bodyLines;
  private String[] pathElements;
  private String from;

  public List<String> getBodyLines()
  {
    return bodyLines;
  }

  public void setBodyLines(List<String> bodyLines)
  {
    this.bodyLines = bodyLines;
  }

  public Date getDate()
  {
    return date == null ? null : new Date(date.getTime());
  }

  public void setDate(Date newDate)
  {
    date = newDate == null ? null : new Date(newDate.getTime());
  }

  public String getDateString()
  {
    return dateString;
  }

  public void setDateString(String newDate)
  {
    dateString = newDate;
  }

  public String getMessageId()
  {
    return messageId;
  }

  public void setMessageId(String messageId)
  {
    this.messageId = messageId;
  }

  public String getNewsgroups()
  {
    return newsgroups;
  }

  public void setNewsgroups(String newsgroups)
  {
    this.newsgroups = newsgroups;
  }

  public String getPath()
  {
    return path;
  }

  public void setPath(String path)
  {
    this.path = path;
    if (path != null)
    {
      if ("".equals(path))
      {
        setPathElements(new String[]
        {});
        setFrom("");
      }
      else
      {
        final String[] parts = path.split(PATH_SEPARATOR);
        setPathElements(parts);
        setFrom(parts[parts.length - 1]);
      }
    }
  }

  public String getSubject()
  {
    return subject;
  }

  public void setSubject(String subject)
  {
    this.subject = subject;
  }

  public String[] getPathElements()
  {
    return pathElements == null ? null : Arrays.copyOf(pathElements, pathElements.length);
  }

  public void setPathElements(String[] pathElements)
  {
    this.pathElements = pathElements == null ? null : Arrays.copyOf(pathElements, pathElements.length);
  }

  public String getFrom()
  {
    return from;
  }

  public void setFrom(String from)
  {
    this.from = from;
  }
}
