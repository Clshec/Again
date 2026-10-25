# HandyCam Manual — Android debug APK

This is an experimental build of the free open-source HandyCam with manual
ISO and shutter-speed controls for the camera sensor. Built from
TomOdellSheetMusic/HandyCam (GPL-3.0).

- Package ID: `com.gumacu.handycam.manual`
- Variant: debug (development signing key; not production signed)
- SHA-256: `8b7b5cd135e164575510f33d549245a2b62312fdc48eb55d0406fe7c523e9d4e`
- APK: [Download HandyCam Manual](HandyCam-Manual-v1.0-debug.apk)

It installs alongside the original HandyCam. Stop the original stream before
opening this version, so the two apps do not compete for camera access or ports.

**Testing:** Build compiled successfully. Manual ISO/shutter operation and
streaming stability still require testing on the target Android phone.

## Controls

When the camera has MANUAL_SENSOR capability and the stream is active:
- On the phone, use the Auto / Manual button and tap the ISO and shutter presets.
- In the built-in /camera remote web UI, enable Manual and adjust ISO/shutter.
- Switch Manual OFF to restore automatic exposure.

Do not enter credentials into a public demo server; use HandyCam on a trusted
local network. Always keep the original app for rollback.
