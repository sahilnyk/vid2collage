# vid2collage

vid2collage takes a portrait video, finds the people in it, counts their separate appearances, and makes one clean collage tile for each person. Everything runs on the phone.

## setup

You need Java 17 and Android SDK 37.

```bash
./gradlew assembleDebug
```

The debug apk is created at `app/build/outputs/apk/debug/app-debug.apk`. The app targets ARM64 Android phones.

## use

1. Open vid2collage.
2. Tap `choose a video` and pick a portrait clip.
3. Leave the app on the processing screen while it detects and matches faces.
4. Review the people count, appearance counts, and generated collage.
5. Tap `save to gallery` or `share collage` when you are happy with it.

The three sample clips work through the same flow. Their results are not hardcoded.

## how it works

- ML Kit detects faces in sampled video frames.
- FaceNet creates 128-value embeddings from aligned 160 × 160 face crops.
- Average-link clustering groups embeddings using cosine similarity.
- The similarity threshold is `0.62`.
- Frontality, sharpness, open eyes, expression, and crop margins choose the representative frame.
- A clear face must remain visible for at least `750 ms`, so blurred whip-pan transitions are ignored.

The FaceNet model and its Apache 2.0 license are in `app/src/main/assets`. There is no backend or upload step. The interface uses the Bricolage Grotesque typeface and native Compose Material 3.

## checks

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```
