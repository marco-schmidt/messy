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
package messy.msgio.formats.twitter;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.twitter.TwitterPlace;
import messy.msgdata.formats.twitter.TwitterStatus;
import messy.msgdata.formats.twitter.TwitterUser;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

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
   * Field for deletion status.
   */
  public static final String DELETE = "delete";
  /**
   * Tweet id number, positive integer.
   */
  public static final String ID = "id";
  /**
   * Tweet language.
   */
  public static final String LANGUAGE = "lang";
  /**
   * Tweet message text.
   */
  public static final String TEXT = "text";
  /**
   * Format of JSON tweet messages.
   */
  public static final String FORMAT_JSON_TWEET = "jsontweet";

  private JsonTwitterParser()
  {
  }

  /**
   * Parse a line containing a single JSON object and return a {@link TwitterStatus} object with some of its
   * information.
   *
   * @param s
   *          JSON string to be parsed
   * @return parsed {@link TwitterStatus} or null if parsing failed
   */
  public static TwitterStatus parseStatus(String s)
  {
    if (s == null)
    {
      return null;
    }
    final Object obj = JSONValue.parse(s);
    if (obj == null)
    {
      return null;
    }
    if (!(obj instanceof JSONObject))
    {
      return null;
    }
    final JSONObject j = (JSONObject) obj;
    final TwitterStatus result = new TwitterStatus();
    result.setCreatedAt(parseTimestamp(j.get(CREATED_AT)));
    final Object delete = j.get(DELETE);
    result.setDelete(delete != null);
    result.setId(parseBigInteger(j.get(ID)));
    result.setLanguage(asString(j.get(LANGUAGE)));
    result.setPlace(parsePlace(j.get(JsonTwitterConstants.STATUS_PLACE)));
    result.setText(asString(j.get(TEXT)));
    result.setUser(parseUser(j.get(JsonTwitterConstants.STATUS_USER)));
    return result;
  }

  public static String asString(Object o)
  {
    return o == null ? null : o.toString();
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

  public static boolean parseBoolean(Object o)
  {
    boolean result;
    if (o == null)
    {
      result = false;
    }
    else
    {
      result = "true".equals(o.toString());
    }
    return result;
  }

  public static TwitterPlace parsePlace(Object object)
  {
    if (object == null || !(object instanceof JSONObject))
    {
      return null;
    }
    final JSONObject j = (JSONObject) object;

    final TwitterPlace result = new TwitterPlace();
    result.setCountry(asString(j.get(JsonTwitterConstants.PLACE_COUNTRY)));
    result.setCountryCode(asString(j.get(JsonTwitterConstants.PLACE_COUNTRY_CODE)));
    result.setId(asString(j.get(JsonTwitterConstants.PLACE_ID)));
    result.setFullName(asString(j.get(JsonTwitterConstants.PLACE_FULL_NAME)));
    result.setName(asString(j.get(JsonTwitterConstants.PLACE_NAME)));
    result.setType(asString(j.get(JsonTwitterConstants.PLACE_TYPE)));

    return result;
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

  protected static TwitterUser parseUser(Object object)
  {
    if (object == null || !(object instanceof JSONObject))
    {
      return null;
    }
    final JSONObject j = (JSONObject) object;
    final TwitterUser result = new TwitterUser();
    result.setId(parseBigInteger(j.get(JsonTwitterConstants.USER_ID)));
    result.setCreatedAt(parseTimestamp(j.get(JsonTwitterConstants.USER_CREATED_AT)));
    result.setScreenName(asString(j.get(JsonTwitterConstants.USER_SCREEN_NAME)));
    result.setVerified(parseBoolean(j.get(JsonTwitterConstants.USER_VERIFIED)));
    return result;
  }

  /**
   * Convert {@link TwitterStatus} to {@link Message}.
   *
   * @param msg
   *          input twitter message
   * @return converted {@link Message}
   */
  public static Message toMessage(TwitterStatus msg)
  {
    final Message result = new Message();
    result.setFormat(FORMAT_JSON_TWEET);
    result.setMedium(Message.MEDIUM_TWITTER);
    final TwitterUser user = msg.getUser();
    if (user != null)
    {
      final BigInteger id = user.getId();
      result.setAuthorId(id == null ? null : id.toString());
      result.setAuthorName(user.getScreenName());
    }
    final TwitterPlace place = msg.getPlace();
    if (place != null)
    {
      result.setCountryCode(place.getCountryCode());
    }
    result.setLanguageCode(msg.getLanguage());
    final BigInteger id = msg.getId();
    result.setMessageId(id == null ? null : id.toString());
    result.setSent(msg.getCreatedAt());
    result.setText(msg.getText());
    return result;
  }
}
