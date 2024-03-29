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
    ip.process("data.dat");
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
    final JsonMessageFormatter jsonMessageFormatter = new JsonMessageFormatter();
    jsonMessageFormatter.setItems(new ArrayList<>());
    ip.getOutputProcessor().setMessageFormatter(jsonMessageFormatter);
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

    // password is "secret" in case decryption is supported in the future
    name = "example.general.encrypted.zip";
    ip.process(open(name), name);

    ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]
    {
        (byte) '7', (byte) 'z', (byte) 0xbc, (byte) 0xaf, (byte) 0x27, (byte) 0x1c, 1
    });
    ip.process(inputStream, "DoesNotExist.7z");

    inputStream = new ByteArrayInputStream(new byte[]
    {
        (byte) 32, 0, 0, 0, 1
    });
    ip.process(inputStream, "data.dat");
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
    final InputProcessor ip = createInputProcessor();

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
    final byte[] anewsBytes = s.getBytes(StandardCharsets.ISO_8859_1);
    ip.processUnidentified(new ByteArrayInputStream(anewsBytes), "1.msg");

    final int anewsNumBytes = anewsBytes.length;
    final byte[] hamsterData = new byte[anewsNumBytes + 4];
    System.arraycopy(anewsBytes, 0, hamsterData, 4, anewsNumBytes);
    hamsterData[0] = (byte) (anewsNumBytes & 0xff);
    ip.processUnidentified(new ByteArrayInputStream(hamsterData), "data.dat");

    ip.processUnidentified(new AppTest.FailingInputStream(new byte[]
    {

    }), "data.dat");
  }

  @Test
  public void testProcessSingleMessageAnews() throws IOException
  {
    final InputProcessor ip = createInputProcessor();
    Assert.assertFalse("Not enough data for anews message.", ip.processSingleMessageAnews(Arrays.asList(new String[]
    {
        "A"
    }), "1.msg"));

    Assert.assertTrue("Regular anews.", ip.processSingleMessageAnews(Arrays.asList(ANEWS), "1.msg"));
  }
}
