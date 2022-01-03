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
package messy.msgio.utils;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class NetUtilsTest
{
  @Test
  public void testValidLabel()
  {
    Assert.assertFalse("Null is invalid.", NetUtils.isValidLabel(null));
    Assert.assertFalse("Empty is invalid.", NetUtils.isValidLabel(""));
    Assert.assertFalse("Too long is invalid.",
        NetUtils.isValidLabel("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcd"));
    Assert.assertFalse("Invalid character is invalid.", NetUtils.isValidLabel("!"));
    Assert.assertFalse("Hyphen at beginning is invalid.", NetUtils.isValidLabel("-ab"));
    Assert.assertFalse("Hyphen at end is invalid.", NetUtils.isValidLabel("ab-"));
    Assert.assertFalse("Two consecutive hyphens are invalid.", NetUtils.isValidLabel("a--ab"));
    Assert.assertTrue("Valid label with digits.", NetUtils.isValidLabel("B52"));
  }

  @Test
  public void testValidCountryCode()
  {
    Assert.assertFalse("Null is invalid.", NetUtils.isCountryCode(null));
    Assert.assertFalse("Empty is invalid.", NetUtils.isCountryCode(""));
    Assert.assertFalse("Invalid first is invalid.", NetUtils.isCountryCode("!a"));
    Assert.assertFalse("Invalid second is invalid.", NetUtils.isCountryCode("a!"));
    Assert.assertTrue("Valid lower.", NetUtils.isCountryCode("ar"));
    Assert.assertTrue("Valid upper.", NetUtils.isCountryCode("UK"));
    Assert.assertTrue("Valid mixed.", NetUtils.isCountryCode("iT"));
    Assert.assertTrue("Valid mixed.", NetUtils.isCountryCode("Es"));
  }

  @Test
  public void testValidHostname()
  {
    final List<String> list = new ArrayList<>();
    list.add("test");
    Assert.assertFalse("Single label hostname is invalid.", NetUtils.isValidHostname(list));
    list.clear();
    list.add("xy");
    list.add("");
    Assert.assertFalse("Empty label hostname is invalid.", NetUtils.isValidHostname(list));
    list.clear();
    list.add("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghij");
    list.add("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghij");
    list.add("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghij");
    list.add("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghij");
    list.add("abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghij");
    Assert.assertFalse("Hostname exceeding maximum length is invalid.", NetUtils.isValidHostname(list));
  }

  @Test
  public void testParseDottedQuads()
  {
    Assert.assertNull("Null list leads to null result.", NetUtils.parseDottedQuadsIpv4((List<String>) null));

    final List<String> list = new ArrayList<>();
    list.add("1a");
    list.add("0");
    list.add("00");
    list.add("f");
    final Long result = NetUtils.parseDottedQuadsIpv4(list);
    Assert.assertEquals("Single label hostname is invalid.", Long.valueOf((0x1a << 24) + 15), result);

    list.clear();
    list.add("1");
    list.add("0");
    list.add("0");
    list.add("1234");
    Assert.assertNull("Last part too long.", NetUtils.parseDottedQuadsIpv4(list));

    Assert.assertNull("Null input leads to null output.", NetUtils.parseDottedQuadsIpv4((String) null));
    Assert.assertNull("Input too short.", NetUtils.parseDottedQuadsIpv4("1.1.1"));
    Assert.assertNull("Input too long.", NetUtils.parseDottedQuadsIpv4("1000.100.100.100"));
    Assert.assertNull("Not four parts.", NetUtils.parseDottedQuadsIpv4("100.100.100"));
    final Long ipv4 = NetUtils.parseDottedQuadsIpv4("0.1.2.3");
    Assert.assertNotNull("Expect not null on correct input.", ipv4);
    Assert.assertEquals("Expect correct value.", 1L << 16 | 2L << 8 | 3, ipv4.longValue());
  }
}
