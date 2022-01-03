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

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test t {@link ImfMessage}.
 *
 * @author Marco Schmidt
 */
public class ImfMessageTest
{
  @Test
  public void testEmptyHeaderList()
  {
    final String bodyLine = "Just a single line.";
    final List<String> body = Arrays.asList(new String[]
    {
        bodyLine
    });
    final ImfHeaderList headerList = new ImfHeaderList();
    final String name = "Subject";
    final String fieldBody = "A description of the content of the message";
    final ImfHeaderField field = new ImfHeaderField(name, fieldBody);
    headerList.add(field);
    final ImfMessage message = new ImfMessage(headerList, body);

    final List<String> bodyLines = message.getBodyLines();
    Assert.assertNotNull("Expect non-null body list.", bodyLines);
    Assert.assertEquals("Expect body list to have one item.", 1, bodyLines.size());
    final String firstLine = bodyLines.get(0);
    Assert.assertNotNull("Expect non-null body line.", firstLine);
    Assert.assertEquals("Expect body line to be identical to initial line.", bodyLine, firstLine);
    final ImfHeaderList retrHeaderList = message.getHeaderList();
    Assert.assertNotNull("Expect non-null header list.", retrHeaderList);
    Assert.assertEquals("Expect header list to have one item.", 1, retrHeaderList.size());
    final ImfHeaderField retrHeaderField = retrHeaderList.get(0);
    Assert.assertNotNull("Expect non-null header field.", retrHeaderField);
    Assert.assertEquals("Expect field name to be identical to initial name.", name, retrHeaderField.getFieldName());
    Assert.assertEquals("Expect field body to be identical to initial body.", fieldBody,
        retrHeaderField.getFieldBody());
    Assert.assertNull("Expect null raw message.", message.getRawMessage());
  }
}
