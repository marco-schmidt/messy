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
package messy.msgdata.formats.mbox;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link MboxMessage}.
 *
 * @author Marco Schmidt
 */
public class MboxMessageTest
{
  @Test
  public void testEnvelopeRegularSpaces()
  {
    final MboxMessage msg = new MboxMessage();
    final String sender = "localpart@example.org";
    final Date timestamp = new Date(1360886392000L);
    final String timestampString = "Thu Feb 14 23:59:52 2013";
    msg.setEnvelope("From " + sender + " " + timestampString, 1);
    final String extractedSender = msg.getSender();
    Assert.assertNotNull("Expect sender to have been extracted.", extractedSender);
    Assert.assertEquals("Expect extracted sender to be equal to the one in envelope line.", sender, extractedSender);
    final Date extractedTimestamp = msg.getDate();
    Assert.assertNotNull("Expect timestamp to have been extracted.", extractedTimestamp);
    Assert.assertEquals("Expect extracted timestamp to be equal to the one in envelope line.", timestamp,
        extractedTimestamp);
  }

  @Test
  public void testEnvelopeRegularTabs()
  {
    final MboxMessage msg = new MboxMessage();
    final String sender = "localpart@example.org";
    final Date timestamp = new Date(1360886392000L);
    final String timestampString = "Thu Feb 14 23:59:52 2013";
    msg.setEnvelope("From " + sender + "\t" + timestampString, 1);
    final String extractedSender = msg.getSender();
    Assert.assertNotNull("Expect sender to have been extracted.", extractedSender);
    Assert.assertEquals("Expect extracted sender to be equal to the one in envelope line.", sender, extractedSender);
    final Date extractedTimestamp = msg.getDate();
    Assert.assertNotNull("Expect timestamp to have been extracted.", extractedTimestamp);
    Assert.assertEquals("Expect extracted timestamp to be equal to the one in envelope line.", timestamp,
        extractedTimestamp);
  }

  @Test
  public void testEnvelopeNoFrom()
  {
    final MboxMessage msg = new MboxMessage();
    msg.setEnvelope("This is a long enough string which does not resemble envelope lines at all.", 1);
    Assert.assertNull("Expect sender to be null.", msg.getSender());
    Assert.assertNull("Expect date to be null.", msg.getDate());
  }

  @Test
  public void testEnvelopeTooSmall()
  {
    final MboxMessage msg = new MboxMessage();
    msg.setEnvelope("From someone", 1);
    Assert.assertNull("Expect sender to be null.", msg.getSender());
    Assert.assertNull("Expect date to be null.", msg.getDate());
  }

  @Test
  public void testEnvelopeInvalidDate()
  {
    final MboxMessage msg = new MboxMessage();
    final String sender = "someone@example.org";
    msg.setEnvelope("From " + sender + " Thu Feb 14 23:59:52 XXXX", 1);
    Assert.assertEquals("Expect sender to be identical.", sender, msg.getSender());
    Assert.assertNull("Expect date to be null.", msg.getDate());
  }

  @Test
  public void testEnvelopeMissingDate()
  {
    final MboxMessage msg = new MboxMessage();
    final String sender = "someonewithaverylengthyaddress@example.org";
    msg.setEnvelope("From " + sender, 1);
    Assert.assertNull("Expect sender to be identical.", msg.getSender());
    Assert.assertNull("Expect date to be null.", msg.getDate());
  }

  public void compareLines(List<String> lines, List<String> result)
  {
    Assert.assertNotNull("Expect list to be non-null.", result);
    Assert.assertEquals("Expect length to be equal.", lines.size(), result.size());
    Assert.assertEquals("Expect first element to be equal.", lines.get(0), result.get(0));
    Assert.assertEquals("Expect second element to be equal.", lines.get(1), result.get(1));
  }

  @Test
  public void testBodyAssignment()
  {
    final List<String> lines = Arrays.asList("a", "b");
    final MboxMessage msg = new MboxMessage();
    msg.setBodyLines(lines);
    final List<String> result = msg.getBodyLines();
    compareLines(lines, result);
  }

  @Test
  public void testHeaderAssignment()
  {
    final List<String> lines = Arrays.asList("a", "b");
    final MboxMessage msg = new MboxMessage();
    msg.setHeaderLines(lines);
    final List<String> result = msg.getHeaderLines();
    compareLines(lines, result);
  }
}
