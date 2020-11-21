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
package messy.msgdata.formats;

import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link Message}.
 *
 * @author Marco Schmidt
 */
public class MessageTest
{
  @Test
  public void testNullValues()
  {
    final Message msg = new Message();
    msg.setAuthorName(null);
    msg.setFormat(null);
    msg.setMedium(null);
    msg.setSent(null);
    msg.setSubject(null);
    Assert.assertNull("Expect author name to remain null.", msg.getAuthorName());
    Assert.assertNull("Expect format name to remain null.", msg.getFormat());
    Assert.assertNull("Expect medium name to remain null.", msg.getMedium());
    Assert.assertNull("Expect sent timestamp to remain null.", msg.getSent());
    Assert.assertNull("Expect subject to remain null.", msg.getSubject());
  }

  @Test
  public void testRegularValues()
  {
    final String authorName = "Jane Doe";
    final String format = "imf";
    final String medium = "mail";
    final String subject = "Just a message";
    final long millis = 123456789L;
    final Date sent = new Date(millis);
    final Message msg = new Message();
    msg.setAuthorName(authorName);
    msg.setFormat(format);
    msg.setMedium(medium);
    msg.setSent(sent);
    msg.setSubject(subject);
    Assert.assertEquals("Expect author name to remain identical.", authorName, msg.getAuthorName());
    Assert.assertEquals("Expect format name to remain identical.", format, msg.getFormat());
    Assert.assertEquals("Expect medium name to remain identical.", medium, msg.getMedium());
    Assert.assertEquals("Expect sent timestamp to remain identical.", sent, msg.getSent());
    Assert.assertEquals("Expect subject to remain identical.", subject, msg.getSubject());
  }

}
