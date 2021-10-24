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
package messy.msgio.output;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import messy.msgdata.formats.Message;
import messy.msgdata.formats.anews.ANewsMessage;
import messy.msgdata.formats.imf.ImfHeaderList;
import messy.msgdata.formats.imf.ImfMessage;
import messy.msgdata.formats.mbox.MboxMessage;
import messy.msgdata.formats.twitter.TwitterStatus;
import messy.msgio.formats.AbstractMessageFormatter;
import messy.msgio.formats.anews.ANewsMessageConverter;
import messy.msgio.formats.imf.ImfConverter;
import messy.msgio.formats.imf.ImfParser;
import messy.msgio.formats.twitter.JsonTwitterParser;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;

/**
 * Write messages to their destination.
 *
 * @author Marco Schmidt
 */
public class OutputProcessor
{
  private static final Logger LOGGER = LoggerFactory.getLogger(OutputProcessor.class);
  private AbstractMessageFormatter messageFormatter;

  private void dump(Message msg)
  {
    System.out.println(getMessageFormatter().format(msg));
  }

  public AbstractMessageFormatter getMessageFormatter()
  {
    return messageFormatter;
  }

  public void setMessageFormatter(AbstractMessageFormatter messageFormatter)
  {
    this.messageFormatter = messageFormatter;
  }

  private Message toMessage(List<String> headerLines, List<String> bodyLines)
  {
    final ImfHeaderList headerList = new ImfParser().createMessageHeaderList(headerLines);
    final ImfMessage imfMsg = new ImfMessage(headerList, bodyLines);
    return new ImfConverter().convert(imfMsg);
  }

  public void write(ANewsMessage msg)
  {
    dump(ANewsMessageConverter.toMessage(msg));
  }

  public void write(MboxMessage mboxMsg, long lineNumber)
  {
    final Message msg = toMessage(mboxMsg.getHeaderLines(), mboxMsg.getBodyLines());
    if (msg == null)
    {
      final StructuredArgument lineRec = StructuredArguments.value("line_nr", lineNumber);
      LOGGER.error("Null mbox message in line {}.", lineRec);
    }
    else
    {
      dump(msg);
    }
  }

  public void write(TwitterStatus status)
  {
    final Message msg = JsonTwitterParser.toMessage(status);
    dump(msg);
  }

  public boolean write(List<String> headerLines, List<String> bodyLines)
  {
    final Message msg = toMessage(headerLines, bodyLines);

    if (msg == null)
    {
      return false;
    }
    else
    {
      dump(msg);
      return true;
    }
  }
}
