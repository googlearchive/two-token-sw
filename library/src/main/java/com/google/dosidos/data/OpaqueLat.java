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

package com.google.dosidos.data;

/**
 * The data structure to generate the LAT opaque string.
 */
public class OpaqueLat {

  private String nonce;
  private String sub;
  private String aud;
  private long iat;
  private long exp;

  public OpaqueLat() {
    this(null, null, -1, 0, null);
  }

  public OpaqueLat(String userId, String audience, long issueAt,
      long expiration, String nonce) {
    this.sub = userId;
    this.aud = audience;
    this.iat = issueAt;
    this.exp = expiration;
    this.nonce = nonce;
  }

  public String getUserId() {
    return sub;
  }

  public void setUserId(String userId) {
    this.sub = userId;
  }

  public long getIssueAt() {
    return iat;
  }

  public void setIssueAt(long issueAt) {
    this.iat = issueAt;
  }

  public String getNonce() {
    return nonce;
  }

  public void setNonce(String nonce) {
    this.nonce = nonce;
  }

  public String getAudience() {
    return aud;
  }

  public void setAudience(String audience) {
    this.aud = audience;
  }

  public long getExpiration() {
    return exp;
  }

  public void setExpiration(long expiration) {
    this.exp = expiration;
  }
}
