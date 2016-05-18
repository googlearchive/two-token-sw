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

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.dosidos.data.OpaqueLat;
import com.google.dosidos.data.SessionState;
import com.google.dosidos.data.SessionState.SessionStateChangeType;
import com.google.dosidos.util.EncryptionUtil;
import com.google.gson.Gson;

/**
 * Allows client to inject its own implementation for below functionalities.
 */
public class TwoTokenManager {
  private static final Gson GSON = new Gson();
  private static final Logger log = Logger.getLogger(
      TwoTokenManager.class.getName());
  private static Random random = new Random();

  private final Settings settings;
  private final TwoTokenHandler handler;
  private final LaunchHook hook;

  public TwoTokenManager(
      Settings settings, TwoTokenHandler handler, LaunchHook hook) {
    this.settings = settings;
    this.handler = handler;
    this.hook = hook;
  }

  public boolean isEnabled(String userAgent, @Nullable String userId) {
    return !Strings.isNullOrEmpty(userId) &&
        hook.isEnabledForUserAgent(userAgent) &&
        hook.isEnabledForUser(userId);
  }

  public boolean isEnabledForUserAgent(String userAgent) {
    return hook.isEnabledForUserAgent(userAgent);
  }

  public String parseLatHeader(HttpServletRequest req) {
    return req.getHeader(settings.getLatHeaderName());
  }

  public OpaqueLat parseLat(String lat) {
    try {
      OpaqueLat opaqueLat = GSON.fromJson(
          EncryptionUtil.decrypt(lat, settings.getAesKey()),
          OpaqueLat.class);
      return opaqueLat;
    } catch (RuntimeException e) {
      log.log(Level.SEVERE, "Error occurs when parsing LAT.", e);
    }
    return null;
  }

  public boolean hasBigAccountChangeSince(String userId, long issueAt) {
    return handler.hasBigAccountChangeSince(userId, issueAt);
  }

  public long issueNewSessionCookie(
      HttpServletRequest req, HttpServletResponse resp, String userId) {
    return handler.issueNewSessionCookie(req, resp, userId);
  }

  public SessionState generateSessionStateOnLogin(String userId,
      String audience, long satLifetime) {
    long expiration = System.currentTimeMillis()
        + settings.getLatLifetime() * 1000L;
    OpaqueLat lat = new OpaqueLat(
        userId,
        audience,
        System.currentTimeMillis(),
        expiration,
        String.valueOf(random.nextLong()));
    return new SessionState(
        SessionStateChangeType.NEW,
        EncryptionUtil.encrypt(GSON.toJson(lat), settings.getAesKey()),
        satLifetime);
  }

  public void clearSessionCookie(
      HttpServletRequest req, HttpServletResponse resp) {
    handler.clearSessionCookie(req, resp);
  }
}
