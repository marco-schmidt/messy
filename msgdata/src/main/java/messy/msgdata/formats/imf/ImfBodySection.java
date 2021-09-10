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

import java.util.List;
import java.util.Map;

/**
 * Decoded part of an IMF message's body.
 *
 * @author Marco Schmidt
 */
public class ImfBodySection
{
  private String contentType;
  private Map<String, String> contentTypeAttributes;
  private List<String> lines;

  public String getContentType()
  {
    return contentType;
  }

  public void setContentType(String contentType)
  {
    this.contentType = contentType;
  }

  public Map<String, String> getContentTypeAttributes()
  {
    return contentTypeAttributes;
  }

  public void setContentTypeAttributes(Map<String, String> contentTypeAttributes)
  {
    this.contentTypeAttributes = contentTypeAttributes;
  }

  public List<String> getLines()
  {
    return lines;
  }

  public void setLines(List<String> lines)
  {
    this.lines = lines;
  }
}
