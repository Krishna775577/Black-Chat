# Black Chat

Black Chat is an Android chat app with private chat, group chat, updates/status, calls, media sharing, profiles, themes, reactions, scheduling, hidden chats, link previews, and many premium-style UI features.

This README is written for **two types of people**:
1. **New users** who want to learn how to use the app
2. **Developers** who want to run or configure the project

---

# 1) What this app includes

## Main user features
- private chat
- group chat
- global/public room support (project configuration dependent)
- text, image, video, file, and voice-note style messaging
- reply, edit, delete for me, delete for everyone
- reactions (reaction bar opens on **tap & hold for 1 second**)
- message search inside chat
- scheduled messages
- custom chat wallpaper/theme
- in-chat social link preview and player support
- full-screen image/media viewing
- updates/status (text, image, video, voice)
- status viewers and status replies
- calls screen and call history flow
- profile banner, profile photo, verification badge, DP Ring
- app lock, hidden chats, backup/restore, linked devices/session options

## Important backend-dependent features
These need correct backend setup/config before they work fully:
- push notifications in background/killed state
- real Firebase sync for some advanced actions
- cloud media uploads
- functions-based notification routing
- any production calling setup that requires external infra

---

# 2) New user guide – how to use Black Chat

This section is for a new user who installs the app for the first time.

## A. First open
1. Open **Black Chat**.
2. Sign in using the method enabled in your build.
3. Complete your basic profile:
   - profile photo
   - name
   - bio/about
   - banner (if enabled in your profile editor)
4. After login you will reach the home screen.

## B. Home screen tabs
Usually the home screen is divided into:
- **Chats** – all personal and group chats
- **Updates** – your status and others' statuses
- **Calls** – call logs/history and call actions

---

# 3) How to use Chats

## A. Start a personal chat
1. Open the **Chats** tab.
2. Tap the add/new chat action.
3. Select a user.
4. Type your message and press send.

## B. Create a group
1. Open **Chats**.
2. Tap the create/new group option.
3. Select members.
4. Enter group name.
5. Save the group.

## C. Send messages
Inside a chat, you can usually send:
- text messages
- image messages
- video messages
- files/documents
- voice notes (room support depends on build configuration)
- link messages

## D. Reply to a message
1. Tap and hold a message.
2. Choose **Reply**.
3. Type your reply.
4. Send it.

The replied message preview will show above the composer and in the sent reply bubble.

## E. Edit your message
1. Tap and hold **your own text message**.
2. Choose **Edit**.
3. Change the text.
4. Save/send the edit.

## F. Delete messages
Tap and hold your message and choose:
- **Delete for me** – only your view removes it
- **Delete for everyone** – removes/replaces it for both sides where supported

## G. Reactions
The reaction bar is set to show on **tap & hold for 1 second**.

How it works:
1. Tap and hold a chat bubble for 1 second.
2. Reaction bar appears.
3. Tap a reaction emoji.
4. The reaction is attached to that message.

## H. Search inside chat
1. Open the chat menu (usually 3 dots).
2. Tap **Search**.
3. Type a keyword.
4. Matching messages will filter/show.

## I. Schedule a message
1. Open a chat.
2. Use the message menu or schedule option.
3. Choose a time like:
   - 15 minutes
   - 1 hour
   - 8 hours
   - tomorrow
4. The message will be queued and sent when due.

## J. Send images/videos/files
1. Use the attach/media button.
2. Pick image, video, document, or other supported file.
3. Confirm send.
4. Recipient can tap supported media for full-screen preview.

## K. View photos full screen
When an image is sent in chat:
1. Tap the image bubble.
2. It opens in full-screen view.

This also applies to profile picture preview where supported.

## L. Link preview and in-chat player
If someone sends a supported link like YouTube and some public social links:
- the app can show a **preview card/thumbnail**
- supported links may open in an **in-chat player**
- full-screen player is available where implemented

Note: some platforms like Instagram/Facebook/X/TikTok may still depend on whether the post is public and embeddable.

## M. Custom chat background/theme
1. Open settings/theme or chat customization.
2. Choose a theme color or custom photo.
3. Save.
4. The selected wallpaper/theme shows behind chat.

If your photo is not showing, reselect the image and verify storage permission if your Android version requires it.

---

# 4) How to use Updates / Status

## A. Post a status
1. Open the **Updates** tab.
2. Tap **My Status** or the add/camera action.
3. Choose the type:
   - text
   - image
   - video
   - voice
4. Select audience if your build supports audience control.
5. Post the status.

## B. View status
1. Open **Updates**.
2. Tap a user status.
3. It opens in a **full-screen viewer** with user info.

## C. Reply to status
1. Open a status.
2. Type reply in the reply field if visible.
3. Send.
4. The reply can sync to private chat where supported.

## D. Status viewers
If enabled in your build:
- you can see total views
- viewer names may be shown
- status reply/reaction flow may appear

---

# 5) How to use Calls

## A. Open Calls tab
The **Calls** tab shows your call flow/history.

## B. Start a call
1. Open a user chat or profile.
2. Tap call or video call action.
3. The call screen/overlay opens.

## C. View call history
Calls screen can show incoming, outgoing, or missed calls depending on what is enabled in your build.

---

# 6) How to use Profile

## A. Open profile
Profile can be opened by:
- tapping your own profile area
- tapping another user's name in chat header
- tapping profile-related actions from chat or lists

## B. What shows in profile
Profile may include:
- top banner
- profile photo
- username/name
- verification badge (if enabled)
- bio/about
- profile actions

