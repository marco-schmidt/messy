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
package messy.msgio.formats;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TsvMessageFormatterTest
{
  @Test
  public void testAppend()
  {
    final TsvMessageFormatter mf = new TsvMessageFormatter();
    final StringBuilder sb = new StringBuilder();
    final List<String> list = new ArrayList<>();
    list.add("a");
    list.add("b");
    mf.append(sb, list);
    Assert.assertEquals("Concatenating list.", "a,b", sb.toString());
  }

  @Test
  public void testAppendList()
  {
    final TsvMessageFormatter mf = new TsvMessageFormatter();
    final StringBuilder sb = new StringBuilder();
    final List<String> list = new ArrayList<>();
    list.add("a");
    list.add("b");
    mf.appendList(sb, list);
    Assert.assertEquals("Concatenating list.", "a,b", sb.toString());
  }
}
