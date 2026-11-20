package com.example.data

import android.content.Context
import com.example.R
import com.example.model.AvatarOption
import com.example.model.ChildProfile
import com.example.model.DifficultyTier
import com.example.model.GameMode
import com.example.model.GameSession
import com.example.model.LeaderboardEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MathSparkRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Please sign in with Google to save progress.")
        if (expectedUserId != null) {
            check(uid == expectedUserId) { "Active account changed; refusing cross-user access." }
        }
        return uid
    }

    fun observeProfile(userId: String): Flow<ChildProfile?> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(ChildProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeRecentSessions(userId: String): Flow<List<GameSession>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/sessions"
        emitAll(
            db.collection("users").document(uid).collection("sessions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(GameSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeLeaderboard(userId: String): Flow<List<LeaderboardEntry>> = flow {
        val uid = requireUserId(userId)
        val path = "leaderboard"
        emitAll(
            db.collection("leaderboard")
                .whereEqualTo("publicEntry", true)
                .limit(50)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(LeaderboardEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(
                            compareByDescending<LeaderboardEntry> { it.brainScore }
                                .thenByDescending { it.totalStars }
                        )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureProfileExists(userId: String, defaultName: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid)
            val leaderboardRef = db.collection("leaderboard").document(uid)
            val existing = docRef.get().await()
            val safeName = defaultName.trim().ifBlank { "Math Explorer" }.take(30)

            if (!existing.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "childName" to safeName,
                    "avatarId" to AvatarOption.OWL.id,
                    "difficultyTier" to DifficultyTier.EXPLORER.name,
                    "totalStars" to 0L,
                    "brainScore" to 100L,
                    "arithmeticSkill" to 20L,
                    "speedSkill" to 20L,
                    "logicSkill" to 20L,
                    "memorySkill" to 20L,
                    "currentStreakDays" to 0L,
                    "bestStreakCombo" to 0L,
                    "sessionsCompleted" to 0L,
                    "totalCorrect" to 0L,
                    "totalAnswered" to 0L,
                    "lastPlayedDate" to "",
                    "unlockedBadges" to emptyList<String>(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                val lbPayload = mapOf(
                    "userId" to uid,
                    "childName" to safeName,
                    "avatarId" to AvatarOption.OWL.id,
                    "difficultyTier" to DifficultyTier.EXPLORER.name,
                    "totalStars" to 0L,
                    "brainScore" to 100L,
                    "currentStreakDays" to 0L,
                    "publicEntry" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                val batch = db.batch()
                batch.set(docRef, payload)
                batch.set(leaderboardRef, lbPayload)
                batch.commit().await()
            } else {
                val lbSnap = leaderboardRef.get().await()
                if (!lbSnap.exists()) {
                    val profile = existing.toObject(ChildProfile::class.java)
                    if (profile != null) {
                        leaderboardRef.set(
                            mapOf(
                                "userId" to uid,
                                "childName" to profile.childName.take(30).ifBlank { safeName },
                                "avatarId" to profile.avatarEnum.id,
                                "difficultyTier" to profile.tierEnum.name,
                                "totalStars" to profile.totalStars.coerceIn(0L, 1000000L),
                                "brainScore" to profile.brainScore.coerceIn(0L, 999L),
                                "currentStreakDays" to profile.currentStreakDays.coerceIn(0L, 10000L),
                                "publicEntry" to true,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProfileSettings(
        userId: String,
        childName: String,
        avatar: AvatarOption,
        difficultyTier: DifficultyTier
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val cleanName = childName.trim().take(30)
            require(cleanName.isNotEmpty()) { "Nickname must not be empty." }

            val profileRef = db.collection("users").document(uid)
            val leaderboardRef = db.collection("leaderboard").document(uid)
            val lbSnap = leaderboardRef.get().await()

            val batch = db.batch()
            batch.update(
                profileRef,
                mapOf(
                    "childName" to cleanName,
                    "avatarId" to avatar.id,
                    "difficultyTier" to difficultyTier.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (lbSnap.exists()) {
                batch.update(
                    leaderboardRef,
                    mapOf(
                        "childName" to cleanName,
                        "avatarId" to avatar.id,
                        "difficultyTier" to difficultyTier.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun recordGameSession(
        userId: String,
        currentProfile: ChildProfile,
        mode: GameMode,
        difficultyTier: DifficultyTier,
        score: Long,
        starsEarned: Long,
        correctCount: Long,
        totalQuestions: Long,
        bestCombo: Long,
        durationSeconds: Long
    ): Result<List<String>> {
        val profilePath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val safeTotal = totalQuestions.coerceIn(1L, 200L)
            val safeCorrect = correctCount.coerceIn(0L, safeTotal)
            val safeScore = score.coerceIn(0L, 100000L)
            val safeStars = starsEarned.coerceIn(0L, 500L)
            val safeCombo = bestCombo.coerceIn(0L, 200L)
            val safeDuration = durationSeconds.coerceIn(1L, 3600L)

            val accuracyRatio = safeCorrect.toDouble() / safeTotal.toDouble()
            val skillGain = when {
                accuracyRatio >= 0.9 -> 10L
                accuracyRatio >= 0.7 -> 7L
                accuracyRatio >= 0.5 -> 4L
                else -> 2L
            } + when (difficultyTier) {
                DifficultyTier.EXPLORER -> 0L
                DifficultyTier.CHALLENGER -> 2L
                DifficultyTier.GENIUS -> 4L
            }

            val newArithmetic = if (mode == GameMode.ARITHMETIC) {
                (currentProfile.arithmeticSkill + skillGain).coerceIn(0L, 100L)
            } else currentProfile.arithmeticSkill

            val newSpeed = if (mode == GameMode.SPEED_SPRINT) {
                (currentProfile.speedSkill + skillGain).coerceIn(0L, 100L)
            } else currentProfile.speedSkill

            val newLogic = if (mode == GameMode.PATTERN_LOGIC) {
                (currentProfile.logicSkill + skillGain).coerceIn(0L, 100L)
            } else currentProfile.logicSkill

            val newMemory = if (mode == GameMode.MEMORY_BALANCE) {
                (currentProfile.memorySkill + skillGain).coerceIn(0L, 100L)
            } else currentProfile.memorySkill

            val avgSkill = (newArithmetic + newSpeed + newLogic + newMemory) / 4.0
            val newSessions = (currentProfile.sessionsCompleted + 1L).coerceAtMost(100000L)
            val newTotalStars = (currentProfile.totalStars + safeStars).coerceAtMost(1000000L)
            val newTotalCorrect = (currentProfile.totalCorrect + safeCorrect).coerceAtMost(1000000L)
            val newTotalAnswered = (currentProfile.totalAnswered + safeTotal).coerceAtMost(1000000L)
            val newBestCombo = maxOf(currentProfile.bestStreakCombo, safeCombo).coerceAtMost(1000L)
            val newBrainScore = (100L + (avgSkill * 7.5).toLong() + (newSessions * 4L).coerceAtMost(150L)).coerceIn(0L, 999L)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayStr = dateFormat.format(Date())
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayStr = dateFormat.format(cal.time)
            val newStreakDays = when (currentProfile.lastPlayedDate) {
                todayStr -> maxOf(1L, currentProfile.currentStreakDays)
                yesterdayStr -> (currentProfile.currentStreakDays + 1L).coerceAtMost(10000L)
                else -> 1L
            }

            val updatedBadges = currentProfile.unlockedBadges.toMutableSet()
            val newlyEarned = mutableListOf<String>()
            fun unlock(badgeId: String) {
                if (updatedBadges.size < 20 && updatedBadges.add(badgeId)) {
                    newlyEarned.add(badgeId)
                }
            }
            unlock("first_spark")
            if (mode != GameMode.SPEED_SPRINT && safeTotal >= 5 && safeCorrect == safeTotal) {
                unlock("perfect_round")
            }
            if (safeCombo >= 5L) unlock("combo_master")
            if (mode == GameMode.SPEED_SPRINT && safeCorrect >= 10L) unlock("speed_flash")
            if (mode == GameMode.PATTERN_LOGIC && safeCorrect >= 4L) unlock("pattern_sleuth")
            if (mode == GameMode.MEMORY_BALANCE && safeCorrect >= 4L) unlock("memory_wizard")
            if (newTotalStars >= 100L) unlock("star_collector")
            if (newStreakDays >= 3L) unlock("streak_keeper")

            val profileRef = db.collection("users").document(uid)
            val profileSnap = profileRef.get().await()
            if (!profileSnap.exists()) {
                ensureProfileExists(uid, currentProfile.childName).getOrThrow()
            }
            val leaderboardRef = db.collection("leaderboard").document(uid)
            val lbSnap = leaderboardRef.get().await()
            val sessionRef = profileRef.collection("sessions").document()

            val sessionPayload = mapOf(
                "userId" to uid,
                "mode" to mode.name,
                "difficultyTier" to difficultyTier.name,
                "score" to safeScore,
                "starsEarned" to safeStars,
                "correctCount" to safeCorrect,
                "totalQuestions" to safeTotal,
                "bestCombo" to safeCombo,
                "durationSeconds" to safeDuration,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val progressPayload = mapOf(
                "totalStars" to newTotalStars,
                "brainScore" to newBrainScore,
                "arithmeticSkill" to newArithmetic,
                "speedSkill" to newSpeed,
                "logicSkill" to newLogic,
                "memorySkill" to newMemory,
                "currentStreakDays" to newStreakDays,
                "bestStreakCombo" to newBestCombo,
                "sessionsCompleted" to newSessions,
                "totalCorrect" to newTotalCorrect,
                "totalAnswered" to newTotalAnswered,
                "lastPlayedDate" to todayStr,
                "unlockedBadges" to updatedBadges.toList().take(20),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val batch = db.batch()
            batch.set(sessionRef, sessionPayload)
            batch.update(profileRef, progressPayload)
            if (lbSnap.exists()) {
                batch.update(
                    leaderboardRef,
                    mapOf(
                        "totalStars" to newTotalStars,
                        "brainScore" to newBrainScore,
                        "currentStreakDays" to newStreakDays,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.set(
                    leaderboardRef,
                    mapOf(
                        "userId" to uid,
                        "childName" to currentProfile.childName.take(30).ifBlank { "Math Explorer" },
                        "avatarId" to currentProfile.avatarEnum.id,
                        "difficultyTier" to currentProfile.tierEnum.name,
                        "totalStars" to newTotalStars,
                        "brainScore" to newBrainScore,
                        "currentStreakDays" to newStreakDays,
                        "publicEntry" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()

            Result.success(newlyEarned)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, profilePath)
            Result.failure(e)
        }
    }
}
