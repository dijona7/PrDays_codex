# PrApp

PrApp is an Android application built with Jetpack Compose for tracking menstrual cycles.
It stores period start and end dates, predicts future cycles using a moving average
model and allows users to review or edit the data.

## Features
- View cycle history grouped by month.
- Edit date ranges manually or through a simple file editor.
- Automatic prediction of future cycles based on recent history.
- Basic settings for prediction window, cycle duration and interface language.

## Usage
1. Open the project in Android Studio or build it from the command line.
2. To build tests or the debug APK, run:
   ```bash
   ./gradlew test
   ./gradlew assembleDebug
   ```
3. Install the generated APK on an Android device or emulator.
4. On first launch the app copies a `date_ranges.txt` file from assets to internal storage.
   You can edit the ranges via the File tab or adjust prediction parameters in Settings.

