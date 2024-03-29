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
package messy.msgdata.formats.imf;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class to store a list of {@link ImfHeaderField} objects.
 *
 * @author Marco Schmidt
 */
public class ImfHeaderList
{
  private final List<ImfHeaderField> headers;

  public ImfHeaderList()
  {
    headers = new ArrayList<ImfHeaderField>();
  }

  public void add(ImfHeaderField header)
  {
    headers.add(header);
  }

  public int size()
  {
    return headers.size();
  }

  public ImfHeaderField get(int index)
  {
    return headers.get(index);
  }
}
