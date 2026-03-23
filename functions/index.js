const {onDocumentCreated, onDocumentWritten} = require("firebase-functions/v2/firestore");
const {onSchedule} = require("firebase-functions/v2/scheduler");
const {onCall, HttpsError} = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
admin.initializeApp();

async function collectUserTargets(userIds) {
  const uniqueUserIds = [...new Set((userIds || []).filter(Boolean))];
  if (uniqueUserIds.length === 0) return [];

  const userSnaps = await Promise.all(
    uniqueUserIds.map((userId) => admin.firestore().collection("users").doc(userId).get())
  );

  const tokenSnaps = await Promise.all(
    uniqueUserIds.map((userId) => admin.firestore().collection("users").doc(userId).collection("device_tokens").get())
  );

  return uniqueUserIds.map((userId, index) => {
    const profile = userSnaps[index].data() || {};
    const subcollectionTokens = tokenSnaps[index].docs.map((doc) => doc.id).filter(Boolean);
    const tokens = [...new Set([
      ...subcollectionTokens,
      typeof profile.notificationToken === "string" ? profile.notificationToken : "",
    ].filter(Boolean))];
    return {userId, profile, tokens};
  }).filter((target) => target.tokens.length > 0);
}

async function sendToTargets(targets, payloadBuilder) {
  for (const target of targets) {
    const payload = payloadBuilder(target);
    if (!payload) continue;
    const response = await admin.messaging().sendEachForMulticast({
      tokens: target.tokens,
      ...payload,
    });

    const invalidTokens = [];
    response.responses.forEach((item, index) => {
      if (item.success) return;
      const code = item.error?.code || "";
      if (code.includes("registration-token-not-registered") || code.includes("invalid-registration-token")) {
        invalidTokens.push(target.tokens[index]);
      }
    });

    if (invalidTokens.length > 0) {
      const batch = admin.firestore().batch();
      invalidTokens.forEach((token) => {
        batch.delete(admin.firestore().collection("users").doc(target.userId).collection("device_tokens").doc(token));
        if (target.profile.notificationToken === token) {
          batch.set(
            admin.firestore().collection("users").doc(target.userId),
            {notificationToken: admin.firestore.FieldValue.delete()},
            {merge: true}
          );
        }
      });
      await batch.commit().catch(() => null);
    }
  }
}

exports.onMessageCreated = onDocumentCreated("chat_threads/{chatId}/messages/{messageId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;
  const message = snapshot.data() || {};
  const chatId = event.params.chatId;
  const messageId = event.params.messageId;
  const threadSnap = await admin.firestore().collection("chat_threads").doc(chatId).get();
  if (!threadSnap.exists) return;
  const thread = threadSnap.data() || {};
  const participantIds = Array.isArray(thread.participantIds) ? thread.participantIds : [];
  const recipients = participantIds.filter((id) => id && id !== message.senderId);
  if (recipients.length === 0) return;

  const targets = await collectUserTargets(recipients);
  if (targets.length === 0) return;

  const senderName = message.senderName || thread.title || "New message";
  const body = message.type === "text"
    ? (message.text || "Open Black Chat")
    : `${message.senderName || "Someone"} sent ${message.type || "media"}`;

  await sendToTargets(targets, (target) => {
    const mutedFor = Array.isArray(thread.mutedFor) ? thread.mutedFor : [];
    if (mutedFor.includes(target.userId)) return null;
    return {
      notification: {title: senderName, body},
      data: {
        chatId,
        messageId,
        type: "message",
        senderId: String(message.senderId || ""),
        senderName: String(message.senderName || senderName),
        title: String(senderName),
        body: String(body),
      },
      android: {
        priority: "high",
        ttl: 86400000,
        directBootOk: true,
        notification: {
          channelId: "chitchat_messages",
          tag: chatId,
          defaultSound: true,
          priority: "high",
          visibility: "private",
        },
      },
    };
  });
});

exports.onGlobalMessageCreated = onDocumentCreated("public_rooms/global_chat/messages/{messageId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;
  const message = snapshot.data() || {};
  const messageId = event.params.messageId;
  const usersSnap = await admin.firestore().collection("users").get();
  const recipients = usersSnap.docs
    .map((doc) => doc.id)
    .filter((userId) => userId && userId !== message.senderId);
  if (recipients.length === 0) return;

  const targets = await collectUserTargets(recipients);
  if (targets.length === 0) return;

  const senderName = message.senderName || "Global chat";
  const body = message.type === "text"
    ? (message.text || "Open Black Chat")
    : `${senderName} sent ${message.type || "media"}`;

  await sendToTargets(targets, () => ({
    notification: {title: `${senderName} in Global Chat`, body},
    data: {
      chatId: "global_chat",
      messageId,
      type: "message",
      senderId: String(message.senderId || ""),
      senderName: String(senderName),
      title: `${senderName} in Global Chat`,
      body: String(body),
    },
    android: {
      priority: "high",
      ttl: 86400000,
      directBootOk: true,
      notification: {
        channelId: "chitchat_messages",
        tag: "global_chat",
        defaultSound: true,
        priority: "high",
        visibility: "private",
      },
    },
  }));
});

