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

package com.google.dosidos.data;

/**
 * The data structure to generate the LAT JWT.
 */
public class Lat {

  private String iss;
  private String url;
  private String aud;
  private long exp;
  private String lat;

  public Lat() {
    this(null, null, null, 0, null);
  }

  public Lat(String issuer, String tokenUrl, String audience,
      long expiration, String opaqueLat) {
    this.iss = issuer;
    this.url = tokenUrl;
    this.aud = audience;
    this.exp = expiration;
    this.lat = opaqueLat;
  }

  public String getIssuer() {
    return iss;
  }

  public void setIssuerd(String issuer) {
    this.iss = issuer;
  }

  public String getTokenEndpointUrl() {
    return url;
  }

  public void setTokenEndpointUrl(String tokenEndpointUrl) {
    this.url = tokenEndpointUrl;
  }

  public String getAudience() {
    return aud;
  }

  public void setAudience(String audience) {
    this.aud = audience;
  }

  public long getExpiration() {
    return exp;
  }

  public void setExpiration(long expiration) {
    this.exp = expiration;
  }

  public String getOpaqueLat() {
    return lat;
  }

  public void setOpaqueLat(String opaqueLat) {
    this.lat = opaqueLat;
  }
}
