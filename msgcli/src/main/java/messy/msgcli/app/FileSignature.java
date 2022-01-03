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
package messy.msgcli.app;

/**
 * Data class to store properties of a file format.
 *
 * @author Marco Schmidt
 */
public class FileSignature
{
  private FileFormatHelper.FileType fileType;
  private int offset;
  private byte[] signature;

  public FileSignature(byte[] magicBytes, int offset, FileFormatHelper.FileType type)
  {
    signature = new byte[magicBytes.length];
    System.arraycopy(magicBytes, 0, signature, 0, magicBytes.length);
    setSignature(magicBytes);
    setFileType(type);
    setOffset(offset);
  }

  public FileFormatHelper.FileType getFileType()
  {
    return fileType;
  }

  public int getOffset()
  {
    return offset;
  }

  /**
   * Does argument buffer match this signature?
   *
   * @param buffer
   *          checks whether this array is a match for this signature
   * @param bufferSize
   *          number of bytes in array filled with data
   * @return whether the argument matches this signature
   */
  public boolean matches(byte[] buffer, int bufferSize)
  {
    int bufferIndex = getOffset();
    if (bufferIndex + signature.length > bufferSize)
    {
      // not enough data in buffer to possibly match
      return false;
    }
    int signIndex = 0;
    final int lastIndex = signIndex + signature.length;
    do
    {
      if (buffer[bufferIndex++] != signature[signIndex++])
      {
        return false;
      }
    }
    while (signIndex != lastIndex);
    return true;
  }

  public void setFileType(FileFormatHelper.FileType fileType)
  {
    this.fileType = fileType;
  }

  public void setOffset(int offset)
  {
    this.offset = offset;
  }

  public byte[] getSignature()
  {
    return signature;
  }

  public void setSignature(byte[] signature)
  {
    this.signature = signature;
  }
}
