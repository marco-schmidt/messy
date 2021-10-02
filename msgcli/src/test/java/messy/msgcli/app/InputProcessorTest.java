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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import messy.msgcli.app.AppTest.FailingInputStream;

public final class InputProcessorTest
{
  private static final String INVALID_FILE_NAME = "thisisa non-existing filename";
  private File tempMboxFile;

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
}
