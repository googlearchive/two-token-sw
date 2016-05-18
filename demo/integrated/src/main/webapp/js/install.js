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
 * @fileoverview Utility method to install service worker in post login page.
 */

/**
 * Installs/Reinstalls the service worker.
 * @param swUrl The URL of the service worker.
 * @param swScope The Scope of the service worker.
 * @param lat The long live auth token.
 * @param satLifetime The life time of the short lived token.
 */
dosidos.installServiceWorker = function(swUrl, swScope, lat, satLifetime) {
  if (!('serviceWorker' in navigator)) {
  	console.log('Browser doesn\'t support service worker');
    return;
  }

  navigator.serviceWorker.register(swUrl, {scope: swScope})
      .then(function(registration) {
  	    console.log('Service Worker is successfully registrated.');
  	    var satExpires = Date.now() + satLifetime * 1000 -
  	        dosidos.settings.TIME_BUFFER_MS;
        var sessionState = {
          'lat': lat,
          'satExpires': satExpires
        };
        dosidos.store.restart(sessionState).then(function() {
        	console.log('Service Worker is restarted.');
        });
      }).catch(function(err) {
  	    console.log('ServiceWorker registration failed: ' + err);
      });
};
