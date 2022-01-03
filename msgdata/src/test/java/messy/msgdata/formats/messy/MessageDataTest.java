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
package messy.msgdata.formats.messy;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

public class MessageDataTest
{
  @Test
  public void testInvalidAlgorithm()
  {
    final byte[] hash = MessageData.computeHash("Invalid algorithm name", new byte[]
    {});
    Assert.assertNotNull("Invalid hash algorithm name leads to non-null output.", hash);
    Assert.assertEquals("Invalid hash algorithm name leads to empty output.", 0, hash.length);
  }

  @Test
  public void testNullGetters()
  {
    final MessageData msg = new MessageData();
    msg.setContent(null);
    Assert.assertNull("Assigning null content leads to null.", msg.getContent());
    msg.setContentHash(null);
    Assert.assertNull("Assigning null content hash leads to null.", msg.getContentHash());
    msg.setTimestamp(null);
    Assert.assertNull("Assigning null timestamp  leads to null.", msg.getTimestamp());
  }

  @Test
  public void testNullFormatter()
  {
    final String string = MessageData.formatHash(null);
    Assert.assertNotNull("Null hash leads to non-null string.", string);
    Assert.assertEquals("Null hash leads to empty string.", "", string);
  }

  @Test
  public void testRegular()
  {
    final MessageData msg = new MessageData();

    final byte[] content = new byte[0];
    msg.setContent(content);
    final byte[] contentCheck = msg.getContent();
    Assert.assertNotNull(contentCheck);
    Assert.assertArrayEquals("Same size as input.", content, contentCheck);

    final byte[] hash = MessageData.computeSha256Hash(content);
    final String hashString = MessageData.formatHash(hash);
    Assert.assertEquals("SHA-256 on empty input equal to expected string.",
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hashString);
    msg.setContentHash(hash);
    final byte[] hashCheck = msg.getContentHash();
    Assert.assertNotNull(hashCheck);
    Assert.assertArrayEquals("Hash content identical.", hash, hashCheck);

    final Date epoch = new Date(0L);
    msg.setTimestamp(epoch);
    final Date timestampCheck = msg.getTimestamp();
    Assert.assertEquals("Time unchanged.", epoch.getTime(), timestampCheck.getTime());

    msg.setPath(Paths.get("."));
    final Path pathCheck = msg.getPath();
    Assert.assertEquals("Path unchanged.", ".", pathCheck.toString());
  }
}
