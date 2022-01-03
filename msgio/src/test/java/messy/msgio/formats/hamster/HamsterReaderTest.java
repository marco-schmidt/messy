/*
 * Copyright 2020, 2021, 2022 the original author or authors.
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
package messy.msgio.formats.hamster;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class HamsterReaderTest
{
  private static final byte[] MESSAGE_1 = new byte[]
  {
      (byte) 'm', (byte) 'e', (byte) 's', (byte) 's', (byte) 'a', (byte) 'g', (byte) 'e',
  };

  public static HamsterReader from(byte[] data)
  {
    return new HamsterReader(new ByteArrayInputStream(data));
  }

  @Test
  public void testReadMessageLength() throws IOException
  {
    HamsterReader in = from(new byte[]
    {});
    long length = in.readMessageLength();
    Assert.assertEquals("Empty input leads to -1.", -1, length);

    in = from(new byte[]
    {
        1
    });
    length = in.readMessageLength();
    Assert.assertEquals("One byte input leads to -1.", -1, length);

    in = from(new byte[]
    {
        1, 2
    });
    length = in.readMessageLength();
    Assert.assertEquals("Two byte input leads to -1.", -1, length);

    in = from(new byte[]
    {
        1, 2, 3
    });
    length = in.readMessageLength();
    Assert.assertEquals("Three byte input leads to -1.", -1, length);

    in = from(new byte[]
    {
        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
    });
    length = in.readMessageLength();
    Assert.assertEquals("All 0xff input leads to 2^32-1.", (1L << 32L) - 1, length);
  }

  @Test
  public void testIsDataFileName()
  {
    Assert.assertFalse("Null input means no data.dat.", HamsterReader.isDataFileName(null));
    Assert.assertTrue("Exact match means true.", HamsterReader.isDataFileName("data.dat"));
    Assert.assertTrue("Exact match means true.", HamsterReader.isDataFileName("DATA.DAT"));
    Assert.assertTrue("Exact match with / means true.", HamsterReader.isDataFileName("/DATA.DAT"));
    Assert.assertTrue("Exact match with \\ means true.", HamsterReader.isDataFileName("\\DATA.DAT"));
    Assert.assertFalse("Suffix match without slash means false.", HamsterReader.isDataFileName("ADATA.DAT"));
    Assert.assertFalse("Partial name means false.", HamsterReader.isDataFileName("ATA.DAT"));
  }

  @Test
  public void testReadNext() throws IOException
  {
    // read regular single-message data.dat
    final byte[] a = new byte[MESSAGE_1.length + 4];
    a[0] = (byte) (MESSAGE_1.length & 0xff);
    System.arraycopy(MESSAGE_1, 0, a, 4, MESSAGE_1.length);
    HamsterReader in = from(a);
    byte[] msg = in.readNext();
    Assert.assertNotNull("Non-null result.", msg);
    Assert.assertEquals("Expected length.", MESSAGE_1.length, msg.length);

    msg = in.readNext();
    Assert.assertNull("Null result because no more data.", msg);

    // make max message size small enough so that first and only message is skipped
    in = new HamsterReader(new ByteArrayInputStream(a), MESSAGE_1.length - 1);
    msg = in.readNext();
    Assert.assertNull("Null result because max length is too small.", msg);

    // mark message deleted and init reader so that deleted messages are skipped
    a[3] = -128;
    in = new HamsterReader(new ByteArrayInputStream(a), 1000, true);
    msg = in.readNext();
    Assert.assertNull("Null result because deleted message and skipDeleted=true.", msg);

    // mark message deleted and init reader so that deleted messages are skipped
    in = from(a);
    msg = in.readNext();
    Assert.assertNotNull("Non-null result because deleted message and skipDeleted=false.", msg);

    // message not deleted and init reader so that deleted messages are skipped
    a[3] = 0;
    in = new HamsterReader(new ByteArrayInputStream(a), 1000, true);
    msg = in.readNext();
    Assert.assertNotNull("Non-null result because non-deleted message and skipDeleted=true.", msg);

    // make message length larger than actual input
    a[0]++;
    in = from(a);
    msg = in.readNext();
    Assert.assertNull("Null result because message length larger than input.", msg);
  }
}
