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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link RawMessage}.
 *
 * @author Marco Schmidt
 */

public class RawMessageTest
{
  @Test
  public void testHeaderBodyAssignment()
  {
    final RawMessage msg = new RawMessage(new ArrayList<>(Arrays.asList(new String[]
    {
        "foo"
    })), new ArrayList<>());
    final List<String> headerLines = msg.getHeaderLines();
    Assert.assertNotNull("Expect header lines to not be null.", headerLines);
    Assert.assertEquals("Expect header lines to have 1 lines.", 1, headerLines.size());
    final List<String> bodyLines = msg.getBodyLines();
    Assert.assertNotNull("Expect body lines to not be null.", bodyLines);
    Assert.assertEquals("Expect body lines to have 0 lines.", 0, bodyLines.size());
  }

  @Test
  public void testLinesToByteArrayConversion()
  {
    final RawMessage msg = new RawMessage(new ArrayList<>(Arrays.asList(new String[]
    {
        "foo"
    })), new ArrayList<>());
    final byte[] header = msg.getHeaderLinesAsBytes();
    Assert.assertNotNull("Expect header bytes to not be null.", header);
    Assert.assertEquals("Expect header bytes array to have length 5.", 5, header.length);
    final byte[] body = msg.getBodyLinesAsBytes();
    Assert.assertNotNull("Expect body bytes to not be null.", body);
    Assert.assertEquals("Expect body bytes array to have length 0.", 0, body.length);
  }

  @Test
  public void testLinesToByteArrayConversionException()
  {
    final RawMessage msg = new RawMessage(new ArrayList<>(Arrays.asList(new String[]
    {
        "\uD83D"
    })), new ArrayList<>());
    final byte[] header = msg.getHeaderLinesAsBytes();
    Assert.assertNotNull("Expect header bytes to not be null.", header);
    Assert.assertEquals("Expect header bytes array to have length 3 (one character plus lf cr).", 3, header.length);
    Assert.assertEquals("Expect first byte to be the lower 8 bits of db3d.", 0x3d, header[0] & 0xff);
  }
}
