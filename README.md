OMiN - an Opportunistic Microblogging Network
----------------------
OMiN is a pocket switched network running on smartphones and tablets. It allows users to send and receive messages without using any global infrastructure such as the internet. Messages are stored by devices and forwarded to more devices using Bluetooth.

# Project Layout
The project is split into three different modules, each build using Gradle:
- The app module contains the Android app to be installed on every node.
- The pkg module is the central authentication server, which runs on the school’s host server via CGI.
- The crypto module is a library of cryptography functions used by both the app and authentication server.

# Building
Executing the following command from the project directory will build everything, downloading libraries, build scripts and the Android SDK if necessary:
``` bash
./gradlew build
```
The binaries will now be in the following locations:
- The main app will be located at app/build/outputs/apk/app-debug.apk
- The cryptography library will be at crypto/build/libs/crypto.jar
- The authentication server will be at pkg/build/libs/pkg.jar and can be executed using the CGI script at pkg/omin.cgi

# Installation
## Authentication Server
To run the authentication server, configure a web server to run the omin.cgi script in the pkg directory. The Android app will have to be modified to use the new server location and master public key. The server stores private information such as the master keys in the working directory, so it is essential that the web server cannot serve these files (e.g. by creating a separate CGI script in the public directory of the web server to call the authentication script in a non-public directory).

## Android App
The app can be installed by executing the following command:
``` bash
./gradlew installDebug
```
