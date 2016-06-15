##Two-Token Based Web Session Management

See [HOW_IT_WORKS](HOW_IT_WORKS.md) for more information.
See the [live demo app](https://ws-codelab.appspot.com).

To deploy your own version web app:

1. Download Apache Maven version 3.1 or greater and then install it.

2. Follow this [tutorial](https://cloud.google.com/appengine/docs/java/gettingstarted/creating) to create a Google App Engine project, and replace the project id in the `<application>` tag in `demo/integrated/src/main/webapp/WEB-INF/appengine-web.xml`.

3. Upload the app to App Engine by running `mvn appengine:update` under `demo/integrated` folder.

4. You can verify the different behaviors in Chrome and FireFox:
   * Long Lived Session:
     * On FireFox,  the session will time out soon (every 1 minute, as we set in SessionUtil.SESSION_LIFETIME).
     * On Chrome, the session is long lived. And in developer console, you can verify the session cookie value changes every minute.
   * Big Account Change:
     * On FireFox,  no effect for the big account change.
     * On Chrome, the session will be terminated by big account change.


__This is not an official Google product.__
