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
package messy.msgio.formats.messy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import messy.msgdata.formats.messy.MessageData;

public class FileSystemTarMessageSinkTest
{
  private Path tempDirectory;

  @Before
  public void setup() throws IOException
  {
    final String time = Long.toString(new Date().getTime());
    tempDirectory = Files.createTempDirectory(time);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidMaximumFileSize() throws IOException
  {
    final FileSystemTarMessageSink sink = new FileSystemTarMessageSink(tempDirectory, Long.MIN_VALUE);
    sink.close();
  }

  @Test
  public void testAdd() throws IOException
  {
    final MessageData msg = new MessageData();
    msg.setContent(new byte[]
    {});
    FileSystemTarMessageSink sink = new FileSystemTarMessageSink(tempDirectory, Long.MAX_VALUE);
    Assert.assertEquals("Maximum file size identical.", Long.MAX_VALUE, sink.getMaxFileSize());
    sink.put(msg);
    msg.setTimestamp(new Date());
    sink.put(msg);
    sink.close();
    sink.close();
    sink = new FileSystemTarMessageSink(tempDirectory, Long.MAX_VALUE);
    sink.put(msg);
    sink.close();
  }

  @After
  public void tearDown() throws IOException
  {
    Files.list(tempDirectory).forEach(entry ->
    {
      try
      {
        Files.delete(entry);
      }
      catch (final IOException e)
      {
        e.printStackTrace();
      }
    });
    Files.delete(tempDirectory);
  }
}
