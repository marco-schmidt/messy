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
import messy.msgdata.formats.imf.ImfMessage;

/**
 * General message data class.
 *
 * Objects of this class typically are the result of a conversion process from some format-specific message class like
 * {@link ImfMessage}.
 *
 * @author Marco Schmidt
 */
public class Message
{
  /**
   * A property of a message. Use as parameter in {@link Message#get(Item)}.
   */
  public enum Item
  {
    /**
     * Statement of author about willingness to include message in archive.
     */
    ARCHIVE,
    /**
     * Address to identify an author, e.g. Twitter's numerical user ID or an e-mail address.
     */
    AUTHOR_ID,
    /**
     * Name of author, either real-life name, pseudonym or screen name.
     */
    AUTHOR_NAME,
    /**
     * Code of country from which message originated.
     */
    COUNTRY_CODE,
    /**
     * Format of message.
     */
    FORMAT,
    /**
     * Discussion forums, newsgroups to which message got sent.
     */
    GROUPS,
    /**
     * Code of natural language used to write message.
     */
    LANG_CODE,
    /**
     * The message got sent to which discussion medium (like Usenet or Twitter).
     */
    MEDIUM,
    /**
     * Value to identify a particular message.
     */
    MESSAGE_ID,
    /**
     * Organization from whose system the message was sent.
     */
    ORGANIZATION,
    /**
     * Hostname of system from which message got sent.
     */
    POSTING_HOST,
    /**
     * IP address of system from which message got sent.
     */
    POSTING_IP_ADDRESS,
    /**
     * IPv4 address of system from which message got sent.
     */
    POSTING_IPV4_ADDRESS,
    /**
     * Message ID values of ancestor messages in a thread.
     */
    REFERENCES,
    /**
     * Date and time when message got sent.
     */
    SENT,
    /**
     * Subject line of message, summarizing its purpose.
     */
    SUBJECT,
    /**
     * List of tags, short strings relevant to this message.
     */
    TAGS,
    /**
     * Main text of message.
     */
    TEXT
  };

  /**
   * Constant for medium Usenet.
   */
  public static final String MEDIUM_USENET = "usenet";
  /**
   * Constant for medium Twitter.
   */
  public static final String MEDIUM_TWITTER = "twitter";

  private Boolean archive;
  private String authorId;
  private String authorName;
  private String countryCode;
  private String format;
  private List<String> groups;
  private String languageCode;
  private String medium;
  private String messageId;
  private String organization;
  private String postingHost;
  private String postingIpAddress;
  private Long postingIpv4Address;
  private List<String> references;
  private Date sent;
  private String subject;
  private List<String> tags;
  private String text;

  public Boolean getArchive()
  {
    return archive;
  }

  public void setArchive(Boolean archive)
  {
    this.archive = archive;
  }

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

  public String getOrganization()
  {
    return organization;
  }

  public void setOrganization(String organization)
  {
    this.organization = organization;
  }

  public String getPostingHost()
  {
    return postingHost;
  }

  public void setPostingHost(String postingHost)
  {
    this.postingHost = postingHost;
  }

  public Long getPostingIpv4Address()
  {
    return postingIpv4Address;
  }

  public void setPostingIpv4Address(Long postingIpv4Address)
  {
    this.postingIpv4Address = postingIpv4Address;
  }

  public String getPostingIpAddress()
  {
    return postingIpAddress;
  }

  public void setPostingIpAddress(String postingIpAddress)
  {
    this.postingIpAddress = postingIpAddress;
  }

  public List<String> getReferences()
  {
    return references;
  }

  public void setReferences(List<String> references)
  {
    this.references = references;
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

  public List<String> getTags()
  {
    return tags;
  }

  public void setTags(List<String> tags)
  {
    this.tags = tags;
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  public Object get(Item item)
  {
    switch (item)
    {
    case ARCHIVE:
      return getArchive();
    case AUTHOR_ID:
      return getAuthorId();
    case AUTHOR_NAME:
      return getAuthorName();
    case COUNTRY_CODE:
      return getCountryCode();
    case FORMAT:
      return getFormat();
    case GROUPS:
      return getGroups();
    case LANG_CODE:
      return getLanguageCode();
    case MEDIUM:
      return getMedium();
    case MESSAGE_ID:
      return getMessageId();
    case ORGANIZATION:
      return getOrganization();
    case POSTING_HOST:
      return getPostingHost();
    case POSTING_IP_ADDRESS:
      return getPostingIpAddress();
    case POSTING_IPV4_ADDRESS:
      return getPostingIpv4Address();
    case REFERENCES:
      return getReferences();
    case SENT:
      return getSent();
    case SUBJECT:
      return getSubject();
    case TAGS:
      return getTags();
    default:
      return getText();
    }
  }
}
