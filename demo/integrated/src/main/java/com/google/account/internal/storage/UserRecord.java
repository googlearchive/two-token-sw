// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package com.google.account.internal.storage;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * The persistence layer representation for a user.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
class UserRecord {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  private String email;

  @Persistent
  private String displayName;

  @Persistent
  private String password;

  @Persistent
  private String photoUrl;

  @Persistent
  private boolean tosAccepted;

  @Persistent
  private long lastBigChangeTime;

  public long getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }

  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
  }

  public boolean isTosAccepted() {
    return tosAccepted;
  }

  public void setTosAccepted(boolean tosAccepted) {
    this.tosAccepted = tosAccepted;
  }

  public long getLastBigChangeTime() {
    return lastBigChangeTime;
  }

  public void setLastBigChangeTime(long lastBigChangeTime) {
    this.lastBigChangeTime = lastBigChangeTime;
  }
}
