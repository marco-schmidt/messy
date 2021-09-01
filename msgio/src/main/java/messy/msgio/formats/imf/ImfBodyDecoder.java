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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import messy.msgdata.formats.imf.ImfMessage;

/**
 * Decode the body content of a message in the Internet Message Format (IMF).
 *
 * @author Marco Schmidt
 */
public final class ImfBodyDecoder
{
  /**
   * IMF content-type encoding 7 bit.
   */
  public static final String CONTENT_TYPE_ENCODING_7_BIT = "7bit";
  /**
   * IMF content-type encoding 8 bit.
   */
  public static final String CONTENT_TYPE_ENCODING_8_BIT = "8bit";
  /**
   * Default content type for IMF, text/plain.
   */
  public static final String DEFAULT_CONTENT_TYPE = "text/plain";
  private static final Map<String, Charset> CHARSET_MAP = new HashMap<>();
  private static final Set<String> KNOWN_CONTENT_TYPES = new HashSet<>();
  static
  {
    KNOWN_CONTENT_TYPES.add(CONTENT_TYPE_ENCODING_7_BIT);
    KNOWN_CONTENT_TYPES.add(CONTENT_TYPE_ENCODING_8_BIT);
    populateCharsetMap(CHARSET_MAP);
  }

  private ImfBodyDecoder()
  {
  }

  private static void populateCharsetMap(Map<String, Charset> charsetMap)
  {
    CHARSET_MAP.clear();
    final SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
    for (final Map.Entry<String, Charset> entry : availableCharsets.entrySet())
    {
      final Charset charset = entry.getValue();
      final String normName = entry.getKey().toLowerCase(Locale.ROOT);
      CHARSET_MAP.put(normName, charset);
    }
  }

  protected static int extract(String s, int index, StringBuilder result, char delim, boolean endOfInputAllowed)
  {
    final int length = s.length();
    boolean quoted = false;

    while (index < length)
    {
      final char c = s.charAt(index++);
      final boolean quotes = c == '"';
      if (quotes)
      {
        quoted = !quoted;
      }
      else
      {
        if (!quoted && c == delim)
        {
          break;
        }
        else
        {
          result.append(c);
        }
      }
    }
    final boolean atEnd = index == length;
    if (atEnd && !endOfInputAllowed)
    {
      return -1;
    }
    return index;
  }

  public static void parseContentType(Map<String, String> headers, StringBuilder type, Map<String, String> attr)
  {
    final String ct = headers.get("content-type");
    parseContentType(ct, type, attr);
  }

  public static void parseContentType(String ct, StringBuilder type, Map<String, String> attr)
  {
    if (ct == null)
    {
      return;
    }
    final int length = ct.length();

    // first extract content type, e.g. text/plain
    int index = 0;
    index = extract(ct, index, type, ';', true);

    // now extract all key-value pairs
    while (index < length)
    {
      // key before =
      StringBuilder sb = new StringBuilder();
      index = extract(ct, index, sb, '=', false);
      if (index < 0)
      {
        break;
      }
      final String key = sb.toString().trim().toLowerCase(Locale.ROOT);

      // value until ;
      sb = new StringBuilder();
      index = extract(ct, index, sb, ';', true);
      final String value = sb.toString().trim();

      attr.put(key, value);
    }
  }

  protected static List<String> decodeLines(List<String> bodyLines, String contentTransferEncoding, String contentType,
      Map<String, String> contentTypeAttr)
  {
    String charsetName = contentTypeAttr.get("charset");
    if (charsetName == null)
    {
      return bodyLines;
    }
    charsetName = charsetName.toLowerCase(Locale.ROOT);
    final Charset charset = CHARSET_MAP.get(charsetName);
    if (charset == null)
    {
      return bodyLines;
    }

    final List<String> result = new ArrayList<>(bodyLines.size());
    for (final String line : bodyLines)
    {
      final byte[] bytes = line.getBytes(StandardCharsets.ISO_8859_1);
      final String decoded = new String(bytes, charset);
      result.add(decoded);
    }
    return result;
  }

  public static List<String> decodeText(ImfMessage message, Map<String, String> headers)
  {
    // make sure content type encoding is supported
    String cte = headers.get("content-transfer-encoding");
    if (cte == null)
    {
      cte = CONTENT_TYPE_ENCODING_8_BIT;
    }
    cte = cte.trim().toLowerCase(Locale.ROOT);
    if (!KNOWN_CONTENT_TYPES.contains(cte))
    {
      return new ArrayList<>();
    }

    // extract content type
    final StringBuilder contentTypeBuilder = new StringBuilder();
    final Map<String, String> contentTypeAttr = new HashMap<>();
    parseContentType(headers, contentTypeBuilder, contentTypeAttr);
    String contentType;
    if (contentTypeBuilder.length() == 0)
    {
      contentType = DEFAULT_CONTENT_TYPE;
    }
    else
    {
      contentType = contentTypeBuilder.toString();
    }

    return decodeLines(message.getBodyLines(), cte, contentType, contentTypeAttr);
  }
}
