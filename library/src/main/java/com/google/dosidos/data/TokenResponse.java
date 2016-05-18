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

import javax.annotation.Nullable;

/**
 * The response from Token Endpoint.
 */
public class TokenResponse {
  private final Result result;
  private final long satLifetime; // in seconds
  private final String error;

  public TokenResponse() {
    this(Result.ERROR, -1, null);
  }

  public TokenResponse(String error) {
    this(Result.ERROR, -1, error);
  }

  public TokenResponse(long satLifetime) {
    this(Result.REFRESHED, satLifetime, null);
  }

  public TokenResponse(
      Result result, long satLifetime, @Nullable String error) {
    this.result = result;
    this.satLifetime = satLifetime;
    this.error = error;
  }

  public Result getResult() {
    return result;
  }

  public long getSatLifetime() {
    return satLifetime;
  }

  public String getError() {
    return error;
  }

  /**
   * The action type of the request.
   */
  public enum Result {

    /*
     * The refresh request has been processed successfully.
     */
    REFRESHED,

    /*
     * Error returned. Whether client should retry is undefined for now.
     */
    ERROR,

    /*
     * The server asks the client the clear current LAT.
     */
    END
  }
}
