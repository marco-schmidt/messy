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
package messy.msgio.formats.imf;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jakarta.mail.internet.MimeUtility;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.imf.ImfBodySection;
import messy.msgdata.formats.imf.ImfHeaderField;
import messy.msgdata.formats.imf.ImfHeaderList;
import messy.msgdata.formats.imf.ImfMessage;
import messy.msgio.utils.NetUtils;
import messy.msgio.utils.StringUtils;

/**
 * Convert {@link ImfMessage} to {@link Message} objects.
 *
 * @author Marco Schmidt
 */
public class ImfConverter
{
  /**
   * Format of Internet Message Format (RFC822) Usenet messages as defined by RFC850 and later.
   */
  public static final String FORMAT_IMF_NETNEWS_RFC_850 = "imf-netnews-rfc850";
  /**
   * Format of Internet Message Format (RFC822) Usenet messages before RFC850.
   */
  public static final String FORMAT_IMF_NETNEWS_PRE_RFC_850 = "imf-netnews-pre-rfc850";

  // shared header field names
  private static final String FIELD_ARCHIVE = "archive";
  private static final String FIELD_FROM = "from";
  private static final String[] FIELDS_HOST = new String[]
  {
      "nntp-posting-host", "x-nntp-posting-host", "x-original-nntp-posting-host"
  };
  private static final String FIELD_NEWSGROUPS = "newsgroups";
  private static final String FIELD_ORGANIZATION = "organization";
  private static final String FIELD_X_NO_ARCHIVE = "x-no-archive";
  private static final String FIELD_X_TRACE = "x-trace";

  // header field names B News before RFC850
  private static final String FIELD_ARTICLE_ID = "article-i.d.";
  private static final String FIELD_TITLE = "title";
  private static final String FIELD_POSTED = "posted";

  // header field names RFC850 and later
  private static final String FIELD_DATE = "date";
  private static final String FIELD_MESSAGE_ID = "message-id";
  private static final String FIELD_REFERENCES = "references";
  private static final String FIELD_SUBJECT = "subject";

  private static final String[] DATE_PATTERNS =
  {
      "EEE, dd MMM yyyy HH:mm:ss z", "dd MMM yyyy HH:mm:ss z", "dd MMM yy HH:mm:ss z", "EEE, dd MMM yy HH:mm:ss z",
      "EEE, dd MMM yyyy HH:mm:ss", "yyyy/MM/dd", "dd MMM yyyy HH:mm z", "dd MMM yyyy HH:mm:ss",
      "EEE, dd MMM yy HH:mm z", "EEE, dd MMM yy HH:mm:ss Z", "dd MMM yyyy HH:mm z", "EEE, dd MMM yyyy HH:mm",
  };
  private static final Set<Character> UNWANTED_MAIL_CHARS = new HashSet<>();
  static
  {
    // with Java 9+ this should be Set.of
    UNWANTED_MAIL_CHARS.add(Character.valueOf('<'));
    UNWANTED_MAIL_CHARS.add(Character.valueOf('>'));
    UNWANTED_MAIL_CHARS.add(Character.valueOf('"'));
    UNWANTED_MAIL_CHARS.add(Character.valueOf('('));
    UNWANTED_MAIL_CHARS.add(Character.valueOf(')'));
  }

  protected Map<String, String> createLookup(ImfHeaderList list)
  {
    final Map<String, String> result = new HashMap<>();
    for (int i = 0; i < list.size(); i++)
    {
      final ImfHeaderField field = list.get(i);
      final String name = field.getFieldName().toLowerCase(Locale.ROOT);
      final String body = field.getFieldBody();
      result.put(name, body);
    }
    return result;
  }

  public Message convert(ImfMessage message)
  {
    final Map<String, String> lookup = createLookup(message.getHeaderList());
    final String newsgroups = lookup.get(FIELD_NEWSGROUPS);
    if (newsgroups == null)
    {
      return null;
    }
    final String articleId = lookup.get(FIELD_ARTICLE_ID);
    if (articleId == null)
    {
      return convertRfc850(lookup, message);
    }
    else
    {
      return convertPreRfc850(lookup, message);
    }
  }

  public static String normalizeMessageId(String mid)
  {
    return normalizeMessageId(mid, 0);
  }

  /**
   * Extract a message ID, a string value in angle brackets, from a certain position of a given string.
   *
   * @param mid
   *          string to be searched
   * @param initialIndex
   *          zero-based index into string where searching begins
   * @return resulting message id including angle brackets on success, input string otherwise
   */
  public static String normalizeMessageId(String mid, int initialIndex)
  {
    if (mid == null)
    {
      return null;
    }
    int fromIndex = initialIndex;
    final int angleLeft = mid.indexOf('<', fromIndex);
    fromIndex = angleLeft < 0 ? 0 : angleLeft + 1;
    final int angleRight = mid.indexOf('>', fromIndex);
    if (angleLeft >= 0 && angleRight >= 0)
    {
      return mid.substring(angleLeft, angleRight + 1);
    }
    return mid;
  }

