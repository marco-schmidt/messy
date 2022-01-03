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
 * Data class for a single Twitter message, called status in Twitter's API documentation, also known as a tweet.
 *
 * @author Marco Schmidt
 */
public class TwitterStatus
{
  private Date createdAt;
  private boolean delete;
  private BigInteger id;
  private String language;
  private TwitterPlace place;
  private String text;
  private TwitterUser user;

  public Date getCreatedAt()
  {
    return createdAt == null ? null : new Date(createdAt.getTime());
  }

  public void setCreatedAt(Date createdAt)
  {
    this.createdAt = createdAt == null ? null : new Date(createdAt.getTime());
  }

  public boolean isDelete()
  {
    return delete;
  }

  public void setDelete(boolean delete)
  {
    this.delete = delete;
  }

  public BigInteger getId()
  {
    return id;
  }

  public void setId(BigInteger id)
  {
    this.id = id;
  }

  public String getLanguage()
  {
    return language;
  }

  public void setLanguage(String language)
  {
    this.language = language;
  }

  public TwitterPlace getPlace()
  {
    return place;
  }

  public void setPlace(TwitterPlace place)
  {
    this.place = place;
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  public TwitterUser getUser()
  {
    return user;
  }

  public void setUser(TwitterUser user)
  {
    this.user = user;
  }
}
