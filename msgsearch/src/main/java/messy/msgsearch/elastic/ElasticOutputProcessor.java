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

import static net.logstash.logback.argument.StructuredArguments.value;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticOutputProcessor.class);
  private BulkRequest bulkRequest;
  private RestHighLevelClient client;
  private int currentBulkSize;
  private int currentMessages;
  private long totalMessages;
  private long connectionTimeMillis;
  private String host = "localhost";
  private String indexDatePattern = "yyyy";
  private String indexPrefix = "msg-";
  private int maxBulkSize = 3 * 1024 * 1024;
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
        final long millis = System.currentTimeMillis() - connectionTimeMillis;
        LOGGER.info("Closed connection to Elastic server '{}:{}', sent {} message(s) in {} ms.", value("host", host),
            value("port", Integer.valueOf(port)), value("total_messages", Long.valueOf(totalMessages)),
            value("duration_ms", Long.valueOf(millis)));
      }
      catch (final IOException e)
      {
        LOGGER.error("Failure closing Elastic client: {}.", e.getMessage());
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
    currentBulkSize = 0;
    currentMessages = 0;
    totalMessages = 0;
    connectionTimeMillis = System.currentTimeMillis();
    LOGGER.info("Connected to Elastic server '{}:{}'.", value("host", host), value("port", Integer.valueOf(port)));
  }

  public String determineIndexName(Message msg)
  {
    final DateFormat formatter = new SimpleDateFormat(indexDatePattern, Locale.ROOT);
    final Date sent = msg.getSent();
    final String date = sent == null ? "misc" : formatter.format(sent);
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
    currentMessages++;
    totalMessages++;
    if (currentBulkSize > maxBulkSize)
    {
      flushBulkRequest();
    }
  }

  public void flushBulkRequest()
  {
    try
    {
      final long now = System.currentTimeMillis();
      client.bulk(bulkRequest, RequestOptions.DEFAULT);
      LOGGER.info("Sent {} messages to Elastic server, {} characters in {} ms.",
          value("num_messages", Integer.valueOf(currentMessages)), value("num_chars", Integer.valueOf(currentBulkSize)),
          value("duration_ms", Long.valueOf(System.currentTimeMillis() - now)));
    }
    catch (final IOException e)
    {
      LOGGER.error("Failure flushing bulk request to Elastic: {}.", e.getMessage());
    }
    bulkRequest = new BulkRequest();
    currentBulkSize = 0;
    currentMessages = 0;
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
