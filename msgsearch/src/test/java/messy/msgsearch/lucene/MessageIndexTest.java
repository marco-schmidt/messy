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
package messy.msgsearch.lucene;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import messy.msgdata.formats.Message;

/**
 * Test {@link MessageIndex}.
 */
public class MessageIndexTest
{
  private Analyzer analyzer;
  private Directory directory;
  private MessageIndex index;

  @Before
  public void setUp() throws IOException
  {
    analyzer = new StandardAnalyzer();
    directory = new ByteBuffersDirectory();
    index = new MessageIndex(directory, analyzer);
    for (int i = 0; i < DocumentConverterTest.SUBJECTS.length; i++)
    {
      index.add(DocumentConverterTest.createMessage(i));
    }
    index.closeWriter();
  }

  @Test
  public void testAllHits() throws IOException, ParseException
  {
    final DirectoryReader reader = DirectoryReader.open(directory);
    final IndexSearcher searcher = new IndexSearcher(reader);
    final QueryParser parser = new QueryParser(Message.Item.SUBJECT.name(), analyzer);
    final Query query = parser.parse(DocumentConverterTest.SUBJECT_LAST_WORD);
    final ScoreDoc[] hits = searcher.search(query, 10).scoreDocs;
    Assert.assertEquals("Expecting two matches.", 2, hits.length);
    for (int i = 0; i < hits.length; i++)
    {
      final Document hitDoc = searcher.doc(hits[i].doc);
      Assert.assertEquals("Expecting stored subject to be the same as in input message used to build index.",
          DocumentConverterTest.SUBJECTS[i], hitDoc.get(Message.Item.SUBJECT.name()));
    }
    reader.close();
  }

  @Test
  public void testSmallerThan() throws IOException, ParseException
  {
    final DirectoryReader reader = DirectoryReader.open(directory);
    final IndexSearcher searcher = new IndexSearcher(reader);

    final Query query = LongPoint.newRangeQuery(Message.Item.SENT.name(), 0, DocumentConverterTest.SENT_1 + 1);
    final ScoreDoc[] hits = searcher.search(query, 10).scoreDocs;
    Assert.assertEquals("Expecting one match.", 1, hits.length);

    reader.close();
  }

  @After
  public void tearDown() throws IOException
  {
    directory.close();
  }
}
