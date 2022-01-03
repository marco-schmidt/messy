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
package messy.msgio.formats.messy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import messy.msgdata.formats.messy.MessageData;

/**
 * Writes messages to a directory filled with tar archive files.
 *
 * @author Marco Schmidt
 */
public class FileSystemTarMessageSink extends AbstractTarMessageSink
{
  private final Path directory;
  private long currentFileNumber;
  private TarArchiveOutputStream out;

  public FileSystemTarMessageSink(final Path directory, final long maxFileSize)
  {
    super(maxFileSize);
    this.directory = directory.toAbsolutePath();
    this.currentFileNumber = 0;
  }

  @Override
  public void close() throws IOException
  {
    if (out != null)
    {
      out.close();
      out = null;
    }
  }

  private String formatFileNumber(final long fileNumber)
  {
    return String.format("%08d", fileNumber);
  }

  private void ensureOutputStream() throws IOException
  {
    if (out == null)
    {
      Path candidate = null;
      while (true)
      {
        currentFileNumber++;
        final String fileName = formatFileNumber(currentFileNumber) + ".tar";
        candidate = directory.resolve(fileName);
        if (Files.notExists(candidate))
        {
          break;
        }
      }
      out = new TarArchiveOutputStream(Files.newOutputStream(candidate, StandardOpenOption.CREATE));
    }
  }

  @Override
  public void put(MessageData msg) throws IOException
  {
    ensureOutputStream();
    final byte[] content = msg.getContent();
    byte[] contentHash = msg.getContentHash();
    if (contentHash == null)
    {
      contentHash = MessageData.computeSha256Hash(content);
      msg.setContentHash(contentHash);
    }
    final String hashString = MessageData.formatHash(contentHash);
    final TarArchiveEntry entry = new TarArchiveEntry(hashString);
    entry.setSize(content.length);
    final Date timestamp = msg.getTimestamp();
    if (timestamp != null)
    {
      entry.setModTime(timestamp);
    }
    out.putArchiveEntry(entry);
    out.write(content);
    out.closeArchiveEntry();
  }
}
