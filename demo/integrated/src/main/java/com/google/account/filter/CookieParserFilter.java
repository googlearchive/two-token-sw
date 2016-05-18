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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.account.User;
import com.google.account.UserManager;

public class CookieParserFilter implements Filter {
  private static final String REQUEST_USER = "user";
  private static final String REQUEST_SESSION_INFO = "session_info";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    SessionInfo sessionInfo = SessionUtil.getSessionInfo(
        (HttpServletRequest) request);
    if (sessionInfo != null && sessionInfo.getExpiresAt() > System.currentTimeMillis()) {
      User user = UserManager.getUser(sessionInfo.getUserId());
      if (user != null) {
        request.setAttribute(REQUEST_USER, user);
        request.setAttribute(REQUEST_SESSION_INFO, sessionInfo);
      }
      String requestUri = ((HttpServletRequest) request).getRequestURI();
      if (!requestUri.contains("logout")) {
        FilterUtil.refreshSessionCookie(
            (HttpServletRequest) request, (HttpServletResponse) response);
      }
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
  }
}
