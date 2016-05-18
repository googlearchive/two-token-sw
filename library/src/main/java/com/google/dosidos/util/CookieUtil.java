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

package com.google.dosidos.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility methods to handle the cookie.
 */
public class CookieUtil {
  public static void setSessionCookie(HttpServletResponse response, String cookieName,
      String domain, String cookieValue, int maxAge) {
    Cookie cookie = new Cookie(cookieName, cookieValue);
    cookie.setDomain(domain);
    maxAge = maxAge > 0 ? maxAge : 0;
    cookie.setMaxAge(maxAge);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  public static void clearSessionCookie(HttpServletResponse response, String cookieName,
      String domain) {
    Cookie cookie = new Cookie(cookieName, "");
    cookie.setMaxAge(0);
    cookie.setDomain(domain);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  public static void refreshSessionCookie(HttpServletRequest request, HttpServletResponse response,
      String cookieName, String domain, int maxAge) {
    Cookie cookie = getSessionCookie(request, cookieName);
    if (cookie != null) {
      cookie.setMaxAge(maxAge);
      cookie.setDomain(domain);
      cookie.setPath("/");
      response.addCookie(cookie);
    }
  }

  public static String getSessionCookieValue(HttpServletRequest request, String cookieName) {
    Cookie cookie = getSessionCookie(request, cookieName);
    if (cookie != null) {
      return cookie.getValue();
    }
    return null;
  }

  private static Cookie getSessionCookie(HttpServletRequest request, String cookieName) {
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
