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

package com.google.account.filter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.util.EncryptionUtil;

/**
 * Utility methods to handle the cookie.
 */
public class SessionUtil {
  private static final String SESSION_COOKIE_NAME = "SID";
  private static final Gson GSON = new Gson();
  public static final int SESSION_LIFETIME = 1 * 60; // in seconds

  public static void setSessionCookie(
      HttpServletResponse response, String domain, SessionInfo session) {
    String value = EncryptionUtil.encrypt(GSON.toJson(session));
    setSessionCookie(
        response, SESSION_COOKIE_NAME, domain, value, SESSION_LIFETIME);
  }

  public static void setSessionCookie(HttpServletResponse response, String cookieName,
      String domain, String cookieValue, int maxAge) {
    Cookie cookie = new Cookie(cookieName, cookieValue);
    cookie.setDomain(domain);
    maxAge = maxAge > 0 ? maxAge : 0;
    cookie.setMaxAge(maxAge);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  public static void clearSessionCookie(HttpServletResponse response, String domain) {
    clearSessionCookie(response, SESSION_COOKIE_NAME, domain);
  }

  public static void clearSessionCookie(HttpServletResponse response, String cookieName,
      String domain) {
    Cookie cookie = new Cookie(cookieName, "");
    cookie.setMaxAge(0);
    cookie.setDomain(domain);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  public static void refreshSessionCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String domain) {
    refreshSessionCookie(
        request, response, SESSION_COOKIE_NAME, domain, SESSION_LIFETIME);
  }

  public static void refreshSessionCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String cookieName,
      String domain,
      int maxAge) {
    Cookie cookie = getSessionCookie(request, cookieName);
    if (cookie != null) {
      cookie.setMaxAge(maxAge);
      cookie.setDomain(domain);
      cookie.setPath("/");
      response.addCookie(cookie);
    }
  }

  public static SessionInfo getSessionInfo(HttpServletRequest request) {
    Cookie cookie = getSessionCookie(request, SESSION_COOKIE_NAME);
    if (cookie == null || Strings.isNullOrEmpty(cookie.getValue())) {
      return null;
    }
    return GSON.fromJson(
        EncryptionUtil.decrypt(cookie.getValue()), SessionInfo.class);
  }

  public static String getSessionCookieValue(
      HttpServletRequest request, String cookieName) {
    Cookie cookie = getSessionCookie(request, cookieName);
    if (cookie != null) {
      return cookie.getValue();
    }
    return null;
  }

  private static Cookie getSessionCookie(
      HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (int i = 0; i < cookies.length; i++) {
        if (cookieName.equals(cookies[i].getName())) {
          return cookies[i];
        }
      }
    }
    return null;
  }
}
