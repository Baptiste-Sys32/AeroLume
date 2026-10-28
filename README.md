# AeroLume Flight Tracker

Android flight-tracking prototype with a radar-style UI. Kotlin, Jetpack Compose, MapLibre. Adaptive layouts, search, flight detail screens, and an inspector panel.

Runs on bundled demo data only. There is no live flight API wired in: `FakeFlightDataRepository` and `DemoFlightCatalog` feed the UI.

**Status: abandoned for now. Might pick it back up when I find the willpower.**

## Build

```bash
./gradlew assembleDebug
```

Needs the Android SDK. `local.properties` is git-ignored, point `sdk.dir` at your install.

## License

This project is licensed under the MIT License. See the [LICENSE.md](LICENSE.md) file for details.
