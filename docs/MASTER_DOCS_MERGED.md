# MASTER DOCS MERGED – 1.5 UPDATE

This merged file now tracks the latest status/call/security/reliability pass.

## Newly added in 1.5
- status viewer overlay
- seen viewers + reactions + reply actions
- grouped notifications + inline reply + mark-read
- secure window hooks for screenshot block / blur in recents
- retry dashboard labels + sync banner
- chat lazy-loading button
- richer privacy/security toggles

## Still needs backend/device validation
- full Room migration
- STUN/TURN production rollout
- encrypted local cache migration to a stronger storage layer
- full cross-device status reaction/reply sync

---

# ChitChat Master Docs (merged)

## Current build focus
- real app stabilization
- OTP hardening
- private/group realtime chat foundation
- media upload progress
- incoming call accept/reject
- offline queue + retry + cache cleanup

## Added in 1.4
- country code + resend timer OTP flow
- wrong OTP handling and timeout text
- safer session restore for OTP vs anonymous users
- message edit/delete/forward/star/copy-share shortcuts
- realtime upload progress for chat media and status media
- group backend hooks: invite link, promote admin, join request, admin-only send mode
- incoming call overlay with accept/reject

## Still requires backend/runtime verification
- Firebase console setup and SHA config
- Firestore rules/index deploy
- Cloud Functions deploy for push
- real STUN/TURN setup
- multi-device and encryption hardening
