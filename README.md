# IYKYK

Creates one on-device collage from a portrait video, with one tile and an appearance count for each person.

## Build

Requires Java 17 and Android SDK 37.

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Approach

- Bundled ML Kit detects and tracks faces.
- [FaceNet](https://github.com/shubham0204/FaceRecognition_With_FaceNet_Android) creates 128-value embeddings from aligned 160 × 160 face crops.
- Average-link clustering uses cosine similarity `0.62`; overlapping appearances can never join one identity.
- Frontality, sharpness, open eyes, expression, and crop margins choose each representative shot.
- MediaStore saves the collage and the Android Sharesheet shares it. No backend is used.

The cosine-similarity threshold is `0.62`. The model and its Apache 2.0 license are bundled in `app/src/main/assets`. ML Kit face detection, FaceNet embedding, and average-link clustering all run on-device with no backend. An appearance must remain clear for at least `750 ms`, which rejects the supplied clips' blurred whip-pan transitions without hardcoding video results.

## Check

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```
