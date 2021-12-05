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
package messy.msgio.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Helper class to detect languages.
 *
 * @author Marco Schmidt
 */
public final class LanguageDetection
{
  private static Map<String, String> fullNameToIso639;

  private LanguageDetection()
  {
  }

  private static void loadMappings()
  {
    final InputStream inputStream = LanguageDetection.class.getResourceAsStream("languages.tsv");
    fullNameToIso639 = new HashMap<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
    {
      String line;
      while ((line = reader.readLine()) != null)
      {
        final String[] items = line.split("\t");
        if (items.length < 2)
        {
          continue;
        }
        fullNameToIso639.put(items[0], items[1]);
      }
    }
    catch (final IOException e)
    {
    }
  }

  public static String fromGoogleLanguage(String value)
  {
    if (value == null)
    {
      return null;
    }
    final String[] items = value.split(",");
    if (items.length < 2)
    {
      return null;
    }
    if (fullNameToIso639 == null)
    {
      loadMappings();
    }
    final String name = items[0].toUpperCase(Locale.ROOT);
    return fullNameToIso639.get(name);
  }
}
