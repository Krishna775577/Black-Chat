# Golden verification badge setup

## What was added
- New backend field: `goldenVerified: Boolean`
- Golden badge UI on:
  - home search results
  - chat list rows
  - chat header
  - user profile screen
  - own profile screen

## How to give a badge to a user
### Option 1: Firestore Console
Open `users/{userId}` and set:

```json
{
  "goldenVerified": true
}
```

To remove it, set `goldenVerified` to `false`.

### Option 2: Secure Cloud Function
A callable Cloud Function was added:
- name: `setGoldenVerifiedBadge`
- input:

```json
{
  "userId": "TARGET_UID",
  "enabled": true
}
```

This function only works for Firebase Auth users with custom claim:

```json
{
  "admin": true
}
```

## Important
Firestore rules in this project allow a user to update only their own `users/{userId}` document.
So giving a badge to someone else should be done from Firestore Console or from the admin Cloud Function, not directly from the normal client app.
