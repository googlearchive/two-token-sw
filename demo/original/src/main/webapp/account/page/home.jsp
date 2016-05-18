/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

<%@ page language="java" contentType="text/html; charset=iso-8859-1" %>
<%@ page import="com.google.config.UrlConfig" %>
<%@ page import="com.google.util.JspUtil" %>
<%@ page import="com.google.account.filter.FilterUtil" %>
<%@ page import="com.google.account.User" %>
<%
  User user = FilterUtil.getSessionUser(request);
%>
<html>
<head>
<title>Home Page</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<link rel="stylesheet" type="text/css" href="/account/css/ui.css" />
</head>
<body>
<div id="reg_box">
  <div id="reg_area">
    <div style="text-align: left; font-size: 15px; color: #1C94C4; font-weight: bold; padding: 8px;">
      Welcome to <%= request.getServerName() %>, <%= user.getDisplayName() %>!
    </div>
    <div style="text-align: left; font-size: 13px; color: #999; padding: 0px 8px 20px 20px;">
      Below are your profile information:
    </div>
    <table cellpadding="1" cellspacing="0">
      <tbody>
        <tr style="vertical-align: top;">
          <td class="label"><label for="email">Your Email:</label></td>
          <td class="label">
            <input id="email" name="email" value='<%= user.getEmail() %>' type="text" readonly>
          </td>
        </tr>
        <tr style="vertical-align: top;">
          <td class="label"><label for="displayName">Display Name:</label></td>
          <td>
            <input id="displayName" name="displayName" value='<%= user.getDisplayName() %>' type="text" readonly>
          </td>
        </tr>
        <tr style="vertical-align: top;">
          <td class="label"><label for="photoUrl">Photo URL:</label></td>
          <td>
            <input id="photoUrl" name="photoUrl" value='<%= user.getPhotoUrl() %>' type="text" readonly>
          </td>
        </tr>
      </tbody>
    </table>
    <p>
    <table><tr><td>
    <table cellspacing="0" cellpadding="0" border="0" class="widget-button">
      <tbody>
        <tr>
          <td class="widget-button-left"></td>
          <td class="widget-button-middle">
            <a class="widget-button-link" href="<%=UrlConfig.HOME_PAGE%>">Refresh Page</a>
          </td>
          <td class="widget-button-right"></td>
        </tr>
      </tbody>
    </table>
    </td>
    <td style="font-size: 13px; color: #999;">
      Or <a href="<%=UrlConfig.LOGOUT_ACTION%>" style="font-size: 13px; color: #1C94C4;">Logout</a>
    </td>
    </tr></table>
    </p>
  </div>
  </form>
</div>
</body>
</html>
