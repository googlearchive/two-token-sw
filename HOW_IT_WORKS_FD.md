# Improve Web Session Management for Federated Accounts

# Overview

Two token model brings better security, user experience and flexibility to web
session management. It can be reused on multiple web scenarios, including
non-federated and federated signon. The non-federated case is discussed in
[HOW_IT_WORKS](/HOW_IT_WORKS.md). In this doc we'll focus on federated signon case.

## Problems to Solve

[OpenID Connect](http://openid.net/specs/openid-connect-core-1_0.html) is most
commonly used protocol for federated signon nowadays. Though, it has some
disadvantages, partly caused by the prevailing of mobile web. On mobile web,
automation becomes increasingly important to user experience.

First of all, it is **authentication-oriented**, which means it basically
focuses on how to present an id token claim from IDP to RP to facilitate the
one-off federated authentication process. On mobile web, these one-off federated
authentication processes should not be repeated again and again, since mobile
device is basically a private device and the IDP/RP dance may cause unacceptable
latency. Instead, after the first time of the federated authentication process,
the federation binding, which keeps track of which IDP account is used for the
RP in this device, should be remembered by the user agent until user explicitly
or implicitly terminate this federation binding. For subsequent recurring
federated authentication processes, the id token of the bound user can be
returned from IDP without any user action. The duration of a federation binding
normally covers multiple RP login sessions. A **federation-binding-oriented**
protocol will focus on how to establish, maintain, and terminate a federation
binding, and how to leverage a federation binding for repeated federated signon
processes.

Secondly, OpenID Connect is tightly coupled with OAuth 2.0, which restricts its
reusability. OpenID Connect is **not self-contained**. For example, to refresh
an expiring id token, OpenID Connect has to turn to OAuth 2.0 for refresh and
access tokens.

Nowadays, the authentication system becomes more and more complex, due to below
reasons:

* Developers should handle authentication requirements on different
 environments, like Android, iOS, mobile Web, and desktop Web.
* Federated signon become increasingly important, which requires developers to
 handle both non-federated and federated signon on all above environments.
* The number of apps used by a normal user on a daily basis have increased very
 fast, which requires the authentication process must be as automatic as
 possible.

A generic and dedicated authentication protocol is deadly needed to capture the
best practices on these dynamic environments, and help developers handle all
these requirements gracefully. Unfortunately, OpenID Connect cannot be that
generic authentication protocol due to a simple fact: OpenID Connect is not
self-contained, it can only works with OAuth 2.0.

Thirdly, building an authentication on top of an authorization protocol causes
lots of **unnecessary complexities and limitations**. OAuth 2.0 is much
heavyweight by design.

* Scope is used for fine-grained access control, which is pointless for an
 authentication system.
* Normally refresh token is stateful (so as to be revoked easily). Some IDPs has
 per-client limit for refresh tokens, which is an unnecessary limitations for
 an authentication system.
* You cannot optimize refresh token for authentication cases, since it is
 defined by OAuth 2.0, an authorization protocol.
* A list of endpoints need to set up to manage the lifecycle of OAuth 2.0
 tokens. On top of it, these endpoints are extremely security sensitive. The
 cost to develop and maintain such a system is too expensive for most websites.

In this doc, we want to define a self-contained federation-binding-oriented
authentication protocol, which can be easily reused by normal websites to manage
web session of federated accounts.

## Objectives

Helping web applications to safely have long-lived sessions like mobile apps do,
but still handle **big-account-changes of federated accounts** on other devices.

# Terminology

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

**OpenID Connect**

  OpenID Connect 1.0 is a simple identity layer on top of the OAuth 2.0
  protocol. Specification:
  [http://openid.net/specs/openid-connect-core-1\_0.html](http://openid.net/specs/openid-connect-core-1_0.html)

**Service Worker**

  a generic entry point for event-driven background processing in the Web
  Platform. Specification:
  [https://www.w3.org/TR/service-workers/](https://www.w3.org/TR/service-workers/)

**Session Cookie**

  the cookie that is used to track authenticated web session. In this docs, we
  may use Cookie as a short name for Session Cookie.

# Architecture
## Two-token Model

See the [definition of two-token
model](/HOW_IT_WORKS.md#two-token-based-web-session-management-model)
on [HOW_IT_WORKS](/HOW_IT_WORKS.md).
Two-token model can be used not only for traditional web session management, but
also for federation binding management. In the latter case, a long-lived auth
token is used to keep track of the federation binding; and a short-lived auth
token is used to facilitate each federated signon process. For the short-lived
auth token, we will reuse the [id
token](http://openid.net/specs/openid-connect-core-1_0.html#IDToken) which is
defined by OpenID Connect.

In this doc, **Federation Binding Token**, a long-lived auth token, is defined
to:

* Keep track of the federation binding relationship.
* Refresh id token silently to facilitate federated signon process.
* Check big-account-change of the bound account remotely.

## Federation Binding Token

[OpenID Connect](http://openid.net/specs/openid-connect-core-1_0.html) is
incomplete from the two-token model point of view, in that it only defines the
short-lived auth token (namely, id token), but fails to defined a long-lived
auth token to refresh id token when it expires (, or for a new federated
signon).  In this doc, we'd propose federation-binding token to manage
federation binding.

Federation-Binding token (, or binding token,  as the short name) represents
user's approval for a federation binding, which grant current RP instance to get
an id token of current federated account, so as to facilitate subsequent
federated signon processes. In practice, instead of going through the OAuth
2.0/OpenID Connect dance, an RP client with a valid binding token can get an
up-to-date id token by accessing an XHR endpoint and presenting the binding
token as a proof of user's approval.

Binding token is a JWT token, which contains all the information about the grant
in the payload. Below is an example of the payload. (For illustration only,
later in this doc, in dedicated sections, we will discuss the format of binding
token.)

```json
{
   "iss": "anIDP",                       // IDP name
   "url": "https://idp.com/token",       // The URL to get ID token.
   "aud": "https://server.example.com",  // Domain or OAuth 2 client ID
   "bid": "s6BhdRkqt3..."                // an opaque string as federation binding ID
}
```

The opaque binding ID is an encrypted string with below information.

```json
{
   "sub": "1234567890",                 // user ID
   "aud": "https://www.example.com",    // Domain or OAuth 2 client ID
   "iat": 1311280970,                   // Federation binding approved time
   "nonce": "n-0S6_WzA2Mj",             // random string value
   "exp": 1311451970                    // optional expiration time
}
```

Binding token has infinite (or long) lifetime until user explicitly or
implicitly terminate the federation binding:

* Binding token will be cleared when user explicitly terminate the federation
 binding from the client. (RP should use 'Disconnect', instead of 'Logout', for
 a federated signed-in user.)
* Big account changes will invalidate all session tokens issued before a point
 of time.

Ideally, the session token should be kept in the RP's client side, and invisible
to RP web server. On the other hand, id token is visible to RP web server by
design.

To get an up-to-date id token,  client can access IDP's Token Endpoint, and
present a binding token. Additions security requirements may be enforced before
a new id token is issued.

* The validity of the binding token.
* no big account changes after the binding token issued.
* (Optional) for a web application, the granting user has an active session on
 the IDP site.

If additional parameter 'check_validity=true' exists in the request, Token
Endpoint will return if the target binding token is still valid. This allows RP
to remotely monitor the federated user's big-account-change. RP is recommended
to do this check periodically, so as to terminate a potential hacked federated
session as early as possible.

(Optional) An advanced IDP can store issued binding tokens in the server side
for tracking and flexible revocation purpose. But this is total optional.
Decided by how RP tracking its web session, binding token can be used in 2
modes:

* RP issues its own session cookie to track authenticated HTTP sessions.
   * Get up-to-date id token on the federated signon process.
   * Check binding token validity periodically.
* RP uses id token to track authenticated HTTP sessions.
   * Refresh id token periodically to keep it up-to-date.

## Why not Refresh Token?

Why introducing a new token, instead of reusing OAuth 2.0 Refresh token?

* Refresh Token is defined by OAuth 2.0. It is optimized for authorization
 cases, instead of federated signon. Is is hard to push OAuth 2.0 to change the
 data structure for the best interest of federated signon.
* Refresh token contains some fields which are pointless for authentication,
 like Scope.
* Normally refresh token is stateful. But we want binding token to be stateless.
* To be self-contained, we don't want to rely on OAuth 2.0.

## Life of a Federation Binding Token

1. A new binding token is generated when a federated signon request is approved.
   * Binding token is a JWT token, which contains all the required information:
     user, audience, etc.
   * Most importantly, it contains the timestamp of the approval.
   * The binding token should be stateless, which means it needn't to be stored
     on the IDP server side.
1. In the federated signon response, the binding token will be passed back and
  stored in the client side. An id token is created and passed back also (to
  save one HTTP round trip).
   * For native app, it is the app's responsibility to store the binding token.
   * For web app, session management service worker should handle this.
1. If RP web site use the id token to track session, the id token will be set to
  the session cookie. When the id token is expiring, the native app or the
  service worker will access IDP's token endpoint to refresh the id token.
1. If RP web site will issue its own session cookie. The returned id token will
  be consumed for that purpose.
1. Suppose RP own session cookie timeout, another federated signon process is
  needed.
   * The client checks if there is a saved binding token. If yes, use that
     binding token to access IDP's token endpoint to refresh the id token. Then
     consume the id token for federated signon.
   * Otherwise, maybe after user clicking on the IDP button, redirect to IDP
     consent page to ask for a federation binding.
   * (Optional) IDP consent page can provide a 'Keep me signed in' checkbox,
     which should be checked by default. If user uncheck it, return an id token
     without binding token. Otherwise go to step 2.
1. The binding token can be invalidated by below user actions:
   * App provide a button/link to allow user 'Disconnect' from the IDP, which ,
     when clicked by user, will clear the saved binding token in the storage.
   * User makes big account changes, which will invalidate all previously
     approved sessions.

## Components

Below are the components:

1. **Session Management Service Worker** (SMSW): is the same JavaScript
  component as we discussed in [HOW_IT_WORKS](HOW_IT_WORKS.md).
   * By unifying the data structure used by binding token and LAT, we hope SMSW
     can handle both non-federated and federated signon cases gracefully.
   * More details to be defined.
1. **Token Endpoint**: is a server side endpoint, which will issue up-to-date id
  tokens. It will:
   * Get the binding token from HTTP authorization header. We strongly
     recommend to use HTTP header (instead of parameter) to pass the binding
     toke, for the purpose to mitigate XSRF.
   * Check if binding toke is valid. If not valid, notify the SMSW to clear the
     stored binding toke.
   * Issue new id token in the response.
1. **Federated Signon Approval Page**.
   * Allow user establish federation bindings; or optionally support one-off
     federated signon by unchecking the 'Keep me signed in' checkbox.
   * Return binding token and id token in the login response.
   * The response should include JavaScript code to install the SMSW.
1. **Change password page.**
   * When password is changed, update the per user 'big account change time',
     to invalidate binding tokens issued before this time.
1. **Disconnect Button/Link** in RP web page; or native app UI.

   * For web app, notify the SMSW to clear the saved binding token.
   * For native app, clear the saved binding token.

## Summary

The result protocol is not that complex, it only add a new endpoints (Approval
page and Token endpoint) to manage the two stateless JWT tokens.

* It is self-contained. The proposed endpoints support the lifecycles of binding
 and id tokens.
* It is a dedicated authentication protocol. All knowledge on
 developing/maintaining such a system relate only to authentication.
* It supports automation, which is increasingly important on mobile web.
