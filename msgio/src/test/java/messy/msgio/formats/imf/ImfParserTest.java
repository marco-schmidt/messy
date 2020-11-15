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
package messy.msgio.formats.imf;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import messy.msgdata.formats.imf.ImfHeaderField;
import messy.msgdata.formats.imf.ImfHeaderList;

/**
 * Test {@link ImfParser}.
 *
 * @author Marco Schmidt
 */
public class ImfParserTest
{
  @Test
  public void testNullHeaderList()
  {
    Assert.assertNull("Expect null list to be parsed to null.", new ImfParser().createMessageHeaderList(null));
  }

  @Test
  public void testEmptyHeaderList()
  {
    final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(new ArrayList<>());
    Assert.assertNotNull("Expect empty list to be parsed to non-null header list.", headerList);
    Assert.assertEquals("Expect empty list to be parsed to empty header list.", 0, headerList.size());
  }

  @Test
  public void testEmptyStringOnly()
  {
    final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(Arrays.asList(new String[]
    {
        ""
    }));
    Assert.assertNotNull("Expect empty list to be parsed to non-null header list.", headerList);
    Assert.assertEquals("Expect empty list to be parsed to empty header list.", 0, headerList.size());
  }

  @Test
  public void testSingleStringSingleSpace()
  {
    final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(Arrays.asList(new String[]
    {
        " "
    }));
    Assert.assertNotNull("Expect single-string one space list to be parsed to non-null header list.", headerList);
    Assert.assertEquals("Expect single-string one space to be parsed to empty header list.", 0, headerList.size());
  }

  @Test
  public void testSingleStringRegular()
  {
    final String name = "Subject";
    final String value = "Just a test";
    final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(Arrays.asList(new String[]
    {
        name + ImfParser.HEADER_FIELD_SEPARATOR + " " + value
    }));
    Assert.assertNotNull("Expect single-string one space list to be parsed to non-null header list.", headerList);
    Assert.assertEquals("Expect valid single line case to be parsed to size 1 header list.", 1, headerList.size());
    final ImfHeaderField field = headerList.get(0);
    Assert.assertNotNull("Expect only element to be non-null.", field);
    Assert.assertEquals("Expect field name to be parsed correctly.", name, field.getFieldName());
    Assert.assertEquals("Expect field body to be parsed correctly.", value, field.getFieldBody());
  }

  @Test
  public void testSingleStringNonSeparator()
  {
    final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(Arrays.asList(new String[]
    {
        "contains no separator colon"
    }));
    Assert.assertNotNull("Expect single-string list without valid separator to be parsed to non-null header list.",
        headerList);
    Assert.assertEquals("Expect single-string list without valid separator to be parsed to size 0 header list.", 0,
        headerList.size());
  }

  @Test
  public void testSingleStringSeparatorAsLastCharacter()
  {
    final String name = "Subject";

    final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(Arrays.asList(new String[]
    {
        name + ImfParser.HEADER_FIELD_SEPARATOR
    }));
    Assert.assertNotNull("Expect separator at the end to be parsed to non-null header list.", headerList);
    Assert.assertEquals("Expect separator at the end to be parsed to size 1 header list.", 1, headerList.size());
    final ImfHeaderField field = headerList.get(0);
    Assert.assertNotNull("Expect only element to be non-null.", field);
    Assert.assertEquals("Expect field name to be parsed correctly.", name, field.getFieldName());
    Assert.assertEquals("Expect field body to be parsed correctly.", "", field.getFieldBody());
  }

  @Test
  public void testSingleHeaderTwoLines()
  {
    final String name = "Subject";
    final String value1 = "This is";
    final String value2 = "to be continued";
    final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(Arrays.asList(new String[]
    {
        name + ImfParser.HEADER_FIELD_SEPARATOR + ' ' + value1, "  " + value2
    }));
    Assert.assertNotNull("Expect lines to be parsed to non-null header list.", headerList);
    Assert.assertEquals("Expect lines to be parsed to size 1 header list.", 1, headerList.size());
    final ImfHeaderField field = headerList.get(0);
    Assert.assertNotNull("Expect only element to be non-null.", field);
    Assert.assertEquals("Expect field name to be parsed correctly.", name, field.getFieldName());
    Assert.assertEquals("Expect field body to be parsed correctly.", value1 + value2, field.getFieldBody());
  }

  @Test
  public void testSingleHeaderTwoLinesSecondLineFieldEmpty()
  {
    final String name = "Subject";
    final String value = "This is";
    final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(Arrays.asList(new String[]
    {
        name + ImfParser.HEADER_FIELD_SEPARATOR + ' ' + value, " "
    }));
    Assert.assertNotNull("Expect lines to be parsed to non-null header list.", headerList);
    Assert.assertEquals("Expect lines to be parsed to size 1 header list.", 1, headerList.size());
    final ImfHeaderField field = headerList.get(0);
    Assert.assertNotNull("Expect only element to be non-null.", field);
    Assert.assertEquals("Expect field name to be parsed correctly.", name, field.getFieldName());
    Assert.assertEquals("Expect field body to be parsed correctly.", value, field.getFieldBody());
  }
}
