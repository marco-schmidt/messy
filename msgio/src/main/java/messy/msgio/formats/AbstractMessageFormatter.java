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
import java.util.List;
import messy.msgdata.formats.Message;

/**
 * Abstract base class for printing {@link Message} objects.
 *
 * @author Marco Schmidt
 */
public abstract class AbstractMessageFormatter
{
  private DateFormat dateFormatter;
  private List<Message.Item> items;

  public AbstractMessageFormatter()
  {
    this(null);
  }

  public AbstractMessageFormatter(DateFormat df)
  {
    setDateFormatter(df);
  }

  public DateFormat getDateFormatter()
  {
    return dateFormatter;
  }

  public void setDateFormatter(DateFormat dateFormatter)
  {
    this.dateFormatter = dateFormatter;
  }

  public List<Message.Item> getItems()
  {
    return items;
  }

  public void setItems(List<Message.Item> items)
  {
    this.items = items;
  }

  /**
   * Convert argument message to String.
   *
   * @param msg
   *          message to be converted
   * @return resulting String object
   */
  public abstract String format(Message msg);
}
