ogame-android-app
=================

Android application for the Ogame web browser game.

To do list and notes
====================

- When a client visits http://ogame.org/, there are a bunch of cookies set in a series of redirections. Thus, we can not automatically follow redirections or else the cookie data is lost (partially no thanks to Java having a bugged implementation of HttpCookies.parse()). For every HTTP response, we must extract any Set-cookie headers and save those cookies. The first cookie that is set is an SID cookie (probably stands for "Session ID"). We then follow a redirection, and then another HTTP response will contain a "deviceID" cookie. The key is that we do set the "follow redirection" property in HttpUrlConnection to false. The lesson here is to turn automatic redirection off. Fortunately, we do not need to visit http://ogame.org/ at all, because those cookies are not what logs us into the game.

- When we send the kid, universe, username, and pass parameters to the login endpoint, we receive as a response an "OG_lastServer" cookie. We do not need this, but we will manually follow the redirection. The Location response header also contains the URL which encodes the cookie data as well as parameters for the second HTTP request, which will be a GET request.

- In the second HTTP response, we receive 4 cookies: PHPSESSID, prsess_188304, login_188304, and language. We only need to hold on to PHPSESSID, prsess_188304, and login_188304. We then follow a redirect for a 3rd HTTP request and send these 3 cookies with our request. This should lead us to the overview screen, which we can then parse and extract out all the good fleet details. These cookies' data are also stored in the Location response header. Interestingly, the 4 cookies are set in a single Set-Cookie header. These cookies are automatically separated for us, but the job to parse the cookies and store them as HttpCookie objects falls on us (no thanks to HttpCookie.parse()'s buggy implementation).

- The 3rd HTTP request is a GET request, which nets us our goal: the overview page.