  public static List<String> extractMessageIdValues(String s, int initialIndex)
  {
    final List<String> result = new ArrayList<>();
    if (s == null)
    {
      return result;
    }
    final int maxIndex = s.length() - 3;

    int fromIndex = Math.max(0, initialIndex);
    while (fromIndex < maxIndex)
    {
      final int angleLeft = s.indexOf('<', fromIndex);
      if (angleLeft < 0)
      {
        break;
      }
      fromIndex = angleLeft + 1;
      final int angleRight = s.indexOf('>', fromIndex);
      if (angleRight < 0)
      {
        break;
      }
      fromIndex = angleRight + 1;
      final String mid = s.substring(angleLeft, fromIndex);
      result.add(mid);
    }
    return result;
  }

  public static List<String> extractReferences(String s)
  {
    return extractMessageIdValues(s, 0);
  }

  private Date decodeDate(String s, String pattern)
  {
    final SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.ROOT);
    try
    {
      return parser.parse(s);
    }
    catch (final ParseException pe)
    {
      return null;
    }
  }

  private Date decodeDate(String s)
  {
    if (s == null)
    {
      return null;
    }
    for (final String pattern : DATE_PATTERNS)
    {
      final Date result = decodeDate(s, pattern);
      if (result != null)
      {
        return result;
      }
    }
    return null;
  }

  private String decodeText(String input)
  {
    try
    {
      return input == null ? null : MimeUtility.decodeText(input);
    }
    catch (final UnsupportedEncodingException e)
    {
      return input;
    }
  }

  private Message convertShared(Map<String, String> lookup, ImfMessage message)
  {
    final Message result = new Message();
    parseFrom(result, lookup);
    extractOrigin(result, lookup);
    result.setOrganization(lookup.get(FIELD_ORGANIZATION));
    result.setGroups(StringUtils.splitAndNormalize(lookup.get(FIELD_NEWSGROUPS), ","));
    result.setReferences(extractReferences(lookup.get(FIELD_REFERENCES)));
    decodeBody(message, result, lookup);
    return result;
  }

  protected void decodeBody(ImfMessage message, Message result, Map<String, String> lookup)
  {
    ImfBodyDecoder.decode(message, lookup);
    final ImfBodySection section = message.findSectionByContentType(ImfBodyDecoder.CONTENT_TYPE_TEXT_PLAIN);
    final List<String> lines = section == null ? new ArrayList<>() : section.getLines();
    final String text = StringUtils.concatItems(lines, "\n");
    result.setText(text);
    parseArchiveStatus(result, lookup, lines);
  }

  /**
   * Parse <em>Archive:</em> header.
   *
   * @param value
   *          value for that header
   * @see https://datatracker.ietf.org/doc/html/rfc5536#section-3.2.2
   * @return Boolean, possibly null
   */
  private Boolean parseArchiveHeader(String value)
  {
    if (value == null)
    {
      return null;
    }
    final String lower = value.toLowerCase(Locale.ROOT);
    if ("yes".equals(lower))
    {
      return Boolean.TRUE;
    }
    if ("no".equals(lower))
    {
      return Boolean.FALSE;
    }
    return null;
  }

  /**
   * Parse <em>X-No-Archive</em> as header and in first line of body.
   *
   * @param value
   *          possibly null value of X-No-Archive header
   * @param lines
   *          body lines
   * @see https://en.wikipedia.org/wiki/X-No-Archive
   * @return Boolean, either null or Boolean.FALSE
   */
  private Boolean parseXNoArchive(String value, List<String> lines)
  {
    if (value != null)
    {
      final String lower = value.toLowerCase(Locale.ROOT);
      if ("yes".equals(lower))
      {
        return Boolean.FALSE;
      }
    }
    final Iterator<String> iter = lines.iterator();
    if (iter.hasNext())
    {
      final String line = iter.next().toLowerCase(Locale.ROOT).trim();
      if (line.startsWith(FIELD_X_NO_ARCHIVE + ": yes"))
      {
        return Boolean.FALSE;
      }
    }
    return null;
  }

  protected void parseArchiveStatus(Message message, Map<String, String> lookup, List<String> lines)
  {
    Boolean result = parseArchiveHeader(lookup.get(FIELD_ARCHIVE));
    if (result == null)
    {
      result = parseXNoArchive(lookup.get(FIELD_X_NO_ARCHIVE), lines);
    }
    message.setArchive(result);
  }

  private void parseFrom(Message result, Map<String, String> lookup)
  {
    String from = lookup.get(FIELD_FROM);
    if (from == null)
    {
      return;
    }
    from = decodeText(from);
    extractAuthor(result, from);
  }

  protected String removeUnwantedFirst(String s, Set<Character> unwanted)
  {
    if (s == null || s.isEmpty())
    {
      return s;
    }
    if (unwanted.contains(s.charAt(0)))
    {
      return s.substring(1);
    }
    else
    {
      return s;
    }
  }

  protected String removeUnwantedLast(String s, Set<Character> unwanted)
  {
    if (s == null || s.isEmpty())
    {
      return s;
    }
    final int lastIndex = s.length() - 1;
    if (unwanted.contains(s.charAt(lastIndex)))
    {
      return s.substring(0, lastIndex);
    }
    else
    {
      return s;
    }
  }

  private void extractOrigin(Message msg, String headerValue)
  {
    if (headerValue == null)
    {
      return;
    }
    final String[] items = headerValue.split(" ");
    for (final String item : items)
    {
      String cand = removeUnwantedFirst(item, UNWANTED_MAIL_CHARS);
      cand = removeUnwantedLast(cand, UNWANTED_MAIL_CHARS);
      final List<String> parts = StringUtils.splitAndNormalize(cand, "\\.");
      final Long ipv4 = NetUtils.parseDottedQuadsIpv4(parts);
      if (ipv4 == null)
      {
        if (NetUtils.isValidHostname(parts))
        {
          final String lastPart = parts.get(parts.size() - 1);
          if (NetUtils.isCountryCode(lastPart))
          {
            msg.setCountryCode(lastPart);
          }
          msg.setPostingHost(cand);
        }
      }
      else
      {
        msg.setPostingIpv4Address(ipv4);
        msg.setPostingIpAddress(cand);
      }
    }
  }

  protected void extractIpv4(Message msg, String[] items)
  {
    for (final String item : items)
    {
      final Long ipv4 = NetUtils.parseDottedQuadsIpv4(item);
      if (ipv4 != null)
      {
        msg.setPostingIpAddress(item);
        msg.setPostingIpv4Address(ipv4);
        break;
      }
    }
  }

  protected void extractOrigin(Message msg, Map<String, String> headers)
  {
    for (final String header : FIELDS_HOST)
    {
      extractOrigin(msg, headers.get(header));
    }
    // if no IP address was found try "X-Trace:"
    if (msg.getPostingIpAddress() == null)
    {
      String trace = headers.get(FIELD_X_TRACE);
      if (trace != null)
      {
        final int index = trace.indexOf('(');
        if (index > 0)
        {
          trace = trace.substring(0, index);
        }
        final String[] items = trace.split(" ");
        extractIpv4(msg, items);
      }
    }
  }

  protected void extractAuthor(Message result, String from)
  {
    if (from == null)
    {
      return;
    }
    final String[] items = from.split(" ");
    final ArrayList<String> nameElements = new ArrayList<>();
    String mailAddress = null;
    for (final String s : items)
    {
      String item = s.trim();
      item = removeUnwantedFirst(item, UNWANTED_MAIL_CHARS);
      item = removeUnwantedLast(item, UNWANTED_MAIL_CHARS);

      if (item.contains("@"))
      {
        mailAddress = item;
        continue;
      }

      if (!item.isEmpty())
      {
        nameElements.add(item);
      }
    }

    result.setAuthorId(mailAddress);
    result.setAuthorName(StringUtils.concatItems(nameElements));
  }

  protected List<String> extractTags(String s)
  {
    final List<String> result = new ArrayList<>();
    if (s == null)
    {
      return result;
    }
    int index2 = 0;
    do
    {
      final int index1 = s.indexOf('[', index2);
      if (index1 < 0)
      {
        break;
      }
      index2 = s.indexOf(']', index1 + 1);
      if (index2 < 0)
      {
        break;
      }
      final String tag = s.substring(index1 + 1, index2).trim().toLowerCase(Locale.ROOT);
      if (!tag.isEmpty())
      {
        result.add(tag);
      }
    }
    while (true);
    return result;
  }

  private Message convertPreRfc850(Map<String, String> lookup, ImfMessage message)
  {
    final Message result = convertShared(lookup, message);
    result.setMedium(Message.MEDIUM_USENET);
    result.setFormat(ImfMessage.FORMAT_INTERNET_MESSAGE_FORMAT);
    result.setSubject(lookup.get(FIELD_TITLE));
    result.setTags(extractTags(result.getSubject()));
    result.setMessageId(lookup.get(FIELD_ARTICLE_ID));
    result.setSent(decodeDate(lookup.get(FIELD_POSTED)));
    return result;
  }

  private Message convertRfc850(Map<String, String> lookup, ImfMessage message)
  {
    final Message result = convertShared(lookup, message);
    result.setMedium(Message.MEDIUM_USENET);
    result.setFormat(ImfMessage.FORMAT_INTERNET_MESSAGE_FORMAT);
    result.setSubject(decodeText(lookup.get(FIELD_SUBJECT)));
    result.setTags(extractTags(result.getSubject()));
    result.setMessageId(normalizeMessageId(lookup.get(FIELD_MESSAGE_ID)));
    result.setSent(decodeDate(lookup.get(FIELD_DATE)));
    return result;
  }
}
