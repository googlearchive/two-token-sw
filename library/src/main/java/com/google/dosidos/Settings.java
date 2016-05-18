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

package com.google.dosidos;

import com.google.common.base.Strings;

/**
 * Holds the configuration information.
 */
public class Settings {
  private final static String DEFAULT_LAT_HEADER_NAME = "X-LAT";

  private final String aesKey;
  private final String latHeaderName;
  private final long latLifetime;

  private Settings(
      String latHeaderName,
      long latLifetime,
      String aesKey) {
    this.latHeaderName = latHeaderName;
    this.latLifetime = latLifetime;
    this.aesKey = aesKey;
  }

  public String getLatHeaderName() {
    return latHeaderName;
  }

  public long getLatLifetime() {
    return latLifetime;
  }

  public String getAesKey() {
    return aesKey;
  }

  public static class Builder {
    private String aesKey;
    private String latHeaderName;
    private long latLifetime;

    public Builder aesKey(String aesKey) {
      this.aesKey = aesKey;
      return this;
    }

    public Builder latHeaderName(String latHeaderName) {
      this.latHeaderName = latHeaderName;
      return this;
    }

    public Builder latLifetime(long latLifetime) {
      this.latLifetime = latLifetime;
      return this;
    }

    public Settings build() {
      if (Strings.isNullOrEmpty(aesKey)) {
        throw new IllegalArgumentException("AES Key cannot be empty.");
      }
      if (latLifetime <= 0) {
        throw new IllegalArgumentException("LAT lifetime must be postive.");
      }
      if (Strings.isNullOrEmpty(latHeaderName)) {
        latHeaderName = DEFAULT_LAT_HEADER_NAME;
      }
      return new Settings(
          latHeaderName,
          latLifetime,
          aesKey);
    }
  }
}
