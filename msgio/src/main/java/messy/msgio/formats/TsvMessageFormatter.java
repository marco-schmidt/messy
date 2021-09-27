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
package messy.msgio.formats;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.Message.Item;
import messy.msgio.utils.StringUtils;

/**
 * Format {@link Message} objects in <a href="https://en.wikipedia.org/wiki/Tab-separated_values">tab-separated values
 * (TSV)</a> format.
 *
 * @author Marco Schmidt
 */
public class TsvMessageFormatter extends AbstractMessageFormatter
{
  private static final char COMMA = ',';
  private static final char TAB = '\t';

  protected void appendList(StringBuilder sb, List<?> list)
  {
    final Iterator<?> iter = list.iterator();
    boolean first = true;
    while (iter.hasNext())
    {
      final Object obj2 = iter.next();
      if (first)
      {
        first = false;
      }
      else
      {
        sb.append(COMMA);
      }
      append(sb, obj2);
    }
  }

  private void append(StringBuilder sb, Object obj)
  {
    if (obj instanceof List<?>)
    {
      appendList(sb, (List<?>) obj);
    }
    else
    {
      if (obj instanceof Date)
      {
        final DateFormat dateFormatter = getDateFormatter();
        sb.append(dateFormatter.format((Date) obj));
      }
      else
      {
        sb.append(escape(obj.toString()));
      }
    }
  }

  protected String escape(String s)
  {
    return StringUtils.escape(s);
  }

  @Override
  public String format(Message msg)
  {
    final StringBuilder sb = new StringBuilder();
    final List<Item> items = getItems();
    boolean first = true;
    for (final Item item : items)
    {
      if (first)
      {
        first = false;
      }
      else
      {
        sb.append(TAB);
      }
      final Object obj = msg.get(item);
      if (obj != null)
      {
        append(sb, obj);
      }
    }
    return sb.toString();
  }
}
