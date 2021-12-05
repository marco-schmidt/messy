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

import org.junit.Assert;
import org.junit.Test;

public class LanguageDetectionTest
{
  @Test
  public void testFromGoogleLanguage()
  {
    Assert.assertNull("Null input leads to null output.", LanguageDetection.fromGoogleLanguage(null));
    Assert.assertEquals("ENGLISH leads to en.", "en", LanguageDetection.fromGoogleLanguage("ENGLISH,ASCII-7-bit"));
  }
}
