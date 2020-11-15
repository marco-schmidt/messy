/*
 * Copyright 2020 the original author or authors.
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
import messy.msgdata.formats.RawMessage;

/**
 * Data class to store both a {@link ImfHeaderList} and a {@link java.util.List} of strings for the message body.
 *
 * @author Marco Schmidt
 */
public class ImfMessage
{
  private ImfHeaderList headerList;
  private List<String> bodyLines;
  private RawMessage rawMessage;

  public ImfMessage(ImfHeaderList headers, List<String> body, RawMessage msg)
  {
    setHeaderList(headers);
    setBodyLines(body);
    setRawMessage(msg);
  }

  public ImfMessage(ImfHeaderList headers, List<String> body)
  {
    this(headers, body, null);
  }

  public List<String> getBodyLines()
  {
    return bodyLines;
  }

  public ImfHeaderList getHeaderList()
  {
    return headerList;
  }

  private void setBodyLines(List<String> body)
  {
    bodyLines = body;
  }

  private void setHeaderList(ImfHeaderList headers)
  {
    headerList = headers;
  }

  public RawMessage getRawMessage()
  {
    return rawMessage;
  }

  private void setRawMessage(RawMessage msg)
  {
    rawMessage = msg;
  }
}
