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
package messy.msgio.formats.imf;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import jakarta.mail.internet.MimeUtility;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.imf.ImfHeaderField;
import messy.msgdata.formats.imf.ImfHeaderList;
import messy.msgdata.formats.imf.ImfMessage;

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

  // header field names B News before RFC850
  private static final String FIELD_ARTICLE_ID = "article-i.d.";
  private static final String FIELD_TITLE = "title";

  // header field names RFC850 and later
  private static final String FIELD_SUBJECT = "subject";
  private static final String FIELD_MESSAGE_ID = "message-id";

  private Map<String, String> createLookup(ImfHeaderList list)
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
    final String newsgroups = lookup.get("newsgroups");
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

  private String decodeText(String input)
  {
    try
    {
      return MimeUtility.decodeText(input);
    }
    catch (final UnsupportedEncodingException e)
    {
      return input;
    }
  }

  private Message convertPreRfc850(Map<String, String> lookup, ImfMessage message)
  {
    final Message result = new Message();
    result.setMedium(Message.MEDIUM_USENET);
    result.setFormat(ImfMessage.FORMAT_INTERNET_MESSAGE_FORMAT);
    result.setSubject(lookup.get(FIELD_TITLE));
    result.setMessageId(lookup.get(FIELD_ARTICLE_ID));
    return result;
  }

  private Message convertRfc850(Map<String, String> lookup, ImfMessage message)
  {
    final Message result = new Message();
    result.setMedium(Message.MEDIUM_USENET);
    result.setFormat(ImfMessage.FORMAT_INTERNET_MESSAGE_FORMAT);
    result.setSubject(decodeText(lookup.get(FIELD_SUBJECT)));
    result.setMessageId(normalizeMessageId(lookup.get(FIELD_MESSAGE_ID)));
    return result;
  }
}
