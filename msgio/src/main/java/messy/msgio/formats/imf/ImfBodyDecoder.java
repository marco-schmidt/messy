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

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import messy.msgdata.formats.imf.ImfBodySection;
import messy.msgdata.formats.imf.ImfHeaderList;
import messy.msgdata.formats.imf.ImfMessage;

/**
 * Decode the body content of a message in the Internet Message Format (IMF).
 *
 * @author Marco Schmidt
 */
public final class ImfBodyDecoder
{
  /**
   * Normalized IMF header name for content transfer encoding.
   */
  public static final String CONTENT_TRANSFER_ENCODING = "content-transfer-encoding";
  /**
   * IMF content transfer encoding 7 bit.
   */
  public static final String CONTENT_TRANSFER_ENCODING_7_BIT = "7bit";
  /**
   * IMF content transfer encoding 8 bit.
   */
  public static final String CONTENT_TRANSFER_ENCODING_8_BIT = "8bit";
  /**
   * IMF content transfer encoding Base64.
   */
  public static final String CONTENT_TRANSFER_ENCODING_BASE_64 = "base64";
  /**
   * IMF content transfer encoding <em>quoted printable</em>.
   */
  public static final String CONTENT_TRANSFER_ENCODING_QUOTED_PRINTABLE = "quoted-printable";
  /**
   * Content type prefix for multiple parts (used with MIME).
   */
  public static final String CONTENT_TYPE_MULTIPART_PREFIX = "multipart";
  /**
   * Content type text / plain.
   */
  public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
  /**
   * Default content type for IMF, text/plain.
   */
  public static final String DEFAULT_CONTENT_TYPE = CONTENT_TYPE_TEXT_PLAIN;
  /**
   * Content type attribute boundary (used with MIME).
   */
  public static final String CONTENT_TYPE_ATTR_BOUNDARY = "boundary";
  /**
   * Content type attribute character set (used with MIME).
   */
  public static final String CONTENT_TYPE_ATTR_CHARSET = "charset";

  private static final Map<String, Charset> CHARSET_MAP = new HashMap<>();
  private static final Set<String> KNOWN_CONTENT_TRANSFER_ENCODINGS = new HashSet<>();
  static
  {
    // this could be Set.of in Java 9+
    KNOWN_CONTENT_TRANSFER_ENCODINGS.add(CONTENT_TRANSFER_ENCODING_7_BIT);
    KNOWN_CONTENT_TRANSFER_ENCODINGS.add(CONTENT_TRANSFER_ENCODING_8_BIT);
    KNOWN_CONTENT_TRANSFER_ENCODINGS.add(CONTENT_TRANSFER_ENCODING_BASE_64);
    KNOWN_CONTENT_TRANSFER_ENCODINGS.add(CONTENT_TRANSFER_ENCODING_QUOTED_PRINTABLE);
    populateCharsetMap(CHARSET_MAP);
  }

  private ImfBodyDecoder()
  {
  }

  private static void populateCharsetMap(final Map<String, Charset> charsetMap)
  {
    charsetMap.clear();
    for (final Map.Entry<String, Charset> entry : Charset.availableCharsets().entrySet())
    {
      final Charset charset = entry.getValue();
      final String normName = entry.getKey().toLowerCase(Locale.ROOT);
      charsetMap.put(normName, charset);
    }
  }

