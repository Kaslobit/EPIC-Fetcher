# EPIC-Fetcher

Simple application that fetches images from NASA's EPIC for the last 24 hours and saves them. Requires JDK 21 or above.

Requires a NASA API key, get one [here.](https://api.nasa.gov)

## Usage

Build the project:
```bash
./gradlew build
```
Output will be at `build/libs/EPIC-Fetcher-1.0.jar`. You need to set the environment variable `NASA_API_KEY`, or it will fall back to the highly rate-limited `DEMO_KEY`. Run it like:
```bash
export NASA_API_KEY=<your-nasa-api-key>
java -jar build/libs/EPIC-Fetcher-1.0.jar
```
