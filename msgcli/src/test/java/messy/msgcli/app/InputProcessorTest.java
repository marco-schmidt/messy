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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import messy.msgcli.app.AppTest.FailingInputStream;
import messy.msgcli.app.FileFormatHelper.FileType;
import messy.msgio.formats.JsonMessageFormatter;
import messy.msgio.utils.IOUtils;
import messy.msgio.utils.StringUtils;

public final class InputProcessorTest
{
  private static final String INVALID_FILE_NAME = "thisisa non-existing filename";
  private File tempMboxFile;
  private static final String[] ANEWS =
  {
      "Amsg1", "news.misc", "foo!bar", "Sat Mar 28 17:56:20 1981", "Subject line", "First message body line."
  };

  @Before
  public void setup() throws IOException
  {
    tempMboxFile = File.createTempFile("messy", ".mbox");
  }

  @Test
  public void testProcessList()
  {
    final InputProcessor ip = new InputProcessor();
    ip.process(new ArrayList<>());
    ip.process(Arrays.asList(new String[]
    {
        INVALID_FILE_NAME
    }));
    ip.process(Arrays.asList(new String[]
    {
        tempMboxFile.getAbsolutePath()
    }));
  }

  @Test
  public void testProcessFile()
  {
    final InputProcessor ip = new InputProcessor();
    ip.process(INVALID_FILE_NAME);
  }

  @Test
  public void testProcessFailedInputStream()
  {
    // note: buffer length must as large as or larger than FileFormatHelper.bytesToLoad
    final byte[] buffer = new byte[512];
    Arrays.fill(buffer, (byte) 32);
    buffer[0] = (byte) 'F';
    buffer[1] = (byte) 'r';
    buffer[2] = (byte) 'o';
    buffer[3] = (byte) 'm';
    buffer[4] = (byte) ' ';
    FailingInputStream in = new AppTest.FailingInputStream(buffer);
    InputProcessor ip = new InputProcessor();
    ip.process(in, "-");

    Arrays.fill(buffer, (byte) 32);
    buffer[0] = (byte) '{';
    in = new AppTest.FailingInputStream(buffer);
    ip = new InputProcessor();
    ip.process(in, "-");
  }

  @Test
  public void testProcessFailedArchiveInputStream()
  {
    final FailingInputStream in = new AppTest.FailingInputStream();
    final InputProcessor ip = new InputProcessor();
    ip.processArchiveInput(in, "-", FileType.TAR);
  }

  public InputStream open(String name)
  {
    return getClass().getResourceAsStream(name);
  }

  private InputProcessor createInputProcessor()
  {
    final InputProcessor ip = new InputProcessor();
    ip.setMessageFormatter(new JsonMessageFormatter());
    ip.getMessageFormatter().setItems(new ArrayList<>());
    return ip;
  }

  @Test
  public void testProcessInputStream() throws IOException
  {
    final InputProcessor ip = createInputProcessor();
    String name = "example.general.tar";
    ip.process(open(name), name);

    name = "example.general.tar.gz";
    ip.process(open(name), name);

    name = "example.general.tar.bz2";
    ip.process(open(name), name);

    name = "example.general.tar.Z";
    ip.process(open(name), name);

    name = "example.general.zip";
    ip.process(open(name), name);
  }

  @Test
  public void testToLines()
  {
    final List<String> lines = IOUtils.toLines(new AppTest.FailingInputStream());
    Assert.assertTrue("List is empty.", lines.isEmpty());
  }

  @Test
  public void testProcessUnidentified() throws IOException
  {
    final InputProcessor ip = new InputProcessor();
    ip.setMessageFormatter(new JsonMessageFormatter());
    ip.getMessageFormatter().setItems(new ArrayList<>());
    ip.processUnidentified(new ByteArrayInputStream(new byte[]
    {}), "1.msg");
    ip.processUnidentified(new ByteArrayInputStream(new byte[]
    {
        (byte) 'A'
    }), "1.msg");
    ip.processUnidentified(new ByteArrayInputStream(new byte[]
    {
        (byte) 'B'
    }), "1.msg");
    ip.processUnidentified(new AppTest.FailingInputStream(new byte[]
    {
        (byte) 'A'
    }), "1.msg");
    final String s = StringUtils.concatItems(Arrays.asList(ANEWS), "\n");
    ip.processUnidentified(new ByteArrayInputStream(s.getBytes(StandardCharsets.ISO_8859_1)), "1.msg");
  }

  @Test
  public void testProcessSingleMessageAnews() throws IOException
  {
    final InputProcessor ip = new InputProcessor();
    ip.setMessageFormatter(new JsonMessageFormatter());
    ip.getMessageFormatter().setItems(new ArrayList<>());
    Assert.assertFalse("Not enough data for anews message.", ip.processSingleMessageAnews(Arrays.asList(new String[]
    {
        "A"
    }), "1.msg"));

    Assert.assertTrue("Regular anews.", ip.processSingleMessageAnews(Arrays.asList(ANEWS), "1.msg"));
  }
}
