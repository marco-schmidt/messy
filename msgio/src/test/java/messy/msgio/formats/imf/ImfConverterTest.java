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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.imf.ImfHeaderField;
import messy.msgdata.formats.imf.ImfHeaderList;
import messy.msgdata.formats.imf.ImfMessage;

/**
 * Test {@link ImfConverter}.
 *
 * @author Marco Schmidt
 */
public class ImfConverterTest
{
  @Test
  public void testNoUsenet()
  {
    final ImfHeaderList list = new ImfHeaderList();
    final ImfMessage input = new ImfMessage(list, new ArrayList<String>());
    final ImfConverter converter = new ImfConverter();
    final Message output = converter.convert(input);
    Assert.assertNull("Expect null result.", output);
  }

  @Test
  public void testPreRfc850()
  {
    final ImfHeaderList list = new ImfHeaderList();
    final String articleId = "abc.123";
    final String newsgroups = "net.general";
    final String title = "The subject line";
    final String dateString = "Fri Nov 19 16:14:55 1982";
    list.add(new ImfHeaderField("Newsgroups", newsgroups));
    list.add(new ImfHeaderField("Article-I.D.", articleId));
    list.add(new ImfHeaderField("Posted", dateString));
    list.add(new ImfHeaderField("Title", title));
    final ImfMessage input = new ImfMessage(list, new ArrayList<String>());
    final ImfConverter converter = new ImfConverter();
    final Message output = converter.convert(input);
    Assert.assertNotNull("Expect non-null result.", output);
    Assert.assertEquals("Expect identical subject.", title, output.getSubject());
    final List<String> groups = output.getGroups();
    Assert.assertNotNull("Expect non-null groups.", groups);
    Assert.assertEquals("Expect one group.", 1, groups.size());
    Assert.assertEquals("Expect certain group.", newsgroups, groups.get(0));
  }

  @Test
  public void testNormalizeMessageId()
  {
    Assert.assertNull("Expect null result for null input.", ImfConverter.normalizeMessageId(null));
    final String noLeft = "sdffs3sd@example.org>";
    Assert.assertEquals("Expect identical if left angle bracket missing.", noLeft,
        ImfConverter.normalizeMessageId(noLeft));
    final String noRight = "<sdffs3sd@example.org";
    Assert.assertEquals("Expect identical if right angle bracket missing.", noRight,
        ImfConverter.normalizeMessageId(noRight));
    final String exact = "<sdffs3sd@example.org>";
    Assert.assertEquals("Expect identical if no extra data.", exact, ImfConverter.normalizeMessageId(exact));
    Assert.assertEquals("Expect message id for padded left.", exact, ImfConverter.normalizeMessageId("  " + exact));
    Assert.assertEquals("Expect message id for padded right.", exact, ImfConverter.normalizeMessageId(exact + " "));
  }

  @Test
  public void testRfc850()
  {
    final ImfHeaderList list = new ImfHeaderList();
    final String newsgroups = "net.general";
    final String subject = "Man";
    final Date date = new Date(1360882792000L);
    final String dateString = "Fri, 15 Feb 2013 00:59:52 +0200";

    list.add(new ImfHeaderField("Newsgroups", newsgroups));
    // https://en.wikipedia.org/wiki/MIME#Encoded-Word
    // https://en.wikipedia.org/wiki/Base64#Examples
    list.add(new ImfHeaderField("Subject", "=?iso-8859-1?B?TWFu?="));
    list.add(new ImfHeaderField("Date", dateString));
    final ImfMessage input = new ImfMessage(list, new ArrayList<String>());
    final ImfConverter converter = new ImfConverter();
    final Message output = converter.convert(input);
    Assert.assertNotNull("Expect non-null result.", output);
    Assert.assertEquals("Expect identical subject.", subject, output.getSubject());
    Assert.assertEquals("Expect identical date.", date, output.getSent());
  }

  @Test
  public void testBrokenSubjectEncodingRfc850()
  {
    final ImfHeaderList list = new ImfHeaderList();
    final String newsgroups = "net.general";
    final String subject = "=?not-a-valid-encoding?B?TWFu?=";
    list.add(new ImfHeaderField("Newsgroups", newsgroups));
    list.add(new ImfHeaderField("Subject", subject));
    final ImfMessage input = new ImfMessage(list, new ArrayList<String>());
    final ImfConverter converter = new ImfConverter();
    final Message output = converter.convert(input);
    Assert.assertNotNull("Expect non-null result.", output);
    Assert.assertEquals("Expect identical subject.", subject, output.getSubject());
  }

