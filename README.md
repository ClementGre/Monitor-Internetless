[![Release](https://img.shields.io/github/v/release/clementgre/Monitor-Internetless?label=Download%20version)](https://github.com/clementgre/Monitor-Internetless/releases/latest)

## Overview

This application allows you to control your phone from another phone without any internet connection.
The data is transmitted by SMS which allows users to find their lost phone even if the phone has no internet connection.

## Features list
### SMS Commands
- ``!info``: Get information about the phone (battery level, last position, etc.)
- ``!locate``: Request a new GPS location of the phone (not using Google Play Services)
- ``!ring <duration>``: Make the phone ring for <duration> seconds.

It is impossible to activate GPS or mobile data without user interaction in recent versions of Android (not rooted) then Monitor Internetless does not support turning on/off the GPS or the mobile datas.

### Commands access
- **Password:** A password can be set to prevent anyone from sending commands to the phone.
- **Authorized numbers:** A list of numbers can be set to allow only these numbers to send commands to the phone.


## Preview

<img src="https://raw.githubusercontent.com/ClementGre/Monitor-Internetless/master/preview/preview1.png" width="300"/> <img src="https://raw.githubusercontent.com/ClementGre/Monitor-Internetless/master/preview/preview2.png" width="300"/>

<img src="https://raw.githubusercontent.com/ClementGre/Monitor-Internetless/master/preview/logo_semi_rounded.png" width="100"/>

## Contributing

The app can be translated on Weblate:
[https://weblate.pdf4teachers.org/projects/monitor-internetless](https://weblate.clgr.io/projects/monitor-internetless)




