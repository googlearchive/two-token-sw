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

import java.io.Serializable;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * This class wraps server side authenticated web session status. It can be
 * piggybacked in a normal HTTP response, so as to sync web session status to
 * the client side. This could happen in below two cases:
 * <ol>
 * <li>After a successful log in, pass the new session info to client side.</li>
 * <li>After a log out, ask the client side to clear saved auth tokens.</li>
 * </ol> 
 */
public class SessionState implements Serializable {
  private static final long serialVersionUID = 1L;

  private final SessionStateChangeType sessionStateChangeType;

  /**
   * In the JWT format, so as to be consistent with federated signon case.
   */
  private final String latJwt;

  /*
   * The life time of the SAT in milliseconds. Client (service worker) needs to
   * refresh the SAT before it expires. <p> A service worker cannot get the
   * expiration time from the session Cookie, since document.cookie doesn't
   * return that information. As a result, the SAT expiration time must be
   * included in the response. </p>
   */
  private final long satLifetime;

  public SessionState() {
    this(SessionStateChangeType.END);
  }

  public SessionState(SessionStateChangeType sessionStateChangeType) {
    this(sessionStateChangeType, null, -1);
  }

  public SessionState(
      SessionStateChangeType sessionStateChangeType,
      @Nullable String latJwt,
      long satLifetime) {
    this.sessionStateChangeType = sessionStateChangeType;
    if (sessionStateChangeType == SessionStateChangeType.NEW) {
      // LAT and SAT expiration time must be set for a NEW session state change.
      Preconditions.checkArgument(!Strings.isNullOrEmpty(latJwt));
      Preconditions.checkArgument(satLifetime > 0);
      this.latJwt = latJwt;
      this.satLifetime = satLifetime;
    } else {
      Preconditions.checkArgument(Strings.isNullOrEmpty(latJwt));
      Preconditions.checkArgument(satLifetime <= 0);
      this.latJwt = null;
      this.satLifetime = -1;
    }
  }

  public SessionStateChangeType getSessionStateChangeType() {
    return sessionStateChangeType;
  }

  public String getLatJwt() {
    return latJwt;
  }

  public long getSatLifetime() {
    return satLifetime;
  }

  /**
   * Enumerates session state change types.
   */
  public enum SessionStateChangeType {

    /**
     * A new authenticated web session is just created. To be used after a
     * successful log in.
     */
    NEW,

    /**
     * The authenticated web session is ended. To be used after server side log
     * out.
     */
    END
  }
}
