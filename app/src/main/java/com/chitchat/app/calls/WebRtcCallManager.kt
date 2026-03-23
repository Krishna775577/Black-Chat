package com.chitchat.app.calls

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class SignalingCandidate(
    val ownerId: String = "",
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val candidate: String = "",
    val createdAt: Long = 0L,
)

data class CallRoomSnapshot(
    val roomId: String,
    val callerId: String,
    val calleeIds: List<String>,
    val isVideo: Boolean,
    val state: String,
    val offerSdp: String,
    val answerSdp: String,
    val updatedAt: Long,
)

class WebRtcCallManager(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val rooms = db.collection("call_rooms")

    private fun Any?.asStringList(): List<String> =
        when (this) {
            is List<*> -> this.filterIsInstance<String>()
            else -> emptyList()
        }

    suspend fun createOutgoingRoom(callerId: String, calleeIds: List<String>, isVideo: Boolean): String {
        val doc = rooms.document()
        val now = System.currentTimeMillis()
        doc.set(
            mapOf(
                "callerId" to callerId,
                "calleeIds" to calleeIds,
                "isVideo" to isVideo,
                "state" to "ringing",
                "offerSdp" to "",
                "answerSdp" to "",
                "startedAt" to now,
                "updatedAt" to now,
            )
        ).await()
        return doc.id
    }

    fun observeIncomingRooms(userId: String): Flow<List<CallRoomSnapshot>> = callbackFlow {
        val registration = rooms
            .whereArrayContains("calleeIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val rooms = snapshot?.documents.orEmpty().mapNotNull { doc -> doc.toSnapshot() }
                    .filter { it.state in setOf("ringing", "offer_sent", "connected") }
                    .sortedByDescending { it.updatedAt }
                trySend(rooms)
            }
        awaitClose { registration.remove() }
    }

    fun observeRoom(roomId: String): Flow<CallRoomSnapshot?> = callbackFlow {
        val registration = rooms.document(roomId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toSnapshot())
        }
        awaitClose { registration.remove() }
    }

    fun observeIceCandidates(roomId: String): Flow<List<SignalingCandidate>> = callbackFlow {
        val registration = rooms.document(roomId)
            .collection("ice_candidates")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val candidates = snapshot?.documents.orEmpty().map {
                    SignalingCandidate(
                        ownerId = it.getString("ownerId").orEmpty(),
                        sdpMid = it.getString("sdpMid").orEmpty(),
                        sdpMLineIndex = (it.getLong("sdpMLineIndex") ?: 0L).toInt(),
                        candidate = it.getString("candidate").orEmpty(),
                        createdAt = it.getLong("createdAt") ?: 0L,
                    )
                }
                trySend(candidates)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveOffer(roomId: String, offerSdp: String) {
        rooms.document(roomId).update(
            mapOf(
                "offerSdp" to offerSdp,
                "state" to "offer_sent",
                "updatedAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    suspend fun saveAnswer(roomId: String, answerSdp: String) {
        rooms.document(roomId).update(
            mapOf(
                "answerSdp" to answerSdp,
                "state" to "connected",
                "updatedAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    suspend fun acceptCall(roomId: String) {
        rooms.document(roomId).update(
            mapOf(
                "state" to "connected",
                "updatedAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    suspend fun rejectCall(roomId: String) {
        endCall(roomId, reason = "rejected")
    }

    suspend fun addIceCandidate(roomId: String, candidate: SignalingCandidate) {
        rooms.document(roomId).collection("ice_candidates").add(
            mapOf(
                "ownerId" to candidate.ownerId,
                "sdpMid" to candidate.sdpMid,
                "sdpMLineIndex" to candidate.sdpMLineIndex,
                "candidate" to candidate.candidate,
                "createdAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    suspend fun heartbeat(roomId: String) {
        rooms.document(roomId).update("updatedAt", System.currentTimeMillis()).await()
    }

    suspend fun endCall(roomId: String, reason: String = "ended") {
        rooms.document(roomId).update(
            mapOf(
                "state" to reason,
                "updatedAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toSnapshot(): CallRoomSnapshot? {
        val data = data ?: return null
        return CallRoomSnapshot(
            roomId = id,
            callerId = data["callerId"] as? String ?: "",
            calleeIds = data["calleeIds"].asStringList(),
            isVideo = data["isVideo"] as? Boolean ?: false,
            state = data["state"] as? String ?: "ringing",
            offerSdp = data["offerSdp"] as? String ?: "",
            answerSdp = data["answerSdp"] as? String ?: "",
            updatedAt = data["updatedAt"] as? Long ?: 0L,
        )
    }
}
