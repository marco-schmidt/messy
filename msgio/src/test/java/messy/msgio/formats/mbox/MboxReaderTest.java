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
package messy.msgio.formats.mbox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;
import messy.msgdata.formats.mbox.MboxMessage;

public final class MboxReaderTest
{
  /**
   * Load resource data from argument file to byte array.
   *
   * @param name
   *          file name
   * @return byte array with data
   * @throws IOException
   *           on read errors
   */
  public byte[] readData(final String name) throws IOException
  {
    InputStream in = null;
    try
    {
      in = getClass().getResourceAsStream(name);
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final byte[] buffer = new byte[1024];
      int numRead;
      while (true)
      {
        numRead = in.read(buffer);
        if (numRead >= 0)
        {
          baos.write(buffer, 0, numRead);
        }
        else
        {
          break;
        }
      }
      return baos.toByteArray();
    }
    catch (final IOException ioe)
    {
      return null;
    }
    finally
    {
      if (in != null)
      {
        in.close();
      }
    }
  }

  public Reader open(final String name) throws IOException
  {
    final byte[] data = readData(name);
    return new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.ISO_8859_1);
  }

  private MboxMessage read(String name) throws IOException
  {
    final Reader in = open(name);
    final MboxReader reader = new MboxReader(in);
    final MboxMessage msg = reader.next();
    final int number = reader.getLineNumber();
    Assert.assertTrue("Read at least one line.", number > 0);
    reader.close();
    reader.close();
    return msg;
  }

  @Test
  public void testEnvelope() throws IOException
  {
    final Reader in = open("main.mbox");
    final MboxReader reader = new MboxReader(in);
    final boolean envelopeLine = reader.isEnvelopeLine("");
    final MboxMessage msg = reader.next();
    reader.close();
    Assert.assertFalse("Empty line is no envelope line.", envelopeLine);
    Assert.assertNotNull("Message read.", msg);
  }

  @Test
  public void testEnvelopeNoHighLevelUnquoting() throws IOException
  {
    final Reader in = open("main.mbox");
    final MboxReader reader = new MboxReader(in, false);
    final MboxMessage msg = reader.next();
    reader.close();
    Assert.assertNotNull("Message read.", msg);
  }

  @Test
  public void testRegular() throws IOException
  {
    final MboxMessage msg = read("main.mbox");
    Assert.assertNotNull("Message read.", msg);
  }

  @Test
  public void testSecondEnvelope() throws IOException
  {
    final Reader in = open("secondenvelope.mbox");
    final MboxReader reader = new MboxReader(in);
    MboxMessage msg = reader.next();
    Assert.assertNotNull("First message read.", msg);
    msg = reader.next();
    Assert.assertNotNull("Second message read.", msg);
    reader.close();
  }

  @Test
  public void testSecondEnvelopeTruncated() throws IOException
  {
    final Reader in = open("secondenvelopetruncated.mbox");
    final MboxReader reader = new MboxReader(in);
    MboxMessage msg = reader.next();
    Assert.assertNotNull("First message read.", msg);
    msg = reader.next();
    Assert.assertNotNull("Second message read.", msg);
    reader.close();
  }

  @Test
  public void testNoEnvelope() throws IOException
  {
    final MboxMessage msg = read("noenvelope.mbox");
    Assert.assertNull("No message because of missing envelope line.", msg);
  }

  @Test
  public void test1969() throws IOException
  {
    final MboxMessage msg = read("1969.mbox");
    Assert.assertNull("Envelope line with year 1969 leads to null message.", msg);
  }

  @Test
  public void test10000() throws IOException
  {
    final MboxMessage msg = read("10000.mbox");
    Assert.assertNull("Envelope line with year 10000 leads to null message.", msg);
  }

  @Test
  public void testInvalidYear() throws IOException
  {
    final MboxMessage msg = read("invalidyear.mbox");
    Assert.assertNull("Envelope line with year XYZ leads to null message.", msg);
  }

  @Test
  public void testIdentifyEnvelope() throws IOException
  {
    Assert.assertNull("Null input.", MboxReader.identifyEnvelope(null));
    Assert.assertNull("Does not start with 'From '.", MboxReader.identifyEnvelope("ab"));
    Assert.assertEquals("Large integer", MboxReader.MboxType.LARGE_INTEGER, MboxReader.identifyEnvelope("From 1345"));
    Assert.assertEquals("Funbackup", MboxReader.MboxType.FUNBACKUP_TRAILER,
        MboxReader.identifyEnvelope("From nobody art.2 FUNBACKUP"));
    Assert.assertEquals("Large integer", MboxReader.MboxType.REGULAR,
        MboxReader.identifyEnvelope("From alice@example.org Mon Jun 18 11:13:50 2001"));
  }

  @Test
  public void testIdentifyEnvelopeLargeInteger() throws IOException
  {
    Assert.assertFalse("Missing 'From '.", MboxReader.identifyEnvelopeLargeInteger(null));
    Assert.assertFalse("Missing 'From '.", MboxReader.identifyEnvelopeLargeInteger(""));
    Assert.assertFalse("Not a number.", MboxReader.identifyEnvelopeLargeInteger("From localpart@example.org"));
    Assert.assertFalse("Number with letter in it.", MboxReader.identifyEnvelopeLargeInteger("From 5546490a254454335"));
    Assert.assertFalse("Number with space in it.", MboxReader.identifyEnvelopeLargeInteger("From 5546490 254454335"));
    Assert.assertTrue("Positive number.", MboxReader.identifyEnvelopeLargeInteger("From 2345546490254454335"));
    Assert.assertTrue("Negative number.", MboxReader.identifyEnvelopeLargeInteger("From -2345546490254454335"));
  }

  @Test
  public void testIsEnvelopeLine() throws IOException
  {
    MboxReader reader = new MboxReader(new StringReader(""));
    Assert.assertTrue("Identify funbackup", reader.isEnvelopeLine("From nobody art.2 FUNBACKUP"));
    reader.close();

    reader = new MboxReader(new StringReader(""));
    Assert.assertTrue("Identify large integer.", reader.isEnvelopeLine("From 2345546490254454335"));
    reader.close();
  }
}
