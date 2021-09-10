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
package messy.msgio.utils;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest
{
  @Test
  public void testSplitAndCleanNull()
  {
    final List<String> result = StringUtils.splitAndNormalize(null, ",");
    Assert.assertNotNull("Null input leads to non-null output.", result);
    Assert.assertTrue("Result is empty.", result.isEmpty());
  }

  @Test
  public void testSplitAndCleanEmpty()
  {
    final List<String> result = StringUtils.splitAndNormalize("a,, ,", ",");
    Assert.assertNotNull("Non-null input leads to null output.", result);
    Assert.assertEquals("Result has one element.", 1, result.size());
    Assert.assertEquals("Result element is 'a'.", "a", result.get(0));
  }
}
