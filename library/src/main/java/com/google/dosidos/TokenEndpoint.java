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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.dosidos.data.OpaqueLat;
import com.google.dosidos.data.TokenRequest;
import com.google.dosidos.data.TokenResponse;
import com.google.dosidos.data.TokenRequest.Action;
import com.google.gson.Gson;

@WebServlet
public class TokenEndpoint extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Gson GSON = new Gson();
  private static final Logger log = Logger.getLogger(
      TokenEndpoint.class.getName());

  private TwoTokenManager getTwoTokenManager() {
    return (TwoTokenManager) getServletContext().getAttribute(
        AppContextListener.ATTR_TWO_TOKEN_MANAGER);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    TokenRequest tokenRequest = parseTokenRequest(req, getTwoTokenManager());
    TokenResponse tokenResponse = handleTokenRequest(
        req, resp, tokenRequest, req.getServerName());
    resp.setContentType("application/json");
    PrintWriter out = resp.getWriter();
    String json = GSON.toJson(tokenResponse);
    log.warning("Sends resonson: " + json);
    out.write(json);
  }

  private TokenRequest parseTokenRequest(
      HttpServletRequest req, TwoTokenManager twoTokenManager) {
    String token = twoTokenManager.parseLatHeader(req);
    Action action = null;
    String actionStr = req.getParameter("action");
    if (!Strings.isNullOrEmpty(actionStr)) {
      try {
        action = Action.valueOf(actionStr);
      } catch (IllegalArgumentException e) {
      }
    }
    log.warning("Recieved rquest: " + actionStr + " with token size "
        + (token == null ? 0 : token.length()));
    return new TokenRequest(action, token);
  }

  private TokenResponse handleTokenRequest(
      HttpServletRequest req,
      HttpServletResponse resp,
      TokenRequest tokenRequest,
      String audience) {
    TwoTokenManager twoTokenManager = getTwoTokenManager();
    if (tokenRequest.getAction() == Action.END) {
      // Service worker initiated log out.
      twoTokenManager.clearSessionCookie(req, resp);
      return generateEndResponse("Session ended in response to client request.");
    } else if (tokenRequest.getAction() != Action.REFRESH_BY_LAT) {
      return generateErrorResponse("Only support REFRESH_BY_LAT for now");
    }
    if (Strings.isNullOrEmpty(tokenRequest.getLat())) {
      return generateErrorResponse("Not valid LAT found.");
    }
    OpaqueLat lat = twoTokenManager.parseLat(tokenRequest.getLat());
    if (lat == null) {
      return generateErrorResponse("Invalid LAT.");
    }
    if (!audience.equals(lat.getAudience())) {
      return generateEndResponse("LAT audience is invalid.");
    }
    if (lat.getExpiration() >= 0 &&
        lat.getExpiration() <= System.currentTimeMillis()) {
      return generateEndResponse("Expired LAT.");
    }
    boolean bigAccountChanged;
    try {
      bigAccountChanged = twoTokenManager.hasBigAccountChangeSince(
          lat.getUserId(), lat.getIssueAt());
    } catch (IllegalArgumentException e) {
      return generateErrorResponse("Invalid LAT.");
    }
    if (bigAccountChanged) {
      return generateEndResponse("LAT invalidated due to big account change.");
    }
    // Refresh SAT.
    long satLifetime = twoTokenManager.issueNewSessionCookie(req, resp, lat.getUserId());
    return generateRefreshedResponse(satLifetime);
  }

  private TokenResponse generateErrorResponse(String error) {
    return new TokenResponse(error);
  }

  private TokenResponse generateEndResponse(String reason) {
    return new TokenResponse(TokenResponse.Result.END, -1, reason);
  }

  private TokenResponse generateRefreshedResponse(long satLifetime) {
    return new TokenResponse(satLifetime);
  }
}
