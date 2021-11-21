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
package messy.msgio.formats.anews;

import java.util.Arrays;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.anews.ANewsMessage;

/**
 * Test {@link ANewsMessageConverter} class.
 */
public class ANewsMessageConverterTest
{
  private static final String[] REGULAR =
  {
      "Amsg1", "news.misc", "foo!bar", "Sat Mar 28 17:56:20 1981", "Subject line", "First message body line."
  };

  @Test
  public void testNullInput()
  {
    Assert.assertNull("Null input yields null output.", ANewsMessageConverter.fromLines(null));
  }

  @Test
  public void testTooSmallInput()
  {
    Assert.assertNull("Empty input yields null output.", ANewsMessageConverter.fromLines(Arrays.asList(new String[]
    {})));
  }

  @Test
  public void testWrongSignatureInput()
  {
    Assert.assertNull("First line does not start with 'A' leads to null output.",
        ANewsMessageConverter.fromLines(Arrays.asList(new String[]
        {
            "X", "", "", "", "", ""
        })));
  }

  @Test
  public void testMissingMessageId()
  {
    Assert.assertNull("First line contains an 'A' only, leads to null output.",
        ANewsMessageConverter.fromLines(Arrays.asList(new String[]
        {
            "A", "", "", "", "", ""
        })));
  }

  @Test
  public void testMessageId()
  {
    final String id = "test.12";
    final ANewsMessage msg = ANewsMessageConverter.fromLines(Arrays.asList(new String[]
    {
        "A" + id, "", "", REGULAR[3], "", ""
    }));
    Assert.assertFalse("Correct A news lines lead to non-null message object.", msg == null);
    Assert.assertEquals("Message ID properly parsed.", id, msg.getMessageId());
  }

  @Test
  public void testCompleteMessage()
  {
    final ANewsMessage msg = ANewsMessageConverter.fromLines(Arrays.asList(REGULAR));
    Assert.assertNotNull("Message was properly parsed.", msg);
  }

  @Test
  public void testToMessage()
  {
    final Date date = new Date();
    final String from = "janedoe";
    final String mid = "jane44.55";
    final String subject = "Request for comments";

    final ANewsMessage input = new ANewsMessage();
    input.setDate(date);
    input.setFrom(from);
    input.setMessageId(mid);
    input.setSubject(subject);

    final Message output = ANewsMessageConverter.toMessage(input);

    Assert.assertEquals("Date got properly converted.", date, output.getSent());
    Assert.assertEquals("Format is A News.", ANewsMessageConverter.FORMAT_A_NEWS_NETNEWS, output.getFormat());
    Assert.assertEquals("From got properly converted.", from, output.getAuthorName());
    Assert.assertEquals("Medium is Usenet.", Message.MEDIUM_USENET, output.getMedium());
    Assert.assertEquals("Message ID got properly converted.", mid, output.getMessageId());
    Assert.assertEquals("Subject got properly converted.", subject, output.getSubject());
  }
}
