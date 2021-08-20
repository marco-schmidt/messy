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
package messy.msgdata.formats.twitter;

import java.math.BigInteger;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

public class TwitterStatusTest
{
  @Test
  public void testTwitterStatus()
  {
    final TwitterStatus ts = new TwitterStatus();
    final Date createdAt = new Date(1000L);
    ts.setCreatedAt(createdAt);
    final BigInteger id = BigInteger.ONE;
    ts.setId(id);

    Assert.assertEquals("Expected equal values for createdAt.", createdAt, ts.getCreatedAt());
    Assert.assertEquals("Expected equal values for id.", id, ts.getId());
  }
}
