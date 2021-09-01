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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import messy.msgdata.formats.imf.ImfMessage;

/**
 * Test {@link ImfBodyDecoder}.
 *
 * @author Marco Schmidt
 */
public class ImfBodyDecoderTest
{
  @Test
  public void testParseContentTypeCorrectlyNoQuotes()
  {
    final String ct = "text/plain";
    final String charset = "iso-8859-1";
    final String input = ct + "; charset=" + charset + "  ";
    final Map<String, String> headers = new HashMap<>();
    headers.put("content-type", input);

    final Map<String, String> attr = new HashMap<>();
    final StringBuilder sb = new StringBuilder();

    ImfBodyDecoder.parseContentType(headers, sb, attr);

    Assert.assertEquals("Expect correct content type.", ct, sb.toString());
    final String attrCharset = attr.get("charset");
    Assert.assertNotNull("Expect non-null charset.", attrCharset);
    Assert.assertEquals("Expect correct charset.", charset, attrCharset);
  }

  @Test
  public void testParseContentTypeCorrectlyWithQuotes()
  {
    final String ct = "text/plain";
    final String charset = "iso-8859-1";
    final String input = ct + "; charset=\"" + charset + "\"  ";
    final Map<String, String> headers = new HashMap<>();
    headers.put("content-type", input);

    final Map<String, String> attr = new HashMap<>();
    final StringBuilder sb = new StringBuilder();

    ImfBodyDecoder.parseContentType(headers, sb, attr);

    Assert.assertEquals("Expect correct content type.", ct, sb.toString());
    final String attrCharset = attr.get("charset");
    Assert.assertNotNull("Expect non-null charset.", attrCharset);
    Assert.assertEquals("Expect correct charset.", charset, attrCharset);
  }

  @Test
  public void testParseContentTypeIncorrectNoQuotes()
  {
    final String ct = "text/plain";
    final String input = ct + "; charset";
    final Map<String, String> headers = new HashMap<>();
    headers.put("content-type", input);

    final Map<String, String> attr = new HashMap<>();
    final StringBuilder sb = new StringBuilder();

    ImfBodyDecoder.parseContentType(headers, sb, attr);

    Assert.assertEquals("Expect correct content type.", ct, sb.toString());
    final String attrCharset = attr.get("charset");
    Assert.assertNull("Expect null charset.", attrCharset);
  }

  @Test
  public void testDecodeTextNoContentTypeEncoding()
  {
    final Map<String, String> headers = new HashMap<>();
    headers.put("content-type", "text/plain");
    final List<String> list = ImfBodyDecoder.decodeText(new ImfMessage(null, new ArrayList<>()), headers);
    Assert.assertNotNull("Expect non-null result.", list);
    Assert.assertTrue("Expect empty result.", list.isEmpty());
  }

  @Test
  public void testDecodeTextUnknownContentTypeEncoding()
  {
    final Map<String, String> headers = new HashMap<>();
    headers.put("content-transfer-encoding", "unknown encoding");
    final List<String> list = ImfBodyDecoder.decodeText(new ImfMessage(null, new ArrayList<>()), headers);
    Assert.assertNotNull("Expect non-null result.", list);
    Assert.assertTrue("Expect empty result.", list.isEmpty());
  }

  @Test
  public void testDecodeLinesCorrectlyUtf8()
  {
    final List<String> input = new ArrayList<>();
    final String expected = "äöüÄÖÜß";
    final byte[] bytes = expected.getBytes(StandardCharsets.UTF_8);
    final String encoded = new String(bytes, StandardCharsets.ISO_8859_1);
    input.add(encoded);

    final Map<String, String> attr = new HashMap<>();
    attr.put("charset", "utf-8");

    final String cte = ImfBodyDecoder.CONTENT_TYPE_ENCODING_8_BIT;
    final String ct = ImfBodyDecoder.DEFAULT_CONTENT_TYPE;

    final List<String> list = ImfBodyDecoder.decodeLines(input, cte, ct, attr);

    Assert.assertNotNull("Expect non-null result.", list);
    Assert.assertEquals("Expect list size 1.", 1, list.size());
    final String actual = list.get(0);
    Assert.assertEquals("Expect correctly decoded line.", expected, actual);
  }

  @Test
  public void testDecodeLinesUnknwonCharset()
  {
    final List<String> input = new ArrayList<>();
    final String expected = "test 123";
    input.add(expected);

    final Map<String, String> attr = new HashMap<>();
    attr.put("charset", "this is not a correct charset name");

    final String cte = ImfBodyDecoder.CONTENT_TYPE_ENCODING_8_BIT;
    final String ct = ImfBodyDecoder.DEFAULT_CONTENT_TYPE;

    final List<String> list = ImfBodyDecoder.decodeLines(input, cte, ct, attr);

    Assert.assertNotNull("Expect non-null result.", list);
    Assert.assertEquals("Expect list size 1.", 1, list.size());
    final String actual = list.get(0);
    Assert.assertEquals("Expect identical input line.", expected, actual);
  }
}
