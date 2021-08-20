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
package messy.msgsearch.index;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import messy.msgdata.formats.Message;

/**
 * Create and update a message index.
 *
 * @author Marco Schmidt
 */
public class MessageIndex
{
  private final Analyzer analyzer;
  private final Directory directory;
  private final IndexWriter writer;

  public MessageIndex(Directory dir, Analyzer ana) throws IOException
  {
    analyzer = ana;
    directory = dir;
    final IndexWriterConfig config = new IndexWriterConfig(analyzer);
    writer = new IndexWriter(directory, config);
  }

  public void add(Message msg) throws IOException
  {
    final Document doc = new DocumentConverter().from(msg);
    writer.addDocument(doc);
  }

  public void closeWriter() throws IOException
  {
    writer.close();
  }
}
