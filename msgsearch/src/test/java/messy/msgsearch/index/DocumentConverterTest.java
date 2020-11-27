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
package messy.msgsearch.index;

import java.util.Date;
import org.apache.lucene.document.Document;
import org.junit.Assert;
import org.junit.Test;
import messy.msgdata.formats.Message;

/**
 * Test {@link DocumentConverter}.
 *
 * @author Marco Schmidt
 */
public class DocumentConverterTest
{
  public static final long SENT = 1000000000L;
  public static final String SUBJECT_LAST_WORD = "line";
  public static final String SUBJECT = "A subject " + SUBJECT_LAST_WORD;

  public static Message createSample()
  {
    final Message msg = new Message();
    msg.setSent(new Date(SENT));
    msg.setSubject(SUBJECT);
    return msg;
  }

  @Test
  public void testRegular()
  {
    final Document doc = new DocumentConverter().from(createSample());
    Assert.assertEquals("Expect identical subject.", SUBJECT, doc.get(DocumentConverter.FIELD_SUBJECT));
  }

  @Test
  public void testNull()
  {
    final Message message = createSample();
    message.setSent(null);
    final Document doc = new DocumentConverter().from(message);
    Assert.assertNull("Sent long point will return null.", doc.get(DocumentConverter.FIELD_SENT));
  }
}
