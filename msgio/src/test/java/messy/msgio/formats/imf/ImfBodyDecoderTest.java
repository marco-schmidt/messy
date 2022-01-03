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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import messy.msgdata.formats.imf.ImfBodySection;
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

  // @Test
  // public void testDecodeTextNoContentTypeEncoding()
  // {
  // final Map<String, String> headers = new HashMap<>();
  // headers.put("content-type", "text/plain");
  // final List<String> list = ImfBodyDecoder.decodeText(new ImfMessage(null, new ArrayList<>()), headers);
  // Assert.assertNotNull("Expect non-null result.", list);
  // Assert.assertTrue("Expect empty result.", list.isEmpty());
  // }
  //
  // @Test
  // public void testDecodeTextUnknownContentTypeEncoding()
  // {
  // final Map<String, String> headers = new HashMap<>();
  // headers.put("content-transfer-encoding", "unknown encoding");
  // final List<String> list = ImfBodyDecoder.decodeText(new ImfMessage(null, new ArrayList<>()), headers);
  // Assert.assertNotNull("Expect non-null result.", list);
  // Assert.assertTrue("Expect empty result.", list.isEmpty());
  // }

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

    final String cte = ImfBodyDecoder.CONTENT_TRANSFER_ENCODING_8_BIT;
    final String ct = ImfBodyDecoder.DEFAULT_CONTENT_TYPE;

    final List<String> list = ImfBodyDecoder.decodeLines(input, cte, ct, attr);

    Assert.assertNotNull("Expect non-null result.", list);
    Assert.assertEquals("Expect list size 1.", 1, list.size());
    final String actual = list.get(0);
    Assert.assertEquals("Expect correctly decoded line.", expected, actual);
  }

  @Test
  public void testDecodeLinesUnknownCharset()
  {
    final List<String> input = new ArrayList<>();
    final String expected = "test 123";
    input.add(expected);

    final Map<String, String> attr = new HashMap<>();
    attr.put("charset", "this is not a correct charset name");

    final String cte = ImfBodyDecoder.CONTENT_TRANSFER_ENCODING_8_BIT;
    final String ct = ImfBodyDecoder.DEFAULT_CONTENT_TYPE;

    final List<String> list = ImfBodyDecoder.decodeLines(input, cte, ct, attr);

    Assert.assertNotNull("Expect non-null result.", list);
    Assert.assertEquals("Expect list size 1.", 1, list.size());
    final String actual = list.get(0);
    Assert.assertEquals("Expect identical input line.", expected, actual);
  }

  @Test
  public void testDecodeLinesBase64()
  {
    final List<String> input = new ArrayList<>();
    input.add("SGksDQoNCnRoaXMgaXMgYSBzYW1wbGUgdGV4dCB0byBiZSBlbmNvZGVkIGluIGJhc2U2NCBz");
    input.add("byB0aGF0IGl0IGNhbiBiZQ0KZGVjb2RlZCBhZ2FpbiBpbiBhIHVuaXQgdGVzdC4gV2hhdCBl");
    input.add("bHNlIGlzIHRoZXJlIHRvIHNheT8NCg0KS2luZCByZWdhcmRzLA0KLS0gdGhlIGF1dGhvcg0K");

    final List<String> expected = new ArrayList<>();
    expected.add("Hi,");
    expected.add("");
    expected.add("this is a sample text to be encoded in base64 so that it can be");
    expected.add("decoded again in a unit test. What else is there to say?");
    expected.add("");
    expected.add("Kind regards,");
    expected.add("-- the author");

    final Map<String, String> attr = new HashMap<>();
    attr.put("charset", "us-ascii");

    final String cte = ImfBodyDecoder.CONTENT_TRANSFER_ENCODING_BASE_64;
    final String ct = ImfBodyDecoder.DEFAULT_CONTENT_TYPE;

    final List<String> list = ImfBodyDecoder.decodeLines(input, cte, ct, attr);

    Assert.assertNotNull("Expect non-null result.", list);
    Assert.assertEquals("Expect certain list size.", expected.size(), list.size());
    for (int i = 0; i < expected.size(); i++)
    {
      Assert.assertEquals("Expect identical line #" + i, expected.get(i), list.get(i));
    }
  }

  @Test
  public void testDecodeLinesBase64Broken()
  {
    final List<String> input = new ArrayList<>();
    input.add("SGksDQoNCnRoaXMgaXMgYSBzYW1 bGUgdGV4dCB0byBiZSBlbmNvZGVkIGluIGJhc2U2NCBz");

    final Map<String, String> attr = new HashMap<>();
    attr.put("charset", "us-ascii");

    final String cte = ImfBodyDecoder.CONTENT_TRANSFER_ENCODING_BASE_64;
    final String ct = ImfBodyDecoder.DEFAULT_CONTENT_TYPE;

    final List<String> list = ImfBodyDecoder.decodeLines(input, cte, ct, attr);

    Assert.assertNotNull("Expect non-null result.", list);
    Assert.assertEquals("Expect one line.", 1, list.size());
    Assert.assertEquals("Expect empty string.", "", list.get(0));
  }

  @Test
  public void testDecodeMimeNoBoundary()
  {
    final Map<String, String> headers = new HashMap<>();
    headers.put("content-type", "multipart/mixed");
    final ImfMessage msg = new ImfMessage(null, null);
    ImfBodyDecoder.decode(msg, headers);
    Assert.assertEquals("Expect one body section.", 0, msg.getBodySections().size());
  }

  @Test
  public void testDecodeMime()
  {
    final Map<String, String> headers = new HashMap<>();
    final String bound = "----bound";
    final String msgText = "Hi, this is the only plain text line.";
    headers.put("content-type", "multipart/mixed; boundary=\"" + bound + "\"");
    final List<String> body = new ArrayList<>();
    body.add("");
    body.add(bound);
    body.add("Content-Type: text/plain; charset=us-ascii");
    body.add("Content-Transfer-Encoding: 7bit");
    body.add("");
    body.add(msgText);
    body.add(bound);
    body.add("Content-Type: text/html; charset=us-ascii");
    body.add("Content-Transfer-Encoding: 7bit");
    body.add("");
    body.add("<html><body>" + msgText + "</body></html");

    final ImfMessage msg = new ImfMessage(null, body);
    ImfBodyDecoder.decode(msg, headers);
    Assert.assertEquals("Expect two body sections.", 2, msg.getBodySections().size());
    final ImfBodySection nonExisting = msg.findSectionByContentType("invalid/type");
    Assert.assertNull("Expect to not find non-existing body section.", nonExisting);
    final ImfBodySection section = msg.findSectionByContentType(ImfBodyDecoder.CONTENT_TYPE_TEXT_PLAIN);
    Assert.assertNotNull("Expect plain text body section.", section);
    final List<String> lines = section.getLines();
    Assert.assertEquals("Expect one line in plain text body section.", 1, lines.size());
    Assert.assertEquals("Expect original message.", msgText, lines.get(0));
  }

  @Test
  public void testDecodeMimeEmptyPart()
  {
    final Map<String, String> headers = new HashMap<>();
    final String bound = "----bound";
    final String msgText = "<html><body>This is HTML.</body></html";
    headers.put("content-type", "multipart/mixed; boundary=\"" + bound + "\"");
    final List<String> body = new ArrayList<>();
    body.add("");
    body.add(bound);
    body.add(bound);
    body.add("Content-Type: text/html; charset=us-ascii");
    body.add("Content-Transfer-Encoding: 7bit");
    body.add("");
    body.add(msgText);

    final ImfMessage msg = new ImfMessage(null, body);
    ImfBodyDecoder.decode(msg, headers);
    Assert.assertEquals("Expect two body sections.", 2, msg.getBodySections().size());
    final ImfBodySection section = msg.findSectionByContentType("text/html");
    Assert.assertNotNull("Expect html text body section.", section);
    final List<String> lines = section.getLines();
    Assert.assertEquals("Expect one line in plain text body section.", 1, lines.size());
    Assert.assertEquals("Expect original message.", msgText, lines.get(0));
  }

  @Test
  public void testDecodeUnknownContentTransferEncoding()
  {
    final Map<String, String> headers = new HashMap<>();
    headers.put("content-transfer-encoding", "invalid-encoding-name");
    headers.put("content-type", "text/plain");
    final List<String> body = new ArrayList<>();
    final ImfMessage msg = new ImfMessage(null, body);
    ImfBodyDecoder.decode(msg, headers);
    Assert.assertEquals("Expect one body section.", 1, msg.getBodySections().size());
  }

  @Test
  public void testDecodeQuotedPrintable()
  {
    byte[] a = ImfBodyDecoder.decodeQuotedPrintable(null);
    Assert.assertNotNull("Expect null input to lead to non-null output.", a);
    Assert.assertEquals("Expect output length 0.", 0, a.length);
    a = ImfBodyDecoder.decodeQuotedPrintable("");
    Assert.assertNotNull("Expect empty input to lead to non-null output.", a);
    Assert.assertEquals("Expect output length 0.", 0, a.length);
    a = ImfBodyDecoder.decodeQuotedPrintable("=FC");
    Assert.assertEquals("Expect output length 1.", 1, a.length);
    Assert.assertEquals("Expect input character.", (byte) 0xfc, a[0]);
    a = ImfBodyDecoder.decodeQuotedPrintable("=F");
    Assert.assertEquals("Expect output length 0 for broken input.", 0, a.length);
    a = ImfBodyDecoder.decodeQuotedPrintable("_");
    Assert.assertEquals("Expect output length 1.", 1, a.length);
    Assert.assertEquals("Expect input character.", (byte) 32, a[0]);
    a = ImfBodyDecoder.decodeQuotedPrintable("AB");
    Assert.assertEquals("Expect output length 2.", 2, a.length);
    Assert.assertEquals("Expect input character.", (byte) 65, a[0]);
    Assert.assertEquals("Expect input character.", (byte) 66, a[1]);
  }

  @Test
  public void testDecodeLinesQuotedPrintableRegular()
  {
    final List<String> input = new ArrayList<>();
    input.add("> > ich habe das Programm zwar nicht ausgef=FChrt, jedoch ist das meiner =");
    input.add("Meinung");

    final List<String> expected = new ArrayList<>();
    expected.add("> > ich habe das Programm zwar nicht ausgeführt, jedoch ist das meiner Meinung");

    final Map<String, String> attr = new HashMap<>();
    attr.put("charset", "iso-8859-1");

    final String cte = ImfBodyDecoder.CONTENT_TRANSFER_ENCODING_QUOTED_PRINTABLE;
    final String ct = ImfBodyDecoder.DEFAULT_CONTENT_TYPE;

    final List<String> list = ImfBodyDecoder.decodeLines(input, cte, ct, attr);

    Assert.assertNotNull("Expect non-null result.", list);
    Assert.assertEquals("Expect certain list size.", expected.size(), list.size());
    for (int i = 0; i < expected.size(); i++)
    {
      Assert.assertEquals("Expect identical line #" + i, expected.get(i), list.get(i));
    }
  }

  @Test
  public void testDecodeLinesQuotedPrintableLastLineSpanned()
  {
    final List<String> input = new ArrayList<>();
    input.add("> > ich habe das Programm zwar nicht ausgef=FChrt, jedoch ist das meiner =");
    input.add("Meinung");
    input.add("> > ich habe das Programm zwar nicht ausgef=FChrt, jedoch ist das meiner =");

    final List<String> expected = new ArrayList<>();
    expected.add("> > ich habe das Programm zwar nicht ausgeführt, jedoch ist das meiner Meinung");
    expected.add("> > ich habe das Programm zwar nicht ausgeführt, jedoch ist das meiner ");

    final Map<String, String> attr = new HashMap<>();
    attr.put("charset", "iso-8859-1");

    final String cte = ImfBodyDecoder.CONTENT_TRANSFER_ENCODING_QUOTED_PRINTABLE;
    final String ct = ImfBodyDecoder.DEFAULT_CONTENT_TYPE;

    final List<String> list = ImfBodyDecoder.decodeLines(input, cte, ct, attr);

    Assert.assertNotNull("Expect non-null result.", list);
    Assert.assertEquals("Expect certain list size.", expected.size(), list.size());
    for (int i = 0; i < expected.size(); i++)
    {
      Assert.assertEquals("Expect identical line #" + i, expected.get(i), list.get(i));
    }
  }
}
