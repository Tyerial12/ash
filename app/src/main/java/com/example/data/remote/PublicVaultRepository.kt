package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

@IgnoreExtraProperties
data class PublicVaultProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val overseerName: String = "",
    val vaultName: String = "",
    val visibility: String = "public",
    val prestigeLevel: Long = 0L,
    val commandLevel: Long = 1L,
    val dayCounter: Long = 1L,
    val totalSurvivors: Long = 0L,
    val uniqueDwellersCount: Long = 0L,
    val totalDefenseRating: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PublicSupplyDrop(
    @DocumentId val id: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val resourceType: String = "RATIONS",
    val amount: Long = 15L,
    val message: String = "",
    val visibility: String = "public",
    val status: String = "open",
    val claimedByUid: String? = null,
    val claimedByName: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class PublicVaultRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth
        get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observePublicVaults(): Flow<List<PublicVaultProfile>> = flow {
        val uid = auth.currentUser?.uid
        val path = "public_vaults"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PublicVaultProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareByDescending<PublicVaultProfile> { it.prestigeLevel }
                            .thenByDescending { it.commandLevel }
                            .thenByDescending { it.dayCounter }
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && (uid == null || auth.currentUser?.uid == uid)) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observePublicSupplyDrops(): Flow<List<PublicSupplyDrop>> = flow {
        val uid = auth.currentUser?.uid
        val path = "public_supply_drops"
        emitAll(
            db.collection(path)
                .whereEqualTo("visibility", "public")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PublicSupplyDrop::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && (uid == null || auth.currentUser?.uid == uid)) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun publishOrUpdateVaultProfile(
        overseerName: String,
        vaultName: String,
        prestigeLevel: Int,
        commandLevel: Int,
        dayCounter: Int,
        totalSurvivors: Int,
        uniqueDwellersCount: Int,
        totalDefenseRating: Int
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val safeOverseer = overseerName.trim().ifBlank { "Overseer" }.take(40)
        val safeVault = vaultName.trim().ifBlank { "Vault City" }.take(50)
        val docRef = db.collection("public_vaults").document(uid)
        val existingSnap = try {
            docRef.get().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }

        if (existingSnap.exists()) {
            val updatePayload = mapOf(
                "overseerName" to safeOverseer,
                "vaultName" to safeVault,
                "visibility" to "public",
                "prestigeLevel" to prestigeLevel.coerceAtLeast(0).toLong(),
                "commandLevel" to commandLevel.coerceAtLeast(1).toLong(),
                "dayCounter" to dayCounter.coerceAtLeast(1).toLong(),
                "totalSurvivors" to totalSurvivors.coerceAtLeast(0).toLong(),
                "uniqueDwellersCount" to uniqueDwellersCount.coerceAtLeast(0).toLong(),
                "totalDefenseRating" to totalDefenseRating.coerceAtLeast(0).toLong(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.update(updatePayload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        } else {
            val createPayload = mapOf(
                "userId" to uid,
                "overseerName" to safeOverseer,
                "vaultName" to safeVault,
                "visibility" to "public",
                "prestigeLevel" to prestigeLevel.coerceAtLeast(0).toLong(),
                "commandLevel" to commandLevel.coerceAtLeast(1).toLong(),
                "dayCounter" to dayCounter.coerceAtLeast(1).toLong(),
                "totalSurvivors" to totalSurvivors.coerceAtLeast(0).toLong(),
                "uniqueDwellersCount" to uniqueDwellersCount.coerceAtLeast(0).toLong(),
                "totalDefenseRating" to totalDefenseRating.coerceAtLeast(0).toLong(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(createPayload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
        }
        uid
    }

    suspend fun getVaultProfileById(targetUserId: String): Result<PublicVaultProfile?> = runCatching {
        val docRef = db.collection("public_vaults").document(targetUserId)
        try {
            val snap = docRef.get().await()
            snap.toObject(PublicVaultProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createSupplyDrop(
        senderName: String,
        resourceType: String,
        amount: Int,
        message: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val safeName = senderName.trim().ifBlank { "Overseer" }.take(40)
        val safeMsg = message.trim().ifBlank { "Vault City Supply Drop" }.take(120)
        val docRef = db.collection("public_supply_drops").document()
        val payload = mapOf(
            "senderUid" to uid,
            "senderName" to safeName,
            "resourceType" to resourceType,
            "amount" to amount.coerceIn(5, 500).toLong(),
            "message" to safeMsg,
            "visibility" to "public",
            "status" to "open",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
        docRef.id
    }

    suspend fun claimSupplyDrop(
        dropId: String,
        claimerName: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(dropId.isNotBlank()) { "Drop ID must not be blank" }
        val safeClaimer = claimerName.trim().ifBlank { "Overseer" }.take(40)
        val docRef = db.collection("public_supply_drops").document(dropId)
        val updatePayload = mapOf(
            "status" to "claimed",
            "claimedByUid" to uid,
            "claimedByName" to safeClaimer,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updatePayload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }
}
