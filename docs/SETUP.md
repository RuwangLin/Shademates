# Local setup

## Prerequisites

- Android Studio with Jetpack Compose support
- Android SDK 36
- JDK 11
- Android emulator or physical device
- Mapbox account with:
  - a secret downloads token for the Maven repository
  - a public runtime access token for the map
- OpenWeather API key
- a compatible backend base URL and API key for the project-specific endpoints

The original course backend is not included and is not guaranteed to remain online.

## Clone

```powershell
git clone https://github.com/RuwangLin/Shademates.git
cd Shademates
```

## Configure credentials

Do not add credentials to this repository.

Store them in your user-level Gradle properties:

- Windows: `C:\Users\<you>\.gradle\gradle.properties`
- macOS/Linux: `~/.gradle/gradle.properties`

```properties
MAPBOX_DOWNLOADS_TOKEN=your_secret_downloads_token
MAPBOX_ACCESS_TOKEN=your_public_runtime_token
OPENWEATHER_API_KEY=your_openweather_key
UV_TIPS_API_KEY=your_backend_api_key
BACKEND_BASE_URL=https://your-backend.example.com/
```

Notes:

- `MAPBOX_DOWNLOADS_TOKEN` is used only to download Mapbox Android dependencies.
- `MAPBOX_ACCESS_TOKEN` is compiled into the Android resources; restrict it to the intended Android app/package in Mapbox.
- `BACKEND_BASE_URL` must be an absolute HTTPS URL ending in `/` because Retrofit requires a trailing slash.
- Upstream private keys should ideally stay behind a backend in any production architecture.

## Sync and run

1. Open the repository root in Android Studio.
2. Allow Gradle to sync.
3. Choose an emulator or connected Android device.
4. Run the `app` configuration.

PowerShell build:

```powershell
.\gradlew.bat assembleDebug
```

JVM unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

Instrumented tests:

```powershell
.\gradlew.bat connectedAndroidTest
```

## Configuration implementation

The app module reads non-repository Gradle properties and generates:

- `R.string.mapbox_access_token`
- `BuildConfig.OPENWEATHER_API_KEY`
- `BuildConfig.UV_TIPS_API_KEY`
- `BuildConfig.BACKEND_BASE_URL`

`settings.gradle.kts` separately reads `MAPBOX_DOWNLOADS_TOKEN` for Mapbox's authenticated Maven repository.

If `BACKEND_BASE_URL` is omitted, the project uses `https://example.invalid/` as a non-routable build-time fallback. The app can compile, but backend features will not work.

## Troubleshooting

### Mapbox dependencies cannot download

Confirm `MAPBOX_DOWNLOADS_TOKEN` exists in the user-level Gradle properties and has the required downloads scope.

### The map is blank

Confirm `MAPBOX_ACCESS_TOKEN` is present and valid for the app configuration.

### Route search or weather is unavailable

Check Mapbox/OpenWeather credentials, network access, API quota, and package restrictions.

### Cool places, awareness, or personalised tips fail

Confirm `BACKEND_BASE_URL` and `UV_TIPS_API_KEY` point to a compatible deployment. These project-specific services are not bundled.

### Android Studio uses the wrong JDK

Set the Gradle JDK to 11 in Android Studio project settings and resync.
