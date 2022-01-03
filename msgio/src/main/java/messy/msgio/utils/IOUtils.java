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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods for stream and file I/O.
 *
 * @author Marco Schmidt
 */
public final class IOUtils
{
  private IOUtils()
  {
  }

  public static BufferedReader openAsBufferedReader(InputStream in, Charset cs)
  {
    return new BufferedReader(new InputStreamReader(in, cs));
  }

  public static List<String> toLines(InputStream is)
  {
    final BufferedReader reader = openAsBufferedReader(is, StandardCharsets.ISO_8859_1);
    final List<String> lines = new ArrayList<>();
    String line;
    try
    {
      while ((line = reader.readLine()) != null)
      {
        lines.add(line);
      }
    }
    catch (final IOException e)
    {
      e.printStackTrace();
    }
    return lines;
  }
}
