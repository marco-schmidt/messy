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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import messy.msgcli.app.AppTest.FailingInputStream;
import messy.msgcli.app.FileFormatHelper.FileType;
import messy.msgio.formats.JsonMessageFormatter;
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
  public void testIsFileNameInteger()
  {
    Assert.assertFalse("Null input leads to false.", InputProcessor.isFileNameInteger(null));
    Assert.assertFalse("Empty input leads to false.", InputProcessor.isFileNameInteger(""));
    Assert.assertFalse("Letter input leads to false.", InputProcessor.isFileNameInteger("file.txt"));
    Assert.assertFalse("Letter input leads to false.", InputProcessor.isFileNameInteger("dir/file.txt"));
    Assert.assertFalse("Mixed input leads to false.", InputProcessor.isFileNameInteger("dir/file123"));
    Assert.assertTrue("Mixed input leads to false.", InputProcessor.isFileNameInteger("0012345"));
    Assert.assertTrue("Mixed input leads to false.", InputProcessor.isFileNameInteger("dir/0012345"));
  }

  @Test
  public void testIsLikelySingleMessageFile()
  {
    Assert.assertFalse("Null input leads to false.", InputProcessor.isLikelySingleMessageFile(null));
    Assert.assertFalse("Empty input leads to false.", InputProcessor.isLikelySingleMessageFile(""));
    Assert.assertFalse("Letter input leads to false.", InputProcessor.isLikelySingleMessageFile("file.txt"));
    Assert.assertTrue("Trailing digit leads to true.", InputProcessor.isLikelySingleMessageFile("dir/ab.100"));
    Assert.assertTrue("Msg leads to true.", InputProcessor.isLikelySingleMessageFile("dir/a.msg"));
    Assert.assertTrue("Eml leads to true.", InputProcessor.isLikelySingleMessageFile("dir/a.eml"));
    Assert.assertFalse("Txt  leads to false.", InputProcessor.isLikelySingleMessageFile("dir/x.txt"));
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

  @Test
  public void testProcessInputStream() throws IOException
  {
    InputProcessor ip = new InputProcessor();
    String name = "example.general.tar";
    ip.process(open(name), name);

    ip = new InputProcessor();
    name = "example.general.tar.gz";
    ip.process(open(name), name);

    ip = new InputProcessor();
    name = "example.general.tar.bz2";
    ip.process(open(name), name);

    ip = new InputProcessor();
    name = "example.general.tar.Z";
    ip.process(open(name), name);

    ip = new InputProcessor();
    name = "example.general.zip";
    ip.process(open(name), name);
  }

  @Test
  public void testProcessUnidentified() throws IOException
  {
    final InputProcessor ip = new InputProcessor();
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
  }

  @Test
  public void testProcessSingleMessageAnews() throws IOException
  {
    final InputProcessor ip = new InputProcessor();
    ip.setMessageFormatter(new JsonMessageFormatter());
    ip.getMessageFormatter().setItems(new ArrayList<>());
    Assert.assertFalse("Not enough data for anews message.",
        ip.processSingleMessageAnews(new ByteArrayInputStream(new byte[]
        {
            (byte) 'A'
        }), "1.msg"));

    Assert.assertFalse("Failed I/O.", ip.processSingleMessageAnews(new AppTest.FailingInputStream(new byte[]
    {
        (byte) 'A'
    }), "1.msg"));
    final String s = StringUtils.concatItems(Arrays.asList(ANEWS), "\n");
    final ByteArrayInputStream bin = new ByteArrayInputStream(s.getBytes(StandardCharsets.ISO_8859_1));
    Assert.assertTrue("Regular anews.", ip.processSingleMessageAnews(bin, "1.msg"));
  }
}
