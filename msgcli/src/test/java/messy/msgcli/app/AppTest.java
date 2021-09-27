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
import messy.msgcli.app.App.OutputFormat;
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
    Assert.assertNull("Null input leads to null output.", StringUtils.escape(null));
    Assert.assertEquals("Empty input leads to empty output.", "", StringUtils.escape(""));
    Assert.assertEquals("LF input leads to space output.", " ", StringUtils.escape("\n"));
    Assert.assertEquals("CR input leads to space output.", " ", StringUtils.escape("\r"));
    Assert.assertEquals("TAB input leads to space output.", " ", StringUtils.escape("\t"));
    Assert.assertEquals("ASCII input leads to identical output.", "ABC", StringUtils.escape("ABC"));
  }

  // @Test
  // public void testFormat()
  // {
  // Assert.assertEquals("Null date leads to empty result.", "", App.format(App.createDateFormatter(), null));
  // }

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
        "\t\t\t\tjsontweet\t\ten\ttwitter\t10\t\t\t\t\t2012-01-01T07:00:06+0000\t\t\tJust a message."
            + System.lineSeparator(),
        result);
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
    App.setOutputFormat(OutputFormat.JSON);
    App.main(new String[]
    {});
    System.setIn(tmpIn);
    System.setOut(tmpOut);
    App.setOutputFormat(saveOutputFormat);
    final String result = out.toString(StandardCharsets.UTF_8.name());
    Assert.assertTrue("Application output stars with.", result.startsWith("{"));
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

  // @Test
  // public void testFormatStringList()
  // {
  // final List<String> input = new ArrayList<>();
  // final String value = "comp.os";
  // input.add(value);
  // final Object output = App.format(input);
  // Assert.assertEquals("Formatted value as expected.", "[\"" + value + "\"]", output.toString());
  // }

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

  // @Test
  // public void testFormatJson() throws UnsupportedEncodingException
  // {
  // final Message msg = new Message();
  // msg.setArchive(Boolean.FALSE);
  // msg.setPostingHost("example.org");
  // msg.setPostingIpAddress("117.0.0.3");
  // msg.setCountryCode("uk");
  // msg.setPostingIpv4Address(Long.valueOf(117 << 24L | 3));
  // final List<String> tags = new ArrayList<>();
  // tags.add("tag");
  // msg.setTags(tags);
  // String json = App.formatJson(msg, null);
  // Assert.assertTrue("Result contains archive false.", json.contains("\"archive\":false"));
  // Assert.assertTrue("Result contains posting host.", json.contains("\"host\":\"example.org\""));
  // Assert.assertTrue("Result contains ip address.", json.contains("\"ip_addr\":\"117.0.0.3\""));
  // Assert.assertTrue("Result contains country code.", json.contains("\"country_code\":\"uk\""));
  // Assert.assertTrue("Result contains tags.", json.contains("\"tags\":[\"tag\""));
  // tags.clear();
  // msg.setTags(tags);
  // final OutputFormat outputFormat = App.getOutputFormat();
  // App.setOutputFormat(OutputFormat.JSON);
  // json = App.formatJson(msg, null);
  // App.setOutputFormat(outputFormat);
  // Assert.assertFalse("Result contains tags.", json.contains("\"tags\""));
  // }
  //
}
