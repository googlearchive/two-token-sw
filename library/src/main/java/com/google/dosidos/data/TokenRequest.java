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
 * A request to the Token Endpoint. Currently we only support refreshing SAT by
 * a LAT. Other refreshing way may be supported by adding a new {@code Action}
 * type and additional parameters.
 */
public class TokenRequest {
  private final Action action;

  /**
   * The LAT will be submitted via HTTP Header.
   */
  private final String lat;

  public TokenRequest() {
    this(Action.END, null);
  }

  public TokenRequest(Action action, String lat) {
    this.action = action;
    this.lat = lat;
  }

  public Action getAction() {
    return action;
  }

  public String getLat() {
    return lat;
  }

  /**
   * The action type of the request.
   */
  public enum Action {

    /*
     * Refreshes SAT by providing a LAT.
     */
    REFRESH_BY_LAT,

    /*
     * Asks server to clear current authenticated web session. To prevent abuse,
     * the server may require a valid LAT in the header.
     */
    END
  }
}
