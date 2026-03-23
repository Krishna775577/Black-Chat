# Email + Google Login Setup

## App changes included
- Phone number + OTP login UI removed from the main login flow
- Permanent account now supports:
  - Email + password signup
  - Email + password login
  - Google Sign-In
- Temporary username account flow is still available

## Firebase Console setup required for Google Sign-In
Google Sign-In button code is already added, but Firebase Console setup is still required.

### 1) Enable providers
Firebase Console -> Authentication -> Sign-in method
- Enable **Email/Password**
- Enable **Google**

### 2) Add Web client ID
Create or copy your Firebase/Google OAuth **Web client ID** and replace this value in:

`app/src/main/res/values/strings.xml`

```xml
<string name="google_web_client_id">YOUR_WEB_CLIENT_ID.apps.googleusercontent.com</string>
```

### 3) SHA fingerprints
If Google Sign-In does not work, add your app SHA-1 and SHA-256 in:
Firebase Console -> Project settings -> Your Android app

Then download the updated `google-services.json` if Firebase asks for it.

## Notes
- Email login works after enabling the Email/Password provider.
- Google Sign-In needs correct Web client ID and SHA setup.
- Firestore user profile flow remains the same as before.
