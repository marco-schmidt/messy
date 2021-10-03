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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import messy.msgcli.app.App.OutputFormat;
import messy.msgdata.formats.Message;
import messy.msgio.utils.StringUtils;

public final class AppTest
{
  private static final String REGULAR_STATUS = "{\"created_at\":\"Sun Jan 01 07:00:06 +0000 2012\",\"id\":10,\"lang\":\"en\","
      + "\"text\":\"Just a message.\",\"tags\":[\"ot\"]}";
  private static final String TWEET_CORRUPTED = "{\"crea";
  private static final String MESSAGE_ID = "<msgid-1@example.org>";
  private static final String NEWSGROUP_1 = "comp.tools";
  private static final String MAIL_ADDRESS = "name@example.org";
  private static final String AUTHOR_NAME = "Person B. Name";
  private static final String REGULAR_MBOX = "From <" + MAIL_ADDRESS + "> Sun Oct 17 12:03:20 2004\n" + "Message-ID: "
      + MESSAGE_ID + "\n" + "From: \"" + AUTHOR_NAME + "\" <" + MAIL_ADDRESS + ">\n" + "Newsgroups: " + NEWSGROUP_1
      + "\n" + "\n" + "Body.";
  private static final String MBOX_MISSING = "From <guestœexample.org> Sun Oct 17 12:03:20 2004\n" + "Message-ID: "
      + MESSAGE_ID + "\n" + "\n" + "Body.";
  private File tempMboxFile;

  /**
   * InputStream that allows reading bytes provided in constructor, after that throws IOException.
   */
  public static class FailingInputStream extends InputStream
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

  @Before
  public void setup() throws IOException
  {
    tempMboxFile = File.createTempFile("messy", ".mbox");
  }

  @Test
  public void testEscape()
  {
    Assert.assertNull("Null input leads to null output.", StringUtils.escape(null));
    Assert.assertEquals("Empty input leads to empty output.", "", StringUtils.escape(""));
    Assert.assertEquals("LF input leads to space output.", " ", StringUtils.escape("\n"));
    Assert.assertEquals("CR input leads to space output.", " ", StringUtils.escape("\r"));
    Assert.assertEquals("TAB input leads to space output.", " ", StringUtils.escape("\t"));
    Assert.assertEquals("ASCII input leads to identical output.", "ABC", StringUtils.escape("ABC"));
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
    final Map<String, String> env = new HashMap<>();
    env.put(App.MESSY_OUTPUT_FORMAT, OutputFormat.TSV.name());
    App.setEnvironment(env);
    App.main(new String[]
    {});
    System.setIn(tmpIn);
    System.setOut(tmpOut);
    final String result = out.toString(StandardCharsets.UTF_8.name());
    Assert.assertEquals("Application output identical to expected output.",
        "\t\t\t\tjsontweet\t\ten\ttwitter\t10\t\t\t\t\t2012-01-01T07:00:06+0000\t\t\tJust a message."
            + System.lineSeparator(),
        result);
  }

  @Test
  public void testMainJsonToTsvWorking() throws UnsupportedEncodingException
  {
    final InputStream tmpIn = System.in;
    final PrintStream tmpOut = System.out;
    final ByteArrayInputStream in = new ByteArrayInputStream(REGULAR_STATUS.getBytes(StandardCharsets.US_ASCII));
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setIn(in);
    System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8.name()));
    final Map<String, String> env = new HashMap<>();
    env.put(App.MESSY_OUTPUT_FORMAT, OutputFormat.TSV.name());
    env.put(App.MESSY_OUTPUT_ITEMS, Message.Item.LANG_CODE.name());
    App.setEnvironment(env);
    App.main(new String[]
    {});
    System.setIn(tmpIn);
    System.setOut(tmpOut);
    final String result = out.toString(StandardCharsets.UTF_8.name());
    Assert.assertEquals("Application output identical to expected output.", "en" + System.lineSeparator(), result);
  }

  @Test
  public void testMainJsonInputJsonOutputWorking() throws UnsupportedEncodingException
  {
    final InputStream tmpIn = System.in;
    final PrintStream tmpOut = System.out;
    final ByteArrayInputStream in = new ByteArrayInputStream(REGULAR_STATUS.getBytes(StandardCharsets.US_ASCII));
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setIn(in);
    System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8.name()));
    final App.OutputFormat saveOutputFormat = App.getOutputFormat();
    final Map<String, String> env = new HashMap<>();
    env.put(App.MESSY_OUTPUT_FORMAT, OutputFormat.JSON.name());
    App.setEnvironment(env);
    App.main(new String[]
    {});
    System.setIn(tmpIn);
    System.setOut(tmpOut);
    App.setOutputFormat(saveOutputFormat);
    final String result = out.toString(StandardCharsets.UTF_8.name());
    Assert.assertTrue("Application output stars with '{'.", result.startsWith("{"));
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
        "Could not identify '-' to be in a supported format." + System.lineSeparator(), result);
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

  @Test
  public void testMainTempMbox()
  {
    final String[] args = new String[]
    {
        tempMboxFile.getAbsolutePath()
    };
    App.main(args);
  }
}
