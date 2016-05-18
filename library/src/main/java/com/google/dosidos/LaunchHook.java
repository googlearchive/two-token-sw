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

import javax.annotation.Nullable;

/**
 * A hook to decide if two-token model should be enabled.
 */
public interface LaunchHook {

  /**
   * Decides if two-token model should be enabled for current user agent.
   * 
   * @param userAgent
   *          Current user agent string.
   * @return {@code true} if two-token model should be enabled; Otherwise
   *         {@code false}.
   */
  boolean isEnabledForUserAgent(String userAgent);

  /**
   * Decides if two-token model should be enabled for current user.
   * 
   * @param userId
   *          The id of logged-in user. Use {@code null} if no user logs in.
   * @return {@code true} if two-token model should be enabled; Otherwise
   *         {@code false}.
   */
  boolean isEnabledForUser(@Nullable String userId);
}
