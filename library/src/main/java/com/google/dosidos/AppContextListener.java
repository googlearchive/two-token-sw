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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Initializes {@code TwoTokenManager} when the web application starts.
 */
@WebListener
public abstract class AppContextListener implements ServletContextListener {
  public static final String ATTR_TWO_TOKEN_MANAGER = "TwoTokenManager";

  @Override
  public void contextDestroyed(ServletContextEvent event) {
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    ServletContext ctx = event.getServletContext();
    ctx.setAttribute(ATTR_TWO_TOKEN_MANAGER, constructTwoTokenManager());
  }

  /**
   * Constructs an application-specific TwoTokenManager instance.
   */
  public abstract TwoTokenManager constructTwoTokenManager();
}
