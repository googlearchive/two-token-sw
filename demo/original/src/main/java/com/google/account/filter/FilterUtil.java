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

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.account.User;

public class FilterUtil {
  private static final String REQUEST_USER = "user";
  private static final String REQUEST_SESSION_INFO = "session_info";

  public static User getSessionUser(ServletRequest request) {
    return (User) request.getAttribute(REQUEST_USER);
  }

  public static SessionInfo getSessionInfo(ServletRequest request) {
    return (SessionInfo) request.getAttribute(REQUEST_SESSION_INFO);
  }

  public static boolean hasSessionUser(HttpServletRequest request) {
    return getSessionUser(request) != null;
  }

  public static void setSessionCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      SessionInfo session) {
    SessionUtil.setSessionCookie(response, request.getServerName(), session);
  }

  public static void clearSessionCookie(
      HttpServletRequest request, HttpServletResponse response) {
    SessionUtil.clearSessionCookie(response, request.getServerName());
  }

  public static void refreshSessionCookie(
      HttpServletRequest request, HttpServletResponse response) {
    SessionUtil.refreshSessionCookie(request, response, request.getServerName());
  }

  public static void setSessionInfoInRequestAttributeAfterLogin(
      ServletRequest request, User user, SessionInfo sessionInfo) {
    request.setAttribute(REQUEST_USER, user);
    request.setAttribute(REQUEST_SESSION_INFO, sessionInfo);
  }

  public static void clearSessionInfoInAttributeAfterLogout(ServletRequest request) {
    request.removeAttribute(REQUEST_USER);
    request.removeAttribute(REQUEST_SESSION_INFO);
  }
}
