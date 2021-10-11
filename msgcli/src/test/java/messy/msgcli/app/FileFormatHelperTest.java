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

import org.junit.Assert;
import org.junit.Test;
import messy.msgcli.app.AppTest.FailingInputStream;
import messy.msgcli.app.FileFormatHelper.FileType;

public final class FileFormatHelperTest
{

  @Test
  public void testIsLikelySingleMessageFile()
  {
    Assert.assertFalse("Null input leads to false.", FileFormatHelper.isLikelySingleMessageFile(null));
    Assert.assertFalse("Empty input leads to false.", FileFormatHelper.isLikelySingleMessageFile(""));
    Assert.assertFalse("Letter input leads to false.", FileFormatHelper.isLikelySingleMessageFile("file.txt"));
    Assert.assertTrue("Trailing digit leads to true.", FileFormatHelper.isLikelySingleMessageFile("dir/ab.100"));
    Assert.assertTrue("Msg leads to true.", FileFormatHelper.isLikelySingleMessageFile("dir/a.msg"));
    Assert.assertTrue("Eml leads to true.", FileFormatHelper.isLikelySingleMessageFile("dir/a.eml"));
    Assert.assertFalse("Txt  leads to false.", FileFormatHelper.isLikelySingleMessageFile("dir/x.txt"));
  }

  @Test
  public void testWrapDecompressor()
  {
    final AppTest.FailingInputStream in = new FailingInputStream();
    FileFormatHelper.wrapDecompressor(in, FileType.GZIP);
  }
}
