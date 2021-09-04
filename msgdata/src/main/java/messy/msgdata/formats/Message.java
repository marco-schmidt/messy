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
package messy.msgdata.formats;

import java.util.Date;
import java.util.List;

/**
 * General message data class.
 *
 * @author Marco Schmidt
 */
public class Message
{
  /**
   * Constant for medium Usenet.
   */
  public static final String MEDIUM_USENET = "usenet";
  /**
   * Constant for medium Twitter.
   */
  public static final String MEDIUM_TWITTER = "twitter";
  private String authorId;
  private String authorName;
  private String countryCode;
  private String format;
  private List<String> groups;
  private String languageCode;
  private String medium;
  private String messageId;
  private Date sent;
  private String subject;
  private String text;

  public String getAuthorId()
  {
    return authorId;
  }

  public void setAuthorId(String authorId)
  {
    this.authorId = authorId;
  }

  public String getAuthorName()
  {
    return authorName;
  }

  public void setAuthorName(String authorName)
  {
    this.authorName = authorName;
  }

  public String getCountryCode()
  {
    return countryCode;
  }

  public void setCountryCode(String countryCode)
  {
    this.countryCode = countryCode;
  }

  public String getFormat()
  {
    return format;
  }

  public void setFormat(String format)
  {
    this.format = format;
  }

  public List<String> getGroups()
  {
    return groups;
  }

  public void setGroups(List<String> groups)
  {
    this.groups = groups;
  }

  public String getLanguageCode()
  {
    return languageCode;
  }

  public void setLanguageCode(String languageCode)
  {
    this.languageCode = languageCode;
  }

  public String getMedium()
  {
    return medium;
  }

  public void setMedium(String medium)
  {
    this.medium = medium;
  }

  public String getMessageId()
  {
    return messageId;
  }

  public void setMessageId(String messageId)
  {
    this.messageId = messageId;
  }

  public Date getSent()
  {
    return sent == null ? null : new Date(sent.getTime());
  }

  public void setSent(Date sent)
  {
    this.sent = sent == null ? null : new Date(sent.getTime());
  }

  public String getSubject()
  {
    return subject;
  }

  public void setSubject(String subject)
  {
    this.subject = subject;
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }
}
