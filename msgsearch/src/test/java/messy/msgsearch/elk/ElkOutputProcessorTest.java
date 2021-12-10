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
package messy.msgsearch.elk;

import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import messy.msgdata.formats.Message;

public class ElkOutputProcessorTest
{
  @Test
  public void testMessageFormatter()
  {
    final ElkOutputProcessor proc = new ElkOutputProcessor();
    proc.close();
    Assert.assertNotNull("Has message formatter.", proc.getMessageFormatter());
  }

  @Test
  public void testDetermineIndexNameProperDate()
  {
    final ElkOutputProcessor proc = new ElkOutputProcessor();
    proc.setIndexDatePattern("yyyy");
    proc.setIndexPrefix("msg-");
    final Message msg = new Message();
    msg.setSent(new Date(90000000L));
    final String indexName = proc.determineIndexName(msg);
    proc.close();
    Assert.assertEquals("Has expected index name.", "msg-1970", indexName);
  }

  @Test
  public void testDetermineIndexNameMissingDate()
  {
    final ElkOutputProcessor proc = new ElkOutputProcessor();
    proc.setIndexDatePattern("yyyy");
    proc.setIndexPrefix("msg-");
    final Message msg = new Message();
    final String indexName = proc.determineIndexName(msg);
    proc.close();
    Assert.assertEquals("Has expected index name.", "msg-misc", indexName);
  }
}
