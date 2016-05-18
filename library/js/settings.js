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

/**
 * @fileoverview The token configuration parameters.
 */

/**
 * Name space for configuration parameters.
 */
dosidos.settings = dosidos.settings || {};

/**
 * The URL of the token end point.
 */
dosidos.settings.TOKEN_ENDPOINT_URL = '/token';

/**
 * The HTTP header name for the LAT when accessing token end point.
 */
dosidos.settings.HTTP_HEADER_LAT = 'X-LAT';

/**
 * Sets a shorter life time intentionally to allow time differences between
 * the server and the user agent. In milliseconds.
 */
dosidos.settings.TIME_BUFFER_MS = 10 * 1000; //5 * 60 * 1000;

