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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public final class AppTest
{
  private static final String REGULAR_STATUS = "{\"created_at\":\"Sun Jan 01 07:00:06 +0000 2012\",\"id\":10,\"lang\":\"en\",\"text\":\"Just a message.\"}";

  static class FailingInputStream extends InputStream
  {
    @Override
    public int read() throws IOException
    {
      throw new IOException("Failing on purpose.");
    }
  }

  @Test
  public void testMainReadFailure()
  {
    InputStream tmp = System.in;
    InputStream in = new FailingInputStream();
    System.setIn(in);
    messy.msgcli.app.App.main(new String[]
    {});
    System.setIn(tmp);
    Assert.assertEquals("System input now back to original value.", tmp, System.in);
  }

  @Test
  public void testMainWorking()
  {
    InputStream tmp = System.in;
    ByteArrayInputStream in = new ByteArrayInputStream(REGULAR_STATUS.getBytes(StandardCharsets.US_ASCII));
    System.setIn(in);
    messy.msgcli.app.App.main(new String[]
    {});
    System.setIn(tmp);
    Assert.assertEquals("System input now back to original value.", tmp, System.in);
  }
}