exports.onCallRoomCreated = onDocumentCreated("call_rooms/{callId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) return;
  const room = snapshot.data() || {};
  if (room.state !== "ringing") return;
  const calleeIds = Array.isArray(room.calleeIds) ? room.calleeIds : [];
  if (calleeIds.length === 0) return;
  const targets = await collectUserTargets(calleeIds);
  if (targets.length === 0) return;

  await sendToTargets(targets, () => ({
    notification: {
      title: room.isVideo ? "Incoming video call" : "Incoming voice call",
      body: "Open Black Chat to answer.",
    },
    data: {
      type: "call",
      roomId: String(event.params.callId || ""),
      title: room.isVideo ? "Incoming video call" : "Incoming voice call",
      body: "Open Black Chat to answer.",
    },
    android: {
      priority: "high",
      ttl: 86400000,
      directBootOk: true,
      notification: {channelId: "chitchat_calls", defaultSound: true, priority: "high", visibility: "public"},
    },
  }));
});

exports.onPresenceUpdated = onDocumentWritten("users/{userId}", async (event) => {
  const after = event.data?.after?.data();
  if (!after) return;
  if (after.online === true) return;
  await event.data.after.ref.set({
    lastSeenAt: Date.now(),
    appState: "background",
  }, {merge: true});
});

exports.cleanupMedia = onSchedule("every 24 hours", async () => {
  const now = Date.now();
  const expiredStories = await admin.firestore()
    .collection("status_stories")
    .where("expiresAt", "<", now)
    .get();

  const batch = admin.firestore().batch();
  expiredStories.docs.forEach((doc) => batch.delete(doc.ref));
  if (!expiredStories.empty) {
    await batch.commit();
  }
});


exports.cleanupExpiredGlobalMessages = onSchedule("every 5 minutes", async () => {
  const now = Date.now();
  const expiredMessages = await admin.firestore()
    .collection("public_rooms")
    .doc("global_chat")
    .collection("messages")
    .where("expiresAt", "<=", now)
    .get();

  if (expiredMessages.empty) return;

  const batch = admin.firestore().batch();
  expiredMessages.docs.forEach((doc) => batch.delete(doc.ref));
  await batch.commit();
});


exports.cleanupExpiredTemporaryAccounts = onSchedule("every 24 hours", async () => {
  const now = Date.now();
  const expiredUsers = await admin.firestore()
    .collection("users")
    .where("deleteAfterAt", ">", 0)
    .where("deleteAfterAt", "<=", now)
    .get();

  if (expiredUsers.empty) return;

  for (const doc of expiredUsers.docs) {
    const profile = doc.data() || {};
    if (!profile.temporaryAccount) continue;
    const uid = doc.id;

    const username = typeof profile.username === "string" ? profile.username.trim().toLowerCase() : "";
    if (username) {
      const usernameRef = admin.firestore().collection("usernames").doc(username);
      const usernameSnap = await usernameRef.get();
      if (usernameSnap.exists && (usernameSnap.data()?.userId || "") === uid) {
        await usernameRef.delete();
      }
    }

    const storySnap = await admin.firestore()
      .collection("status_stories")
      .where("authorId", "==", uid)
      .get();
    if (!storySnap.empty) {
      const storyBatch = admin.firestore().batch();
      storySnap.docs.forEach((storyDoc) => storyBatch.delete(storyDoc.ref));
      await storyBatch.commit();
    }

    const tokenSnap = await doc.ref.collection("device_tokens").get();
    if (!tokenSnap.empty) {
      const tokenBatch = admin.firestore().batch();
      tokenSnap.docs.forEach((tokenDoc) => tokenBatch.delete(tokenDoc.ref));
      await tokenBatch.commit();
    }

    await doc.ref.delete();
    await admin.auth().deleteUser(uid).catch(() => null);
  }
});


exports.setGoldenVerifiedBadge = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in required.");
  }
  if (request.auth.token.admin !== true) {
    throw new HttpsError("permission-denied", "Only admin can manage golden badges.");
  }

  const userId = typeof request.data?.userId === "string" ? request.data.userId.trim() : "";
  const enabled = request.data?.enabled === true;
  if (!userId) {
    throw new HttpsError("invalid-argument", "userId is required.");
  }

  await admin.firestore().collection("users").doc(userId).set({
    goldenVerified: enabled,
    updatedAt: Date.now(),
  }, {merge: true});

  return {ok: true, userId, enabled};
});
