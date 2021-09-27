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
import java.util.List;
import java.util.Locale;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.Message.Item;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

/**
 * Format {@link Message} objects in JSON format.
 *
 * @author Marco Schmidt
 */
public class JsonMessageFormatter extends AbstractMessageFormatter
{
  protected void append(JSONObject res, String key, Object obj)
  {
    if (obj instanceof List<?>)
    {
      appendList(res, key, (List<?>) obj);
    }
    else
    {
      if (obj instanceof Date)
      {
        final DateFormat dateFormatter = getDateFormatter();
        res.put(key, dateFormatter.format((Date) obj));
      }
      else
      {
        res.put(key, obj.toString());
      }
    }
  }

  private void appendList(JSONObject res, String key, List<?> list)
  {
    final JSONArray array = new JSONArray();
    for (final Object o : list)
    {
      array.add(o.toString());
    }
    res.put(key, array);
  }

  @Override
  public String format(Message msg)
  {
    final JSONObject res = new JSONObject();
    final List<Item> items = getItems();
    for (final Item item : items)
    {
      final Object obj = msg.get(item);
      if (obj != null)
      {
        final String key = item.name().toLowerCase(Locale.ROOT);
        append(res, key, obj);
      }
    }
    return JSONValue.toJSONString(res, JSONValue.COMPRESSION);
  }
}
