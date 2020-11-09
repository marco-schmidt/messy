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
  private static final String[] LINES_ARRAY = new String[]
  {
      "Salutation", "This is a sentence."
  };
  private static final String PATH_1 = "srv1";
  private static final String PATH_2 = "me";
  private static final String PATH = PATH_1 + "!" + PATH_2;
  private ANewsMessage msg;
  private static final Date SENT = new Date(123456L);
  private static final String SENT_STRING = SENT.toString();
  private static final String MESSAGE_ID = "abc.123";
  private static final String NEWSGROUPS = "group1,group2";
  private static final String SUBJECT = "Message description";
  private static final List<String> LINES = Arrays.asList(LINES_ARRAY);

  @Before
  public void setUp()
  {
    msg = new ANewsMessage();
    msg.setPath(PATH);
    msg.setDate(SENT);
    msg.setDateString(SENT_STRING);
    msg.setBodyLines(LINES);
    msg.setMessageId(MESSAGE_ID);
    msg.setNewsgroups(NEWSGROUPS);
    msg.setSubject(SUBJECT);
  }

  @Test
  public void testGetters()
  {
    Assert.assertEquals("From must be last part of path element.", PATH_2, msg.getFrom());
    Assert.assertEquals("Date content must be identical.", SENT.getTime(), msg.getDate().getTime());
    Assert.assertEquals("Date string must be identical.", SENT_STRING, msg.getDateString());
    Assert.assertEquals("Message ID must be identical.", MESSAGE_ID, msg.getMessageId());
    Assert.assertEquals("Newsgroups must be identical.", NEWSGROUPS, msg.getNewsgroups());
    Assert.assertEquals("Path must be identical.", PATH, msg.getPath());
    Assert.assertEquals("Subject must be identical.", SUBJECT, msg.getSubject());
    final List<String> msgLines = msg.getBodyLines();
    Assert.assertNotNull("Lines list must be non-null.", msgLines);
    Assert.assertEquals("Number of LINES_ARRAY must be identical.", msgLines.size(), LINES_ARRAY.length);
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
