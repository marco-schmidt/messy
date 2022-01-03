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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Data class for a message and some minimal metadata.
 *
 * @author Marco Schmidt
 */
public class MessageData
{
  private byte[] content;
  private byte[] contentHash;
  private Date timestamp;
  private Path path;

  public byte[] getContent()
  {
    return content == null ? null : Arrays.copyOf(content, content.length);
  }

  public void setContent(byte[] content)
  {
    this.content = content == null ? null : Arrays.copyOf(content, content.length);
  }

  public byte[] getContentHash()
  {
    return contentHash == null ? null : Arrays.copyOf(contentHash, contentHash.length);
  }

  public static byte[] computeSha256Hash(byte[] data)
  {
    return computeHash("SHA-256", data);
  }

  public static byte[] computeHash(String algorithm, byte[] data)
  {
    try
    {
      final MessageDigest digest = MessageDigest.getInstance(algorithm);
      digest.update(data);
      return digest.digest();
    }
    catch (final NoSuchAlgorithmException e)
    {
      return new byte[]
      {};
    }
  }

  public static String formatHash(byte[] hashValueArray)
  {
    final StringBuilder sb = new StringBuilder();
    if (hashValueArray != null)
    {
      for (int i = 0; i < hashValueArray.length; i++)
      {
        sb.append(String.format(Locale.ROOT, "%02x", hashValueArray[i]));
      }
    }
    return sb.toString();
  }

  public void setContentHash(byte[] contentHash)
  {
    this.contentHash = contentHash == null ? null : Arrays.copyOf(contentHash, contentHash.length);
  }

  public Date getTimestamp()
  {
    return timestamp == null ? null : new Date(timestamp.getTime());
  }

  public void setTimestamp(Date timestamp)
  {
    this.timestamp = timestamp == null ? null : new Date(timestamp.getTime());
  }

  public Path getPath()
  {
    return path;
  }

  public void setPath(Path path)
  {
    this.path = path;
  }
}
