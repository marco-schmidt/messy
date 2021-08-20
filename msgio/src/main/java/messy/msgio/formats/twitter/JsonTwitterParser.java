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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import messy.msgdata.formats.twitter.TwitterStatus;

/**
 * Parse JSON string to {@link TwitterStatus} object.
 *
 * @author Marco Schmidt
 */
public final class JsonTwitterParser
{
  /**
   * Name for timestamp of tweet creation.
   */
  public static final String CREATED_AT = "created_at";
  /**
   * Tweet id number, positive integer.
   */
  public static final String ID = "id";

  private JsonTwitterParser()
  {
  }

  public static BigInteger parseBigInteger(Object o)
  {
    if (o == null)
    {
      return null;
    }
    try
    {
      return new BigInteger(o.toString());
    }
    catch (final NumberFormatException nfe)
    {
      return null;
    }
  }

  public static Date parseTimestamp(Object obj)
  {
    if (obj == null)
    {
      return null;
    }
    final DateFormat parser = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
    parser.setLenient(false);
    try
    {
      final Date result = parser.parse(obj.toString());
      return result;
    }
    catch (final ParseException e)
    {
      return null;
    }
  }
}
