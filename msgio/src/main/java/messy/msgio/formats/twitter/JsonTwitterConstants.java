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

/**
 * Constants for (de-)serializing JSON tweets.
 *
 * @author Marco Schmidt
 */
public final class JsonTwitterConstants
{
  /**
   * Status field user.
   */
  public static final String STATUS_USER = "user";
  /**
   * User created at value (string with timestamp).
   */
  public static final String USER_CREATED_AT = "created_at";
  /**
   * User id value (positive integer).
   */
  public static final String USER_ID = "id";
  /**
   * User account name (string).
   */
  public static final String USER_SCREEN_NAME = "screen_name";
  /**
   * Has user been verified by Twitter, the white checkmark on blue background (boolean).
   */
  public static final String USER_VERIFIED = "verified";

  private JsonTwitterConstants()
  {
    // avoid instantiation
  }
}
