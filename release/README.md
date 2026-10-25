# HandyCam Manual for OBS

Experimental Android builds of HandyCam with true sensor ISO/shutter control and a browser-based camera control panel. Based on TomOdellSheetMusic/HandyCam (GPL-3.0). Full modified source is in this repository.

## Latest v1.1: Browser controls

**[Download v1.1 APK](HandyCam-Manual-v1.1-debug.apk)**

The browser page on http://PHONE_IP:8080/camera now supports:

- ISO numeric input within the active camera range, plus the existing slider.
- Shutter logarithmic slider, precise values such as 1/50, 1/125, 20ms or 0.02s, and quick presets.
- Camera white balance mode selection and optional lock of auto white balance.
- Camera anti-flicker selection Auto / 50Hz / 60Hz / Off, when supported by the active lens.
- Manual focus toggle and distance slider ONLY for H.264/AVC on supported lenses. MJPEG autofocus is unchanged.
- Working AE lock control (separate from manual ISO/Shutter).
- Automatic web status refresh without overwriting the ISO or shutter value while the user is typing it.

Input outside the sensor and FPS constraints is rejected. All changes are performed in the browser, with no new phone control UI.

**Development build:** Compile-tested but needs physical phone testing for the added camera functions. Same Android package ID as v1.0: com.gumacu.handycam.manual. Signed with the same debug keystore, versionCode 2; install directly over the v1.0 debug build.

## Previous v1.0

[Download v1.0 APK](HandyCam-Manual-v1.0-debug.apk) (user-tested sensor manual exposure).

To revert from v1.1 to v1.0, Android will generally require uninstalling v1.1; this can reset the app settings. The original official HandyCam app remains untouched.

## How to use

1. Stop the official HandyCam stream (to avoid camera and port conflicts).
2. Start HandyCam Manual, then connect to OBS as usual.
3. Visit http://PHONE_IP:8080/camera from the trusted local network.
4. Enable Manual and adjust ISO/Shutter. The stream should update without an OBS restart.
5. Test H.264 before expecting the manual focus slider.

**Security:** The browser control runs via plain HTTP on your local network. Do not expose port 8080 to the public Internet.

No extra Android permissions, external dependencies, or web servers were introduced.
