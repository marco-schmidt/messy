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
import org.junit.Assert;
import org.junit.Test;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.imf.ImfHeaderField;
import messy.msgdata.formats.imf.ImfHeaderList;
import messy.msgdata.formats.imf.ImfMessage;

/**
 * Test {@link ImfConverter}.
 *
 * @author Marco Schmidt
 */
public class ImfConverterTest
{
  @Test
  public void testNoUsenet()
  {
    final ImfHeaderList list = new ImfHeaderList();
    final ImfMessage input = new ImfMessage(list, new ArrayList<String>());
    final ImfConverter converter = new ImfConverter();
    final Message output = converter.convert(input);
    Assert.assertNull("Expect null result.", output);
  }

  @Test
  public void testPreRfc850()
  {
    final ImfHeaderList list = new ImfHeaderList();
    final String articleId = "abc.123";
    final String newsgroups = "net.general";
    final String title = "The subject line";
    final String dateString = "Fri Nov 19 16:14:55 1982";
    list.add(new ImfHeaderField("Newsgroups", newsgroups));
    list.add(new ImfHeaderField("Article-I.D.", articleId));
    list.add(new ImfHeaderField("Posted", dateString));
    list.add(new ImfHeaderField("Title", title));
    final ImfMessage input = new ImfMessage(list, new ArrayList<String>());
    final ImfConverter converter = new ImfConverter();
    final Message output = converter.convert(input);
    Assert.assertNotNull("Expect non-null result.", output);
    Assert.assertEquals("Expect identical subject.", title, output.getSubject());
  }

  @Test
  public void testRfc850()
  {
    final ImfHeaderList list = new ImfHeaderList();
    final String newsgroups = "net.general";
    final String subject = "The subject line";
    list.add(new ImfHeaderField("Newsgroups", newsgroups));
    list.add(new ImfHeaderField("Subject", subject));
    final ImfMessage input = new ImfMessage(list, new ArrayList<String>());
    final ImfConverter converter = new ImfConverter();
    final Message output = converter.convert(input);
    Assert.assertNotNull("Expect non-null result.", output);
    Assert.assertEquals("Expect identical subject.", subject, output.getSubject());
  }
}
