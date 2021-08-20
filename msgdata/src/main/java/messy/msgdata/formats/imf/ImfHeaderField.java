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
package messy.msgdata.formats.imf;

/**
 * <p>
 * Data class to store a single header field of an IMF (Internet Message Format) message. This header consists of a
 * field name and field body. Header fields are described in
 * <a target="_top" href="https://tools.ietf.org/html/rfc5322#section-2.2">section 2.2 of RFC 5322</a>.
 * </p>
 *
 * @author Marco Schmidt
 */
public class ImfHeaderField
{
  private final String fieldName;
  private final String fieldBody;

  public ImfHeaderField(String name, String body)
  {
    fieldName = name;
    fieldBody = body;
  }

  public String getFieldBody()
  {
    return fieldBody;
  }

  public String getFieldName()
  {
    return fieldName;
  }
}
