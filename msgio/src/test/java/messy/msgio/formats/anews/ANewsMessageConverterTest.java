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
package messy.msgio.formats.anews;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import messy.msgdata.formats.anews.ANewsMessage;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link ANewsMessageConverter} class.
 */
public class ANewsMessageConverterTest
{
  @Test
  public void testNullInput()
  {
    Assert.assertNull("Null input yields null output.", ANewsMessageConverter.fromLines(null));
  }

  @Test
  public void testTooSmallInput()
  {
    Assert.assertNull("Empty input yields null output.", ANewsMessageConverter.fromLines(Arrays.asList(new String[]{})));
  }

  @Test
  public void testWrongSignatureInput()
  {
    Assert.assertNull("First line does not start with 'A' leads to null output.", ANewsMessageConverter.fromLines(Arrays.asList(new String[]{"X", "", "", "", "", ""})));
  }

  @Test
  public void testMissingMessageId()
  {
    Assert.assertNull("First line contains an 'A' only, leads to null output.", ANewsMessageConverter.fromLines(Arrays.asList(new String[]{"A", "", "", "", "", ""})));
  }

  @Test
  public void testMessageId()
  {
    String id = "test.12";
    ANewsMessage msg = ANewsMessageConverter.fromLines(Arrays.asList(new String[]{"A" + id, "", "", "", "", ""}));
    Assert.assertFalse("Correct A news lines lead to non-null message object.", msg == null);
    Assert.assertEquals("Message ID properly parsed.", id, msg.getMessageId());
  }
}
