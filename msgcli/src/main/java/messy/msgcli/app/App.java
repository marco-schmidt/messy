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
package messy.msgcli.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import messy.msgdata.formats.Message;
import messy.msgdata.formats.twitter.TwitterStatus;
import messy.msgio.formats.twitter.JsonTwitterParser;

/**
 * Command-line application to provide messy conversion functionality.
 */
public final class App
{
  private App()
  {
    // prevent instantiation
  }

  private static void dump(Message msg)
  {
    System.out.println(msg.getSent() + "\t" + msg.getMessageId() + "\t" + msg.getAuthorName());
  }

  private static void processStandardInput()
  {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)))
    {
      String line;
      while ((line = in.readLine()) != null)
      {
        TwitterStatus status = JsonTwitterParser.parseStatus(line);
        Message msg = JsonTwitterParser.toMessage(status);
        dump(msg);
      }
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
    processStandardInput();
  }
}