## C. Profile picture full-screen
Tap profile picture to open full-screen preview where supported.

## D. DP Ring
DP Ring is a decorative effect around the profile picture.

How to change it:
1. Open **Settings**.
2. Open **Dp Ring**.
3. Select a style, for example:
   - Off
   - Neon glow
   - Premium gold
   - Royal glow
   - Rainbow
4. Save/apply.

## E. Scanner in profile
If your build includes the custom profile scanner:
- scanner opens from the app UI
- it does not rely on the phone camera app directly
- quality is configured to target high quality / 1080p where device support allows

---

# 7) Hidden Chats

If hidden chats are enabled in your build:
1. Open chat menu.
2. Tap **Hide chat**.
3. The chat moves to hidden section.
4. Open **Hidden chats** from the relevant menu to see them.
5. Use **Unhide** to bring them back.

For privacy-focused setups, hidden chats may be combined with app lock or PIN.

---

# 8) App Lock / Security

## A. PIN lock
If app lock is enabled:
1. Go to app security settings.
2. Set a PIN.
3. Next time app reopens or resumes, lock screen can appear.

## B. Biometric unlock
If your device supports it and your build includes the dependency:
- use fingerprint/biometric unlock from lock screen

## C. Verification badge
Verification can appear as a badge beside the user name. In the cleaned profile layout it should appear next to the name only, without extra verification tiles.

---

# 9) Notifications

The app may support:
- message notifications
- group notifications
- call alerts
- inline reply
- mark as read
- grouped notifications
- quiet hours
- message previews on/off

## How to adjust notifications
1. Open **Settings**.
2. Open notification settings.
3. Turn on/off the options you want.

Important: background push notifications usually need Firebase Messaging + Cloud Functions setup to work fully.

---

# 10) Backup / Restore

If backup is enabled in your build:
1. Open settings.
2. Use **Backup** to save snapshot data.
3. Use **Restore** to bring data back.

Note:
- backup behavior depends on the storage implementation in your current build
- encryption/obfuscation may be lightweight unless you upgrade it further

---

# 11) Group and community features

Depending on your build, group/community rooms may support:
- admin-only sending
- add/remove members
- invite links
- polls
- pinned chats
- archived chats
- media gallery

## Polls in group
1. Open a group.
2. Choose **Create poll**.
3. Enter question and options.
4. Post the poll.

---

# 12) Settings guide

Common settings/features available in this project may include:
- theme mode
- custom wallpaper/photo theme
- Dp Ring
- notifications
- privacy/security
- app lock
- hidden chats
- backup/restore
- linked devices/session controls

---

# 13) Developer setup

This section is for running the project in Android Studio.

## Requirements
- Android Studio (latest stable recommended)
- JDK compatible with your Android Gradle Plugin
- Android SDK installed
- Firebase project
- Cloudinary account (for media upload builds)

## Project files to check
- `app/google-services.json`
- `gradle.properties`
- `functions/index.js`
- `firestore.rules`
- `firestore.indexes.json`
- `storage.rules` (if still used in your variant)

## Cloudinary setup
Open `gradle.properties` and set:

```properties
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_UPLOAD_PRESET=your_unsigned_preset
```

## Firebase setup
Make sure you configure:
- Authentication
- Firestore
- Messaging
- Functions (for advanced push behavior)

Also verify:
- package name matches Firebase app
- SHA fingerprints are added if needed
- Firestore rules and indexes are deployed

## Build the app
Open the project in Android Studio and run:
- **Sync Project with Gradle Files**
- then build/run the `app` module

If you use terminal:

```bash
./gradlew assembleDebug
```

On Windows:

```bash
gradlew.bat assembleDebug
```

## Deploy functions
If your notification or backend logic depends on functions:

```bash
firebase deploy --only functions
```

## Deploy Firestore rules/indexes

```bash
firebase deploy --only firestore:rules
firebase deploy --only firestore:indexes
```

---

# 14) Recommended first checks after build

After first successful install, test these flows one by one:
1. login
2. open chats
3. send text message
4. send image
5. open image full screen
6. react to a message with 1-second hold
7. post a status
8. open status full screen
9. open profile
10. change Dp Ring
11. apply custom wallpaper
12. test notification in foreground/background
13. test hidden chat / app lock if enabled

---

# 15) Troubleshooting

## Chat sync issues
- verify Firebase configuration
- verify Firestore rules
- check network access

## Notifications not arriving
- make sure Firebase Messaging is configured
- deploy Cloud Functions if required
- check Android notification permission on newer Android versions
- disable battery restrictions for testing

## Media upload not working
- verify Cloudinary cloud name and unsigned preset
- verify internet connection
- verify runtime permissions if local file access is needed

## Custom wallpaper not visible
- reselect the image
- confirm file permission access
- verify your build includes the wallpaper fix version

## Link preview/player not working
- some public social embeds can fail if the original post is private, restricted, or blocks embedding
- YouTube is usually the most reliable

## Scanner warnings during compile
- old Camera API deprecation warnings do not always mean a build failure
- long-term recommended fix is migration to CameraX

---

# 16) Project structure (important folders)

```text
app/
  src/main/java/com/chitchat/app/
    ui/
    data/
    notifications/
    qr/
functions/
docs/
web-client/
```

---

# 17) Final note

This project has many UI and feature upgrades already patched at source level. Some features are **fully visible in UI**, while some advanced features still depend on **final backend deployment, real device validation, and production testing**.

If you are handing this project to a new user or tester, tell them to start with:
- profile setup
- chats
- status
- calls
- settings
- privacy/notifications

That is the fastest way to learn the app.
