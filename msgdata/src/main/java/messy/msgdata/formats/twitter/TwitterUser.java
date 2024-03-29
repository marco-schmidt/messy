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
package messy.msgdata.formats.twitter;

import java.math.BigInteger;
import java.util.Date;

/**
 * Data class for a Twitter account representing a person or institution.
 *
 * @author Marco Schmidt
 */
public class TwitterUser
{
  private Date createdAt;
  private BigInteger id;
  private String screenName;
  private boolean verified;

  public Date getCreatedAt()
  {
    return createdAt == null ? null : new Date(createdAt.getTime());
  }

  public void setCreatedAt(Date createdAt)
  {
    this.createdAt = createdAt == null ? null : new Date(createdAt.getTime());
  }

  public BigInteger getId()
  {
    return id;
  }

  public void setId(BigInteger id)
  {
    this.id = id;
  }

  public String getScreenName()
  {
    return screenName;
  }

  public void setScreenName(String screenName)
  {
    this.screenName = screenName;
  }

  public boolean isVerified()
  {
    return verified;
  }

  public void setVerified(boolean verified)
  {
    this.verified = verified;
  }
}
