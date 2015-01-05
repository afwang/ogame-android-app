ogame-android-app
=================

Android application for the Ogame web browser game.

App's desired design and goals
==============================

In the long term, the app will be a good enough stand-in for playing Ogame without having
to rely on a browser (especially helpful since browsers on Android eat up a lot of memory,
terrible for low to mid-level devices). The app must also be able to allow a user to
switch between multiple universes.

As a first major goal, the app should not provide user
tools that are not part of the game without add-ons or scripts (for example, the app should
not offer the user simulators or calculators that are not already part of the game).

As the first minor goal, the app should be able to acquire fleet movement data
(origin planet name, origin planet coordinates, destination name, destination coordinates,
fleet composition, resource composition, time of arrival) and then present a list of
these fleet events on screen. This is pretty simple, but that means it can be achieved
quickly. This app will be released once it can get to this stage because that
is when it can become useful for players (being able to check up on your fleets
quickly and simply is a valuable tool). This will also require lots of testing and
data with various missions (for example, I don't have RIPs, so I will need some players
to send RIPs to each other to harvest the data in the HTML responses).

The app's design involves 1 Activity that will be binding to a Service. The Service will
handle all the networking with the servers and parse the responses into nice data which
will be returned to the Activity for updating the UI. The main reason behind this is that
I want to separate the task of acquiring data from the task of updating the UI.

To do list
==========

- Add thank you page of some sort to thank makers of images used for icon, from
https://openclipart.org/detail/121903/full-moon-by-merlin2525, and
https://openclipart.org/detail/189057/android-another-by-roshellin-189057

- Add in an indicator for number of unread messages. This should eventually
evolve into another activity that presents messages using DialogFragments

- Add remaining number of missions to the IntegerMissionMap class.

- Fix potential bugs caused by change in screen orientation during loading
(e.g. login fragment looks terrible in landscape mode)

- Extract names for planets. Add names to the TextViews on overview.

- Add color to missions based on mission type, return status, and hostile/friendly status
