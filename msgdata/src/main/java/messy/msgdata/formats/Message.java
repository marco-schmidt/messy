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
package messy.msgdata.formats;

import java.util.Date;

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
  private String authorName;
  private String format;
  private String medium;
  private String messageId;
  private Date sent;
  private String subject;

  public String getAuthorName()
  {
    return authorName;
  }

  public void setAuthorName(String authorName)
  {
    this.authorName = authorName;
  }

  public String getFormat()
  {
    return format;
  }

  public void setFormat(String format)
  {
    this.format = format;
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
}
