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
package messy.msgsearch.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.TextField;
import messy.msgdata.formats.Message;

/**
 * Convert {@link Message} to {@link Document} objects.
 *
 * @author Marco Schmidt
 */
public class DocumentConverter
{
  /**
   * Convert a message to a document.
   *
   * @param msg
   *          {@link Message} to be converted
   * @return resulting {@link Document}
   */
  public Document from(Message msg)
  {
    final Document doc = new Document();
    doc.add(new Field(Message.Item.SUBJECT.name(), msg.getSubject(), TextField.TYPE_STORED));
    doc.add(new LongPoint(Message.Item.SENT.name(), msg.getSent() == null ? 0L : msg.getSent().getTime()));

    return doc;
  }
}
