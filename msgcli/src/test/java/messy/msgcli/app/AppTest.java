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
package messy.msgcli.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;

public final class AppTest
{
  private static final String REGULAR_STATUS = "{\"created_at\":\"Sun Jan 01 07:00:06 +0000 2012\",\"id\":10,\"lang\":\"en\",\"text\":\"Just a message.\"}";
  private static final String TWEET_CORRUPTED = "{\"crea";
  private static final String MESSAGE_ID = "<msgid-1@example.org>";
  private static final String NEWAGROUP_1 = "comp.tools";
  private static final String REGULAR_MBOX = "From <guestœexample.org> Sun Oct 17 12:03:20 2004\n" + "Message-ID: "
      + MESSAGE_ID + "\n" + "Newsgroups: " + NEWAGROUP_1 + "\n" + "\n" + "Body.";
  private static final String MBOX_MISSING = "From <guestœexample.org> Sun Oct 17 12:03:20 2004\n" + "Message-ID: "
      + MESSAGE_ID + "\n" + "\n" + "Body.";

  /**
   * InputStream that allows reading bytes provided in constructor, after that throws IOException.
   */
  static class FailingInputStream extends InputStream
  {
    private int index;
    private final byte[] data;

    FailingInputStream()
    {
      this(new byte[]
      {});
    }

    FailingInputStream(byte[] data)
    {
      this.data = data;
    }

    @Override
    public int read() throws IOException
    {
      if (index < data.length)
      {
        return data[index++] & 0xff;
      }
      throw new IOException("Failing on purpose.");
    }
  }

  @Test
  public void testEscape()
  {
    Assert.assertNull("Null input leads to null output.", App.escape(null));
    Assert.assertEquals("Empty input leads to empty output.", "", App.escape(""));
    Assert.assertEquals("LF input leads to space output.", " ", App.escape("\n"));
    Assert.assertEquals("CR input leads to space output.", " ", App.escape("\r"));
    Assert.assertEquals("TAB input leads to space output.", " ", App.escape("\t"));
    Assert.assertEquals("ASCII input leads to identical output.", "ABC", App.escape("ABC"));
  }

  @Test
  public void testFormat()
  {
    Assert.assertEquals("Null date leads to empty result.", "", App.format(App.createFormatter(), null));
  }

  @Test
  public void testMainReadFailure()
  {
    final InputStream tmp = System.in;
    final InputStream in = new FailingInputStream();
    System.setIn(in);
    App.main(new String[]
    {});
    System.setIn(tmp);
    Assert.assertEquals("System input now back to original value.", tmp, System.in);
  }

  @Test
  public void testMainJsonWorking() throws UnsupportedEncodingException
  {
    final InputStream tmpIn = System.in;
    final PrintStream tmpOut = System.out;
    final ByteArrayInputStream in = new ByteArrayInputStream(REGULAR_STATUS.getBytes(StandardCharsets.US_ASCII));
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setIn(in);
    System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8.name()));
    App.main(new String[]
    {});
    System.setIn(tmpIn);
    System.setOut(tmpOut);
    final String result = out.toString(StandardCharsets.UTF_8.name());
    Assert.assertEquals("Application output identical to expected output.",
        "2012-01-01T07:00:06+0000\ten\t\t10\t\t\tJust a message." + System.lineSeparator(), result);
  }

  @Test
  public void testMainJsonTruncated() throws UnsupportedEncodingException
  {
    final InputStream tmpIn = System.in;
    final PrintStream tmpOut = System.out;
    final FailingInputStream in = new FailingInputStream("{".getBytes(StandardCharsets.US_ASCII));
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setIn(in);
    System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8.name()));
    App.main(new String[]
    {});
    System.setIn(tmpIn);
    System.setOut(tmpOut);
  }

  @Test
  public void testMainJsonCorrupted() throws UnsupportedEncodingException
  {
    final InputStream tmpIn = System.in;
    final PrintStream tmpErr = System.err;
    final ByteArrayInputStream in = new ByteArrayInputStream(TWEET_CORRUPTED.getBytes(StandardCharsets.US_ASCII));
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setIn(in);
    System.setErr(new PrintStream(out, true, StandardCharsets.UTF_8.name()));
    App.main(new String[]
    {});
    System.setIn(tmpIn);
    System.setErr(tmpErr);
    final String result = out.toString(StandardCharsets.UTF_8.name());
    Assert.assertTrue("Expected failed decoding.", result.startsWith("Could not decode JSON tweet:'"));
  }

  @Test
  public void testUnknownInput() throws UnsupportedEncodingException
  {
    final InputStream tmpIn = System.in;
    final PrintStream tmpErr = System.err;
    final ByteArrayInputStream in = new ByteArrayInputStream(new byte[]
    {
        0
    });
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setIn(in);
    System.setErr(new PrintStream(out, true, StandardCharsets.UTF_8.name()));
    App.main(new String[]
    {});
    System.setIn(tmpIn);
    System.setErr(tmpErr);
    final String result = out.toString(StandardCharsets.UTF_8.name());
    Assert.assertEquals("Application output identical to expected output.",
        "Could not identify input to be in a supported format." + System.lineSeparator(), result);
  }

  @Test
  public void testFailingInput() throws UnsupportedEncodingException
  {
    final InputStream tmpIn = System.in;
    final PrintStream tmpErr = System.err;
    final InputStream in = new FailingInputStream();
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setIn(in);
    System.setErr(new PrintStream(out, true, StandardCharsets.UTF_8.name()));
    App.main(new String[]
    {});
    System.setIn(tmpIn);
    System.setErr(tmpErr);
    final String result = out.toString(StandardCharsets.UTF_8.name());
    Assert.assertEquals("Application output identical to expected output.",
        "Could not identify input to be in a supported format." + System.lineSeparator(), result);
  }

  @Test
  public void testMainMboxWorking() throws UnsupportedEncodingException
  {
    final InputStream tmpIn = System.in;
    final PrintStream tmpOut = System.out;
    final ByteArrayInputStream in = new ByteArrayInputStream(REGULAR_MBOX.getBytes(StandardCharsets.US_ASCII));
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setIn(in);
    System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8.name()));
    App.main(new String[]
    {});
    System.setIn(tmpIn);
    System.setOut(tmpOut);
  }

  @Test
  public void testMainMboNoNewsgroups() throws UnsupportedEncodingException
  {
    final InputStream tmpIn = System.in;
    final PrintStream tmpOut = System.out;
    final ByteArrayInputStream in = new ByteArrayInputStream(MBOX_MISSING.getBytes(StandardCharsets.US_ASCII));
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setIn(in);
    System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8.name()));
    App.main(new String[]
    {});
    System.setIn(tmpIn);
    System.setOut(tmpOut);
  }
}
