# Black Chat (Cloudinary media build)

This build moves media uploads away from Firebase Storage and uses **Cloudinary unsigned uploads** for:
- profile photos
- chat images / videos / files
- status media

## Before building
Open `gradle.properties` and set your real Cloudinary values:

```properties
CLOUDINARY_CLOUD_NAME=dtnqffvzs
CLOUDINARY_UPLOAD_PRESET=blackchat_unsigned
```

## Cloudinary console setup
1. Create a Cloudinary account.
2. Copy your **Cloud name** from the dashboard.
3. Go to **Settings -> Upload -> Upload presets**.
4. Create an **unsigned** upload preset.
5. Put that preset name into `gradle.properties`.

## Notes
- Firebase Auth / Firestore / Messaging remain unchanged.
- `firebase deploy --only storage` is no longer needed for media uploads in this build.
- Full Android runtime verification was not possible in this environment.
