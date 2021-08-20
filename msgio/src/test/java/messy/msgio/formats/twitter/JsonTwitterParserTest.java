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
package messy.msgio.formats.twitter;

import java.math.BigInteger;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.twitter.TwitterStatus;

public class JsonTwitterParserTest
{
  @Test
  public void testEmptyIdConversion()
  {
    final TwitterStatus msg = new TwitterStatus();
    final Message message = JsonTwitterParser.toMessage(msg);
    Assert.assertNotNull("Converted message must not be null.", message);
  }

  @Test
  public void testRegularConversion()
  {
    final TwitterStatus msg = new TwitterStatus();
    msg.setId(BigInteger.ONE);
    final Message message = JsonTwitterParser.toMessage(msg);
    Assert.assertNotNull("Converted message must not be null.", message);
  }

  @Test
  public void testTimestampParser()
  {
    Assert.assertNull("Expected null result for null input.", JsonTwitterParser.parseTimestamp(null));
    Assert.assertNull("Expected null result for empty input.", JsonTwitterParser.parseTimestamp(""));
    Assert.assertNull("Expected null result for invalid input (some non-empty string).",
        JsonTwitterParser.parseTimestamp("yarjigOIJP45353"));
    Assert.assertNull("Expected null result for invalid input (wrong day of the week).",
        JsonTwitterParser.parseTimestamp("Knf Dec 22 22:34:15 +0000 2012"));
    Assert.assertNull("Expected null result for invalid input (wrong day of the week).",
        JsonTwitterParser.parseTimestamp("Sun Dec 22 22:34:15 +0000 2012"));
    Assert.assertNull("Expected null result for invalid input (wrong name of month).",
        JsonTwitterParser.parseTimestamp("Sat Dez 22 22:34:15 +0000 2012"));
    Assert.assertNull("Expected null result for invalid input (wrong day of the month).",
        JsonTwitterParser.parseTimestamp("Sat Dec 32 22:34:15 +0000 2012"));
    Assert.assertNull("Expected null result for invalid input (wrong hour of day).",
        JsonTwitterParser.parseTimestamp("Sat Dec 22 25:34:15 +0000 2012"));
    Assert.assertNull("Expected null result for invalid input (wrong minute of hour).",
        JsonTwitterParser.parseTimestamp("Sat Dec 22 22:61:15 +0000 2012"));
    Assert.assertNotNull("Expected non-null result for valid input.",
        JsonTwitterParser.parseTimestamp("Sat Dec 22 22:34:15 +0000 2012"));
    final Date d = JsonTwitterParser.parseTimestamp("Thu Jan 1 00:00:01 +0000 1970");
    Assert.assertNotNull("Expected non-null result for valid input.", d);
    if (d != null)
    {
      Assert.assertEquals("Expected equal values.", 1000L, d.getTime());
    }
  }

  @Test
  public void testParseBigInteger()
  {
    Assert.assertNull("Expected null result for null input.", JsonTwitterParser.parseBigInteger(null));
    Assert.assertNull("Expected null result for empty input.", JsonTwitterParser.parseBigInteger(""));
    Assert.assertNull("Expected null result for invalid input (some non-empty string).",
        JsonTwitterParser.parseBigInteger("yarjigOIJP45353"));
    final BigInteger bi = JsonTwitterParser.parseBigInteger("0");
    Assert.assertNotNull("Expected non-null result for valid input.", bi);
    if (bi != null)
    {
      Assert.assertEquals("Expected equal values.", BigInteger.ZERO, bi);
    }
  }
}
