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
package messy.msgdata.formats.anews;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link ANewsMessage} class.
 */
public class ANewsMessageTest
{
  private static final String[] LINES = new String[]
  {
      "Salutation", "This is a sentence."
  };
  private static final String PATH_1 = "srv1";
  private static final String PATH_2 = "me";
  private static final String PATH = PATH_1 + "!" + PATH_2;
  private ANewsMessage msg;
  private final Date sent = new Date(123456L);
  private final String sentString = sent.toString();
  private final String msgId = "abc.123";
  private final String newsgroups = "group1,group2";
  private final String subject = "Message description";
  private final List<String> lines = Arrays.asList(LINES);

  @Before
  public void setUp()
  {
    msg = new ANewsMessage();
    msg.setPath(PATH);
    msg.setDate(sent);
    msg.setDateString(sentString);
    msg.setBodyLines(lines);
    msg.setMessageId(msgId);
    msg.setNewsgroups(newsgroups);
    msg.setSubject(subject);
  }

  @Test
  public void testGetters()
  {
    Assert.assertEquals("From must be last part of path element.", PATH_2, msg.getFrom());
    Assert.assertEquals("Date content must be identical.", sent.getTime(), msg.getDate().getTime());
    Assert.assertEquals("Date string must be identical.", sentString, msg.getDateString());
    Assert.assertEquals("Message ID must be identical.", msgId, msg.getMessageId());
    Assert.assertEquals("Newsgroups must be identical.", newsgroups, msg.getNewsgroups());
    Assert.assertEquals("Path must be identical.", PATH, msg.getPath());
    Assert.assertEquals("Subject must be identical.", subject, msg.getSubject());
    final List<String> msgLines = msg.getBodyLines();
    Assert.assertNotNull("Lines list must be non-null.", msgLines);
    Assert.assertEquals("Number of lines must be identical.", msgLines.size(), LINES.length);
  }

  @Test
  public void testDate()
  {
    final ANewsMessage m = new ANewsMessage();
    m.setDate(null);
    Assert.assertNull("Null assignment leads to null.", m.getDate());
  }

  @Test
  public void testPath()
  {
    final ANewsMessage m = new ANewsMessage();
    m.setPath(null);
    Assert.assertNull("Null assignment leads to null.", m.getPath());
    m.setPath("");
    Assert.assertNotNull("Empty assignment leads to non-null result.", m.getPath());
    Assert.assertNotNull("Empty assignment leads to non-null result.", m.getPathElements());
    final String[] elements = m.getPathElements();
    Assert.assertEquals("Empty array assignment leads to length zero array.", 0, elements.length);
  }

  @Test
  public void testPathElements()
  {
    final ANewsMessage m = new ANewsMessage();
    m.setPathElements(null);
    Assert.assertNull("Null assignment leads to null.", m.getPathElements());
    m.setPathElements(new String[]
    {});
    Assert.assertNotNull("Empty array assignment leads to non-null value.", m.getPathElements());
    Assert.assertEquals("Empty array assignment leads to length zero array.", 0, m.getPathElements().length);
    m.setPathElements(new String[]
    {
        PATH_1, PATH_2
    });
    Assert.assertNotNull("Non-empty array assignment leads to non-null value.", m.getPathElements());
    Assert.assertEquals("Two-element array assignment leads to length two array.", 2, m.getPathElements().length);

  }
}
