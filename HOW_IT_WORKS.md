# Improve Web Session Management by leveraging Service Worker

## Overview

In the document, we will discuss some best practices to improve web session
management by leveraging [Service
Worker](https://www.w3.org/TR/service-workers/).

This doc only cover the non-federated account case, please refer to
[HOW_IT_WORKS_FD](/HOW_IT_WORKS_FD.md) for the federated account case.

### Objectives

Helping web applications to safely have long-lived sessions like mobile apps do,
but still handle big-account-changes on other devices.

### Problems to Solve

Nowadays session cookie is widely used to track authenticated HTTP session,
although it has some very serious pitfalls, which are discussed in [[RFC
6265](https://tools.ietf.org/html/rfc6265#section-8)].

Also, if compared to OAuth 2.0 [[RFC
6749](https://tools.ietf.org/html/rfc6749)], session cookie has some
disadvantages on security and end user experience.

1. **Vulnerable to leak**: The same cookie value will be transferred on every
  HTTP request to access the resources, which makes it vulnerable to leak.

1. **Bad UX caused by short session lifetime**: to mitigate the leakage issue,
  short cookie lifetime is recommended, which leads to bad user experience. Web
  session timeout frequently. As a result, user will be asked to log in again
  and again.

  Take mobile phone as an example. An OAuth 2.0 based native app can save a
  refresh token after user's first login, then use that refresh token to
  exchange an access token for web access. Thus the user is stayed in signed-in
  status, unless the refresh token is revoked.

  As for mobile web (even in the same phone as above), the web session will
  timeout soon, and user will be asked to log in. This places web application at
  a disadvantage versus other technology stacks.

1. **Unlimited attack window for leaked cookies**: session cookie expiration
  time will be extended on each HTTP request, which means a leaked session
  cookie can be used for a long time if attacker keeps refreshing it before
  timeout.

1. **No revocation/invalidation mechanics** (for a leaked/stolen cookie): There
  is no easy way to invalidate a session cookie based on current HTTP session
  architecture.

  Suppose a session cookie is stolen by a bad guy from a user (good guy).
  Basically nothing can be done by the good guy to stop the bad guy from using
  the stolen session cookie.

   * The good guy logs out. What logging-out does is: clearing session cookie
     from the good guy's user agent. The session cookie is still valid. Bad guy
     can still use it from other user agent.

   * The good guy changes password. Normally the cookie value has no
     relationship to the the version of password. Even if it does, it is almost
     impossible to verify the password version on each incoming HTTP request.
     Thus, bad guy can still use the stolen cookie for as long as the session
     lifetime period used by the same. But the shorter that is, the worse the
     user experience is for the good user (see #2).

  What good guy needed is a way to invalidate all cookies issued before, and
  thus block any HTTP request to use those tokens from whatever user agent.
  Apparently, there is no such mechanic available now based on current HTTP
  session management architecture.

## Terminology

**Big Account Change**

  Literally, it means user make a big change to his account. For example, user
  changes his password. In essence, it is just a signal to indicate that user
  want to invalidate all web sessions he approved before that point of time.

**Cookie**

  a small piece of data sent from a website and stored in the user's web browser
  while the user is browsing. Specification:
  [https://tools.ietf.org/html/rfc6749](https://tools.ietf.org/html/rfc6749)

**OAuth 2.0**

  a framework enables a third-party application to obtain limited access to an
  HTTP service. Specification:
  [https://tools.ietf.org/html/rfc6265](https://tools.ietf.org/html/rfc6265)

**JWT (JSON Web Token)**

  a compact, URL-safe means of representing claims to be transferred between two
  parties. Specification:
  [https://tools.ietf.org/html/rfc7519](https://tools.ietf.org/html/rfc7519)

**Service Worker**

  a generic entry point for event-driven background processing in the Web
  Platform. Specification:
  [https://www.w3.org/TR/service-workers/](https://www.w3.org/TR/service-workers/)

**Session Cookie**

  the cookie that is used to track authenticated web session. In this docs, we
  may use Cookie as a short name for Session Cookie.

## Architecture
### Two-token based Web Session Management Model

From a more abstract level, current session cookie based web session management
can be referred as **one-token model**, since only one token (namely, the
session cookie value) is used.
In one-token model, the same token is used for two purposes:

  1. as the proof of the approval from end user;
  2. as the credential whenever accessing web resources.

The mixing of these two purposes is the root cause for most issues of current
web session management:

  1. If increase session cookie lifetime, the possibility of leakage is
  increased.
  2. If decrease the lifetime, user needs to sign in more frequently.
  3. To extend the lifetime of the token, the same token itself is used as the
  proof of user's approval.
  4. No standard way defined to validate the cookie periodically. Normal cookie
  validation (on each HTTP request) only verify the cookie is not faked and not
  expired. The cost to check big-account-change on each HTTP request is too
  high. That kind of checking must be done in a periodical way, which is
  apparently missing in current one-token model.

Instead of making incremental changes to fix one or more issues listed above, we
want to introduce the two-token web session management model into normal web
browsers. The **two-token model** is basically an abstraction of OAuth 2.0
session management, which is already widely used by thousands of OAuth 2.0
mobile apps. Thanks to Service Worker technology, now it is possible to support
two-token model in normal web browsers.
In two-token model,  two auth tokens are used to manage web session. The two
tokens have different purposes and expiration times.

* **Long-lived Auth Token** (LAT) represents user's approval. It has a long or
 infinite lifetime. Similar to OAuth 2.0 refresh token, LAT is saved in the
 user agent, and used to get a short lived auth token periodically.
* **Short-lived Auth Token** (SAT) is used to access web resources. It has a
 short expiration time. A new SAT needs to be issued if the old one is
 expiring.

Two-Token model brings flexibility, security, and better user experience to HTTP
session management in normal web browsers.

* LAT keeps user's approval in the user agent for a long time (say, as long as
 the site's mobile app would keep the equivalent approval), unless user
 explicitly or implicitly revokes/invalidates it. User needn't to sign in again
 and again during that period.
* SAT has short expiration time, which decreases the possibility of leakage.
* If SAT is expiring, a new SAT needs to be issued by accessing the Token
 Endpoint. To issue a new SAT, LAT is presented directly or indirectly, as the
 proof of user's approval.
* LAT can only be used for refreshing SAT, and must not for accessing web
 resources. When refreshing a new SAT, the LAT itself or HMAC (signed by the
 LAT) need to be presented to verify if the user agent own that LAT. At minimum
 the site should make sure the LAT was issued AFTER the last big-account
 change, but if it optionally stores a list of valid LATs then it can compare
 the LAT that was sent to that list. Additional security checks can be done
 when refreshing SAT.

With the Service Worker technology, the above mentioned two-token logics can be
added easily to an existing website, and no changes are needed for most web
pages and JavaScript codes.

### Life of Auth Tokens

1. A new LAT is generated on a successful login. Normally, a LAT is a stateless
  (encrypted) token, which contains all information by itself. (See [stateful
  LAT section](#stateful-lat) for alternative design.)

  A LAT must record its issuance time.

1. The LAT will be passed back and stored in the user agent, so that the session
  management service worker (hereinafter referred to as SMSW) to work.
   * The login response page should always install the SMSW.
   * A SAT also created and passed back (to save one HTTP round trip) in the
     successful login response.
1. When accessing web resources, to be consistent across browsers (with and
  without service worker support), SAT should be included in session cookie.
1. A new SAT will be generated if current one is expired.
   * The SMSW will intercept all web requests.
   * If current SAT is expired, SMSW will connect to Token Endpoint to get a
     new SAT.
   * After the SAT refreshed, the original web request continues.
1. The LAT and SAT will be cleared on user logs out.
   * Session cookie is cleared.
   * Notify the SMSW to remove stored LAT. Otherwise, a SAT may be created by
     SMSW on next refreshing.

### LAT Format

It is not recommended for a website to define its own formats for LAT. Following
the format defined in this doc would allow the website:

* Easily integrating two-token model by leveraging open-sourced libraries, which
 are based on these data structure definitions.
* Compatible with federated signon accounts. More details at
 [HOW_IT_WORKS_FD](/HOW_IT_WORKS_FD.md).


LAT is a stateless [JWT](https://tools.ietf.org/html/rfc7519) token, which
contains all the information about the grant in the payload. Below information
should be included:

* Subject: the user id, as a string.
* Audience: the client origin, or client id for an OAuth 2.0 application.
* Issuance time: the timestamp the LAT is created, as a long value.
* Expiration time: the timestamp the LAT will expire, as a long value.

To be compatible with federation account, below optional fields are added:

* Issuer: an optional identifier for the identifier provider.
* Token Endpoint URL: the URL where to get up-to-date SAT.

LAT is a security-sensitive token. The website and service worker can only pass
it on secure HTTPS domains. In addition, service worker only works on HTTPS
origin.
LAT will use unsecured JWT format, since it is already secured by the HTTPS
layer.  The JOSE Header should be:

```json
{"alg": "none"}
```

Below is an example of decoded LAT token payload.

```json
{
   "iss": "anIDP",                       // Optional IDP name
   "url": "https://example.com/token",   // Token Endpoint URL.
   "aud": "https://example.com",         // Audience
   "exp": 1311451970                     // Optional expiration time
   "lat": "s6BhdRkqt3..."                // an opaque string. See below example.
}
```

LAT JWT defines some information to be consumed by the client. When submitting
refreshing request to the Token Endpoint, only the opaque 'lat' string is
needed.

The 'lat' string is an encrypted string, which contains below information to be
consumed by the Token Endpoint. These information are opaque to the client side
(since they are encrypted).

``` json
{
   "sub": "1234567890",                 // Subject
   "aud": "https://www.example.com",    // Audience
   "iat": 1311280970,                   // Issuance time
   "exp": 1311451970                    // Optional expiration time
   "nonce": "n-0S6_WzA2Mj",             // Random string value
}
```

### SAT Format

Token Endpoint (, and Login Endpoint in some cases) will generate a SAT, then
set it into session cookie directly. When accessing web resource, the SAT will
be included automatically via the session cookie. In summary, SAT is basically
transparent to the service worker.

It should be OK for a website to use its old way to generate the session cookie.
The only difference is: the session cookie has a short and fixed expiration
time. Actually, the back-compatible of the format of session token is an
important feature, which makes [cross-browser
compatible](#cross-browser-support) much easier. See cross-browser support
section for more details.

### Components

Below are the new components:

1. **Session Management Service Worker** (SMSW): is a JavaScript component. It
  will be installed (or reinstalled) after user successfully signed in. Once
  activated, it will intercept all the web requests (for the registered scope),
  and fulfill below tasks:
   * Check SAT expiration time. Refresh SAT from Token Endpoint if expired.
   * When successful, continue original web request.
   * When failed, clear stored LAT.
1. **Token Endpoint**: is a server side endpoint, which will process the SAT
  refreshing request. It will:
   * Get the LAT value from HTTP header (say,  'X-LAT'). We strongly recommend
     to use HTTP header (instead of URL parameter) to pass the LAT, for the
     purpose of preventing XSRF attack.
   * Check if LAT is valid. If not valid, notify the SMSW to clear the stored
     LAT.
   * Issue new SAT in session cookie or response.

Below components need to be changed:

1. **Login Endpoint**.
   * On successful login, generate LAT and SAT;
   * (Optional) Parse user agent string (or allow user to input) for meta
     information; Generates session record, and save to database.
   * Return LAT and SAT in the login response.
   * The response should include JavaScript code to install the SMSW.
1. **Logout Endpoint**.
   * Notify the SMSW to clear the stored LAT (and SAT if header is used).
   * Clear session cookie if SAT is passed by cookie.
   * Optionally remove current session record in the database.

### Cross-Browser Support

How can a website enable two-token model on browsers with service worker support
(say, Chrome and FireFox), but fall back to one-token model on browsers without
service worker support?

In the abstract, the token endpoint can be thought as a different kind of login
endpoint, which re-login user again based on the LAT in the request. The service
worker, together with a valid LAT, will trigger the auto login process silently
and periodically. Thus the token endpoint and the service worker compose a
**silent relogin system**, which enforces the two token model silently.

The silent relogin system overlays on existing system, and requires minimal
changes to be hooked into. The only hook point is at the successful login
response:

1. To enable silent relogin system (two-token model):
   * Generates and returns the LAT to the service worker.
   * Includes JavaScript code to install the service worker.
   * Issue session token with shorter lifetime.
1. To work as before (one-token model):
   * Issue session token with normal lifetime.

Thus, above hook point in the central control point for switching between
one-token and two-token model. By checking the userAgent string, we can enable
two-token model only for browsers with service worker API support.

This hook architecture also helps for the gradual launch of the two-token model.
After the service worker and token endpoint is ready, the two-token model can be
rolled out gradually by enabling the hook by the user agent type and/or
percentage.

## Advanced Features

All advanced feature are **optional**.

### Stateful LAT

Active LATs can be stored in the server side. Each LAT corresponds to an
approved web session. User can browse all active sessions, and invalidate one or
all sessions when needed. This feature bring further advantages:

* Trackable. User can view active sessions at any time.
* Flexible. User can invalidate a single session if he wants to.
* Secure. Identity theft can be found more easily by end user.
* Trust and confidence. This tool allows the session management to be more
 controlled by end user.  End user should be more confident on web security if
 he has the ability to track and invalidate any web session under his name.

To support this feature, below changes are needed:

* On a successful login, create a **Session Record** in the persistence layer on
 the server side. The session record includes LAT and some meta info,  which is
 used to differentiate this session from others when user browses all his
 approved session. For example, user agent, create time, etc.
* Session record is removed on server side.
* Add **Session Management UI** to display approved session records, and
 allow user to invalidate a selected session.
   * Lists approved session records of current user.
   * Invalidate one session by removing it from session record list.
* The Session record (and LAT) can be removed on server side by some other user
 or administrator actions:
   * User makes big account changes, which may trigger predefined policies to
     invalidate all previously approved sessions.
   *  Administrator (or program with administrator permission) can monitor the
     behavior of end users, and invalidate some suspicious sessions.
   * If per-user active session limit is reached, the most oldest unused
     session can be removed.

### OAuth 2.0 Integration

For web sites whose mobile apps already support OAuth 2.0, follow below steps to
do the integration:

1. Register an OAuth client for the website.
1. The Token Endpoint defined in doc will delegate all incoming SAT refreshing
  requests to OAuth 2.0 Token Endpoint, and add below parameters:
   * Client ID
   * Client Security
1. Treat the returned access token as SAT, and returns the expiration time in
  the response.
1. To comply to OAuth 2.0 standard, the service worker can inject HTTP header
  'Authorization: Bearer {token}' when accessing web resource.

With above changes, both web clients and native clients will use the same way to
access your web resources. No session cookies any more.

### Support Credentials Management API by Token Endpoint

In the abstract, the token endpoint can be thought as a silent login system,
which can issue SAT based on some credentials provided by the user agent. See
[token endpoint request](#token-endpoint-request) &
[response](#token-endpoint-response) section for more detail.

Token endpoint can be extended to support more credentials types. For example,
the token endpoint can be easily reused as[ the AJAX login endpoint for the
Credential Management
API](https://www.w3.org/TR/credential-management-1/#examples-password-signin).
To facilitate the credential management API, below changes can be made to the
token endpoint:

* Adding a new Action type. For example, **REFRESH\_BY\_PWD**.
* Parsing username/password from the post body.
* Issuing SAT in session cookie, and returns response.

Advantages:

* Websites are more likely to make the server side changes required for the
 Credentials Management API.
* Optimizes the UX for two-token and credentials-prefill/auto-login as a whole.

## Implementation Details

We will provide [the Dosidos
library](/library/)
to help websites to upgrade to two-token based web session management. In this
section, we provide more details about how the Dosidos library is implemented.

### Session Management Service Worker

A [default implementation of the
SMSW](/library/js/sw.js)
is provided in the dosidos library:

* Once installed, it will be activated without page reloading.
* To mitigate XSRF, LAT is sent in the HTTP header.
* The  SAT expiration time, and LAT value are stored in the indexedDB. On each
 'fetch' event, the service worker will load the values from indexedDB, to
 decide whether SAT should be refreshed or not.
* Below is the data structure used in the indexedDB:

```java
/*
 * Data structure for two-token web session status information.
 * @typedef {{
 *   active: (boolean|undefined),
 *   lat: (string|undefined),
 *   satExpires: (number|undefined)
 * }}
 */
dosidos.SessionState;
```

### Install and update the Service Worker

A [method to install/update the service
worker](/library/js/install.js)
is provided in the dosidos library:

* This method should be only invoked in the response of a successful login.
* To control the behavior of the service worker, it will directly change the
 session state stored in the indexedDB. No communication between the page and
 the service worker is needed.

### Suspend the Service Worker

* The service worker will always intercept 'fetch' event. If there is not LAT
 value, or the active field is set to false, the service worker will do
 nothing. We call the service worker is suspended if it is in this status.
* When user logout, instead of uninstalling the service worker,  the library
 will suspend it. The reason is: suspending/restarting is much lightweight than
 uninstalling/reinstalling.
* To suspend the service worker, just clear the session state stored in the
 indexedDB. Below code can be used:

```java
dosidos.store.clear().then();
```

### Token Endpoint Request

The Dosidos library provides a reference implementation for the token endpoint,
which will accept below request parameters:

* **action**. The action type of the request. Currently only two values are
 supported.
   * **END**: Asks server to clear current authenticated web session. To
     prevent abuse, the server may require a valid LAT in the HTTP header.
   * **REFRESH_BY_LAT** : Refreshes SAT by providing a LAT. The LAT is pass
     in the HTTP header.

  We may support more credentials types in the future by adding more action
  types.

For some actions, the LAT should be provided in the HTTP header.

* **X-LAT**. The dosidos library use the 'X-LAT' header by default. Websites can
 easily change it to other name though.

### Token Endpoint Response

The token endpoint will return below response:

* **result**. The SAT refreshing action result.
   * **REFRESHED**: The refresh request has been processed successfully. A new
     SAT has been issued in the session cookie. The SAT expiration time is
     returned in the response also.
   * **END**: The server asks the client the clear current LAT. The reason is
     given in the error field.
   * **ERROR**: Unexpected error returned. Whether client should retry is
     undefined for now.
* **satLifetime**. The lifetime of the SAT in seconds. Returned only if result
 is REFRESHED.
* **error**. The error message for ERROR or END result.

### Is SAT Json Web Token?

To compatible across one-token (for browser without service worker support) and
two-token (for browser with SW support) models, we don't change the format of
the SAT. Thus, SAT is just the session cookie value the web site previously
used.

## Open Questions

1. How to share the LAT across subdomains, to simplify the deployment tasks?
1. How to work with Google MultiLogin?
