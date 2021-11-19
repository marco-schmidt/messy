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
package messy.msgsearch.elastic;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import messy.msgdata.formats.Message;
import messy.msgio.formats.JsonMessageFormatter;
import messy.msgio.output.OutputProcessor;

/**
 * Connect to Elasticsearch server and upload JSON messages.
 *
 * @author Marco Schmidt
 */
public class ElasticOutputProcessor extends OutputProcessor
{
  private BulkRequest bulkRequest;
  private RestHighLevelClient client;
  private int currentBulkSize;
  private String host = "localhost";
  private String indexDatePattern = "yyyy-MM";
  private String indexPrefix = "msg-";
  private int maxBulkSize = 5 * 1024 * 1024;
  private int port = 9200;

  public ElasticOutputProcessor()
  {
    super();
    setMessageFormatter(new JsonMessageFormatter());
  }

  @Override
  public void close()
  {
    if (client != null)
    {
      if (currentBulkSize > 0)
      {
        flushBulkRequest();
      }
      try
      {
        client.close();
      }
      catch (final IOException e)
      {
        e.printStackTrace();
      }
      client = null;
    }
  }

  public void connect()
  {
    if (client != null)
    {
      close();
    }
    client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port)));
    bulkRequest = new BulkRequest();
  }

  public String determineIndexName(Message msg)
  {
    final DateFormat formatter = new SimpleDateFormat(indexDatePattern, Locale.ROOT);
    final String date = formatter.format(msg.getSent());
    return indexPrefix + date;
  }

  @Override
  public void dump(Message msg)
  {
    final String indexName = determineIndexName(msg);
    final String json = getMessageFormatter().format(msg);
    final String messageId = msg.getMessageId();
    bulkRequest.add(new IndexRequest(indexName).id(messageId).source(json, XContentType.JSON));
    currentBulkSize += json.length();
    if (currentBulkSize > maxBulkSize)
    {
      flushBulkRequest();
    }
  }

  public void flushBulkRequest()
  {
    try
    {
      client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }
    catch (final IOException e)
    {
      e.printStackTrace();
    }
    currentBulkSize = 0;
  }

  public int getMaxBulkSize()
  {
    return maxBulkSize;
  }

  public void setMaxBulkSize(int maxBulkSize)
  {
    this.maxBulkSize = maxBulkSize;
  }

  public String getHost()
  {
    return host;
  }

  public void setHost(String host)
  {
    this.host = host;
  }

  public String getIndexDatePattern()
  {
    return indexDatePattern;
  }

  public void setIndexDatePattern(String indexDatePattern)
  {
    this.indexDatePattern = indexDatePattern;
  }

  public String getIndexPrefix()
  {
    return indexPrefix;
  }

  public void setIndexPrefix(String indexPrefix)
  {
    this.indexPrefix = indexPrefix;
  }

  public int getPort()
  {
    return port;
  }

  public void setPort(int port)
  {
    this.port = port;
  }
}
