# Build instructions
This requires SBT 13.x, and the Android SDK, with the android-21 build tools.

To build:
	sbt android:package
To run on an android device:
	sbt android:run

To create an IntelliJ IDEA project:
	sbt gen-idea
