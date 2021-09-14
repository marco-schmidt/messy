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

import java.util.Arrays;
import java.util.List;

/**
 * Helper methods for network checks.
 *
 * @author Marco Schmidt
 */
public final class NetUtils
{
  private NetUtils()
  {
  }

  /**
   * Maximum length of a hostname label, a part of hostname separated by dot(s).
   */
  public static final int MAX_LABEL_LENGTH = 63;

  /**
   * Maximum length of entire hostname including dots.
   */
  public static final int MAX_HOSTNAME_LENGTH = 253;

  public static boolean isValidHostname(List<String> labels)
  {
    if (labels.size() < 2)
    {
      return false;
    }
    for (final String item : labels)
    {
      if (!isValidLabel(item))
      {
        return false;
      }
    }
    return true;
  }

  public static boolean isValidLabelChar(char c)
  {
    // obvious implementation ...
    // return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
    // ... has non-perfect code-coverage, see
    // https://stackoverflow.com/questions/31546047/test-coverage-for-if-statement-with-logical-or-with-javas-short-circuiti
    return Character.isLetterOrDigit(c);
  }

  public static boolean isValidLabel(String label)
  {
    if (label == null || label.isEmpty())
    {
      return false;
    }
    final int length = label.length();
    if (length > MAX_LABEL_LENGTH)
    {
      return false;
    }
    final char[] a = label.toCharArray();
    for (int i = 0; i < length; i++)
    {
      final char c = a[i];
      if (isValidLabelChar(c))
      {
        // a to z characters (lower and upper case) and decimal digits are always okay
        continue;
      }

      if (c == '-')
      {
        if (i == 0 || i + 1 == length)
        {
          // no hyphen at beginning or end of label
          return false;
        }
        // note: i - 1 cannot be negative because i cannot be 0 according to previous if statement
        if (a[i - 1] == '-')
        {
          // no two consecutive hyphens
          return false;
        }
      }
      else
      {
        return false;
      }
    }
    return true;
  }

  public static boolean isCountryCodeLetter(char c)
  {
    // obvious implementation ...
    // return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    // ... has non-perfect code-coverage, see
    // https://stackoverflow.com/questions/31546047/test-coverage-for-if-statement-with-logical-or-with-javas-short-circuiti
    return Character.isLetter(c);
  }

  public static boolean isCountryCode(String code)
  {
    if (code == null || code.length() != 2)
    {
      return false;
    }
    return isCountryCodeLetter(code.charAt(0)) && isCountryCodeLetter(code.charAt(1));
  }

  public static Long parseDottedQuadsIpv4(List<String> parts, int radix)
  {
    final int maxLength = radix == 16 ? 2 : 3;
    for (final String part : parts)
    {
      if (part.length() > maxLength)
      {
        return null;
      }
    }
    long result = 0;
    int shift = 24;
    for (final String part : parts)
    {
      try
      {
        final int value = Integer.parseInt(part, radix);
        result |= (long) value << shift;
      }
      catch (final NumberFormatException nfe)
      {
        return null;
      }
      shift -= 8;
    }
    return Long.valueOf(result);
  }

  public static Long parseDottedQuadsIpv4(List<String> parts)
  {
    if (parts == null || parts.size() != 4)
    {
      return null;
    }
    Long value = parseDottedQuadsIpv4(parts, 10);
    if (value == null)
    {
      value = parseDottedQuadsIpv4(parts, 16);
    }
    return value;
  }

  public static Long parseDottedQuadsIpv4(String addr)
  {
    if (addr == null)
    {
      return null;
    }
    final int length = addr.length();
    if (length < 7 || length > 15)
    {
      // min/max length: "1.1.1.1", "100.100.100.100"
      return null;
    }
    final String[] items = addr.split("\\.");
    if (items.length != 4)
    {
      return null;
    }
    return parseDottedQuadsIpv4(Arrays.asList(items));
  }
}
