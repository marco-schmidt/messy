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
import messy.msgdata.formats.messy.MessageData;

/**
 * Abstract base class for writing messages into tar files.
 *
 * @author Marco Schmidt
 */
public abstract class AbstractTarMessageSink implements AutoCloseable
{
  /**
   * The default value for the maximum size of a single tar file.
   */
  public static final long DEFAULT_MAX_FILE_SIZE = 1000000000L;
  /**
   * The minimum value for the maximum size of a single tar file.
   */
  public static final long MINIMUM_MAX_FILE_SIZE = 1000000L;
  private final long maxFileSize;

  public AbstractTarMessageSink(long maxFileSize)
  {
    if (maxFileSize < MINIMUM_MAX_FILE_SIZE)
    {
      throw new IllegalArgumentException(
          "Minimum max file size must be at least " + MINIMUM_MAX_FILE_SIZE + ", got " + maxFileSize);
    }
    this.maxFileSize = maxFileSize;
  }

  @Override
  public abstract void close() throws IOException;

  public long getMaxFileSize()
  {
    return maxFileSize;
  }

  public abstract void put(MessageData msg) throws IOException;
}