  @Test
  public void testExtractAuthorNull()
  {
    final Message msg = new Message();
    new ImfConverter().extractAuthor(msg, null);
    Assert.assertNull("Null input leads to null author id.", msg.getAuthorId());
    Assert.assertNull("Null input leads to null author name.", msg.getAuthorName());
  }

  @Test
  public void testExtractEmptyAuthor()
  {
    final Message msg = new Message();
    new ImfConverter().extractAuthor(msg, "<>");
    Assert.assertNull("Empty input leads to null author id.", msg.getAuthorId());
    Assert.assertEquals("Empty input leads to empty author name.", "", msg.getAuthorName());
  }

  @Test
  public void testRemoveUnwanted()
  {
    final Set<Character> unwanted = new HashSet<>();
    final ImfConverter conv = new ImfConverter();
    Assert.assertNull("Null input leads to null result.", conv.removeUnwantedFirst(null, unwanted));
    Assert.assertEquals("Empty input leads to empty result.", "", conv.removeUnwantedFirst("", unwanted));
    Assert.assertNull("Null input leads to null result.", conv.removeUnwantedLast(null, unwanted));
    Assert.assertEquals("Empty input leads to empty result.", "", conv.removeUnwantedLast("", unwanted));
  }

  @Test
  public void testDecodeBodyNoTextPlain()
  {
    final ImfConverter conv = new ImfConverter();
    final ImfHeaderList headerList = new ImfHeaderList();
    final ImfMessage inMsg = new ImfMessage(headerList, new ArrayList<>());
    final Message outMsg = new Message();
    final Map<String, String> headers = new HashMap<>();
    headers.put("content-transfer-encoding", "7bit");
    headers.put("content-type", "text/other");
    conv.decodeBody(inMsg, outMsg, headers);
    Assert.assertEquals("Empty message text.", "", outMsg.getText());
  }

  @Test
  public void testExtractArchive()
  {
    final ImfConverter conv = new ImfConverter();
    final Message msg = new Message();
    final List<String> lines = new ArrayList<>();
    final Map<String, String> headers = new HashMap<>();
    conv.parseArchiveStatus(msg, headers, lines);
    Assert.assertNull("No headers or body lines, null output.", msg.getArchive());

    msg.setArchive(null);
    lines.add("something unrelated.");
    conv.parseArchiveStatus(msg, headers, lines);
    Assert.assertNull("No headers and unrelated first body line, null output.", msg.getArchive());

    msg.setArchive(null);
    lines.clear();
    lines.add("x-no-archive: yes");
    conv.parseArchiveStatus(msg, headers, lines);
    Assert.assertEquals("No headers and XNAY first body line, output false.", Boolean.FALSE, msg.getArchive());

    msg.setArchive(null);
    lines.clear();
    headers.put("x-no-archive", "yes");
    conv.parseArchiveStatus(msg, headers, lines);
    Assert.assertEquals("XNAY header and empty body line, output false.", Boolean.FALSE, msg.getArchive());

    msg.setArchive(null);
    lines.clear();
    headers.clear();
    headers.put("x-no-archive", "nonsense");
    conv.parseArchiveStatus(msg, headers, lines);
    Assert.assertNull("XNA header with value other than 'yes' and empty body line, output null.", msg.getArchive());

    msg.setArchive(null);
    lines.clear();
    headers.clear();
    headers.put("archive", "nonsense");
    conv.parseArchiveStatus(msg, headers, lines);
    Assert.assertNull("Archive header with value other than 'yes' or 'no' and empty body line, output null.",
        msg.getArchive());

    msg.setArchive(null);
    lines.clear();
    headers.clear();
    headers.put("archive", "yes");
    conv.parseArchiveStatus(msg, headers, lines);
    Assert.assertEquals("Archive header with value 'yes', output true.", Boolean.TRUE, msg.getArchive());

    msg.setArchive(null);
    lines.clear();
    headers.clear();
    headers.put("archive", "no");
    conv.parseArchiveStatus(msg, headers, lines);
    Assert.assertEquals("Archive header with value 'no', output false.", Boolean.FALSE, msg.getArchive());
  }
}