  protected static int extract(String s, int initialIndex, StringBuilder result, char delim, boolean endOfInputAllowed)
  {
    final int length = s.length();
    boolean quoted = false;

    int index = initialIndex;
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
    String charsetName = contentTypeAttr.get(CONTENT_TYPE_ATTR_CHARSET);
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

    if (CONTENT_TRANSFER_ENCODING_BASE_64.equals(contentTransferEncoding))
    {
      return decodeLinesBase64(bodyLines, charset);
    }
    if (CONTENT_TRANSFER_ENCODING_QUOTED_PRINTABLE.equals(contentTransferEncoding))
    {
      return decodeLinesQuotedPrintable(bodyLines, charset);
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

  private static List<String> decodeLinesBase64(List<String> bodyLines, Charset charset)
  {
    final Decoder base64 = Base64.getDecoder();
    final CharArrayWriter out = new CharArrayWriter(0);
    for (final String line : bodyLines)
    {
      try
      {
        final byte[] bytes = base64.decode(line);
        final String chars = new String(bytes, charset);
        out.append(chars);
      }
      catch (final IllegalArgumentException iae)
      {
      }
    }
    final String text = out.toString();
    final String[] strings = text.split("\r\n");
    return Arrays.asList(strings);
  }

  protected static byte[] decodeQuotedPrintable(String s)
  {
    if (s == null || s.isEmpty())
    {
      return new byte[0];
    }
    final int length = s.length();
    final ByteArrayOutputStream out = new ByteArrayOutputStream(s.length());
    int currentOffset = 0;
    do
    {
      final char c = s.charAt(currentOffset++);
      if (c == '=')
      {
        if (currentOffset + 1 >= length)
        {
          break;
        }
        final char c1 = s.charAt(currentOffset++);
        final char c2 = s.charAt(currentOffset++);
        final int value = Character.digit(c1, 16) * 16 + Character.digit(c2, 16);
        out.write(value);
      }
      else
      {
        if (c == '_')
        {
          out.write(' ');
        }
        else
        {
          out.write(c & 0xff);
        }
      }
    }
    while (currentOffset < length);
    return out.toByteArray();
  }

  protected static List<String> decodeLinesQuotedPrintable(List<String> bodyLines, Charset charset)
  {
    final List<String> result = new ArrayList<>();
    byte[] buffer = null;
    for (final String line : bodyLines)
    {
      final byte[] bytes = decodeQuotedPrintable(line);
      buffer = concat(buffer, bytes);
      final boolean spanned = line.endsWith("=");
      if (!spanned)
      {
        final String outputLine = new String(buffer, charset);
        result.add(outputLine);
        buffer = null;
      }
    }
    if (buffer != null)
    {
      result.add(new String(buffer, charset));
    }
    return result;
  }

  private static byte[] concat(byte[] buffer, byte[] bytes)
  {
    if (buffer == null)
    {
      return bytes;
    }
    else
    {
      final byte[] tmp = new byte[buffer.length + bytes.length];
      System.arraycopy(buffer, 0, tmp, 0, buffer.length);
      System.arraycopy(bytes, 0, tmp, buffer.length, bytes.length);
      return tmp;
    }
  }

  public static void decode(ImfMessage message, Map<String, String> headers)
  {
    final ImfBodySection section = decodeSection(headers, message.getBodyLines());
    final String contentType = section.getContentType();
    final boolean mime = contentType.startsWith(CONTENT_TYPE_MULTIPART_PREFIX + "/");
    if (mime)
    {
      decodeMime(message, section);
    }
    else
    {
      message.getBodySections().add(section);
    }
  }

  private static ImfBodySection decodeSection(Map<String, String> headers, List<String> textLines)
  {
    final ImfBodySection result = new ImfBodySection();

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
      contentType = contentTypeBuilder.toString().toLowerCase(Locale.ROOT);
    }
    result.setContentType(contentType);
    result.setContentTypeAttributes(contentTypeAttr);

    // make sure content type encoding is supported
    String cte = headers.get(CONTENT_TRANSFER_ENCODING);
    if (cte == null)
    {
      cte = CONTENT_TRANSFER_ENCODING_8_BIT;
    }
    cte = cte.trim().toLowerCase(Locale.ROOT);
    if (!KNOWN_CONTENT_TRANSFER_ENCODINGS.contains(cte))
    {
      result.setLines(new ArrayList<>());
      return result;
    }

    if (!contentType.startsWith(CONTENT_TYPE_MULTIPART_PREFIX))
    {
      final List<String> decodedLines = decodeLines(textLines, cte, contentType, contentTypeAttr);
      result.setLines(decodedLines);
    }
    return result;
  }

  private static void decodeMime(ImfMessage message, ImfBodySection section)
  {
    final Map<String, String> contentTypeAttr = section.getContentTypeAttributes();
    final String boundary = contentTypeAttr.get(CONTENT_TYPE_ATTR_BOUNDARY);
    if (boundary == null)
    {
      return;
    }
    final List<List<String>> stringLists = new ArrayList<>();
    final List<String> bodyLines = message.getBodyLines();
    List<String> currentList = null;
    final Iterator<String> iter = bodyLines.iterator();
    while (iter.hasNext())
    {
      final String line = iter.next();
      if (boundary.equals(line))
      {
        currentList = new ArrayList<>();
        stringLists.add(currentList);
      }
      else
      {
        if (currentList != null)
        {
          currentList.add(line);
        }
      }
    }
    decodeMultipartSections(message, stringLists);
  }

  private static void decodeMultipartSections(ImfMessage message, List<List<String>> stringLists)
  {
    final List<ImfBodySection> bodySections = message.getBodySections();
    for (final List<String> list : stringLists)
    {
      final ImfBodySection section = decodeMultipartSection(list);
      bodySections.add(section);
    }
  }

  private static ImfBodySection decodeMultipartSection(List<String> lines)
  {
    final List<String> headerLines = new ArrayList<>();
    final Iterator<String> iter = lines.iterator();
    while (iter.hasNext())
    {
      final String line = iter.next();
      iter.remove();
      if (line.isEmpty())
      {
        break;
      }
      headerLines.add(line);
    }

    final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(headerLines);
    final Map<String, String> lookup = new ImfConverter().createLookup(headerList);
    return decodeSection(lookup, lines);
  }
}
