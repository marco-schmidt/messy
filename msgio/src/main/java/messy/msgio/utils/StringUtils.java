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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class StringUtils
{
  private StringUtils()
  {
  }

  public static String concatItems(List<String> elems)
  {
    return concatItems(elems, " ");
  }

  public static String concatItems(List<String> elems, String delimiter)
  {
    final StringBuilder sb = new StringBuilder();
    if (elems != null)
    {
      boolean later = false;
      for (final String elem : elems)
      {
        if (later)
        {
          sb.append(delimiter);
        }
        else
        {
          later = true;
        }
        sb.append(elem);
      }
    }
    return sb.toString();
  }

  public static List<String> splitAndNormalize(String s, String delimiter)
  {
    final List<String> result = new ArrayList<>();
    if (s == null)
    {
      return result;
    }
    final String[] items = s.split(delimiter);
    for (final String item : items)
    {
      final String i = item.trim().toLowerCase(Locale.ROOT);
      if (!i.isEmpty())
      {
        result.add(i);
      }
    }
    return result;
  }

  public static String escape(String s)
  {
    String result;
    if (s == null)
    {
      result = null;
    }
    else
    {
      if (s.isEmpty())
      {
        result = s;
      }
      else
      {
        boolean modified = false;
        final char[] a = s.toCharArray();
        for (int i = 0; i < a.length; i++)
        {
          final char c = a[i];
          if (c == 9 || c == 10 || c == 13)
          {
            a[i] = ' ';
            modified = true;
          }
        }
        if (modified)
        {
          result = new String(a);
        }
        else
        {
          result = s;
        }
      }
    }
    return result;
  }
}
