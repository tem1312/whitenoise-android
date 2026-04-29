# WhiteNoise Android App

WhiteNoise is a relaxing sound generator designed to help users focus, sleep, or unwind. It offers ambient audio loops like waves, birds, cars, and wind, with customizable volume, fade effects, and timers. Users can also track usage history.

## Features
- Ambient audio loops (waves, birds, wind, cars)
- Adjustable volume
- Sleep timer with duration picker
- Fade and wave oscillation effects
- User account system (local)
- Usage tracking (daily session logging)
- Light/dark mode toggle in settings

## APK Installation
You can install and run the generated APK on any Android device (Android 8.0+):

1. Locate the APK (typically in `app/build/outputs/apk/debug/` or `release/` after building in Android Studio).
2. Transfer it to your Android device (via USB, AirDrop, email, or cloud).
3. On your device, enable "Install unknown apps" for the file manager or browser you're using.
4. Tap the APK to install and run the app.

The app is fully functional offline and requires no internet access. New users can register locally when they first open the app.

## Tech Stack
- Kotlin + Jetpack Compose
- Room DB for local user & usage data
- ExoPlayer for audio playback
- ViewModel & StateFlow for UI state
- Material Design 3
- SharedPreferences for persistent settings

## Building and Running
To build and run the project:

1. Ensure you have Android Studio installed.
2. Open the project in Android Studio.
3. Sync the Gradle files.
4. Build the APK using the Build menu.
5. Run on an emulator or connected device.
