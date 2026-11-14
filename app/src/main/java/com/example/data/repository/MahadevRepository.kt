package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.SecurityCrypto
import com.example.core.handleFirestoreError
import com.example.core.unwrapFirestoreException
import com.example.data.model.AdminRecord
import com.example.data.model.AppNotification
import com.example.data.model.PanelRequest
import com.example.data.model.SupportTicket
import com.example.data.model.TransactionRecord
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MahadevRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    fun isBootstrappedAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true) && user.isEmailVerified
    }

    // --- User Profile & Onboarding ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) null
                else snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                val fsErr = error.unwrapFirestoreException()
                if (fsErr == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsErr ?: error, OperationType.GET, path)
                }
                throw error
            }
    }

    fun observeAdminRecord(adminId: String): Flow<AdminRecord?> {
        val path = "admins/$adminId"
        return db.collection("admins").document(adminId)
            .snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) null
                else snapshot.toObject(AdminRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                val fsErr = error.unwrapFirestoreException()
                if (fsErr == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == adminId) {
                    handleFirestoreError(fsErr ?: error, OperationType.GET, path)
                }
                throw error
            }
    }

    suspend fun registerUserProfile(
        fullName: String,
        email: String,
        phone: String,
        rawPin: String,
        otpVerified: Boolean
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val pinHash = if (rawPin.isNotBlank()) SecurityCrypto.hashSecret(rawPin) else ""
        val role = if (isBootstrappedAdmin()) "admin" else "user"
        val payload = mapOf(
            "userId" to uid,
            "fullName" to fullName.trim().take(80),
            "email" to email.trim().take(120),
            "phone" to phone.trim().take(20),
            "role" to role,
            "status" to "active",
            "accessLevel" to "standard",
            "otpVerified" to otpVerified,
            "pinHash" to pinHash,
            "walletBalance" to 0.0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            val batch = db.batch()
            batch.set(docRef, payload)
            if (isBootstrappedAdmin()) {
                val adminRef = db.collection("admins").document(uid)
                batch.set(
                    adminRef,
                    mapOf(
                        "adminId" to uid,
                        "email" to email.trim().take(120),
                        "displayName" to fullName.trim().take(80),
                        "adminPinHash" to pinHash,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            val welcomeNotif = db.collection("notifications").document()
            batch.set(
                welcomeNotif,
                mapOf(
                    "userId" to uid,
                    "title" to "Welcome to Mahadev Panel",
                    "message" to "Your encrypted profile is active. Complete OTP verification anytime from Account Status.",
                    "category" to "security",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateOwnProfile(
        fullName: String,
        phone: String,
        otpVerified: Boolean,
        newRawPin: String? = null,
        existingPinHash: String = ""
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val finalPinHash = if (!newRawPin.isNullOrBlank()) SecurityCrypto.hashSecret(newRawPin) else existingPinHash
        val updates = mapOf(
            "fullName" to fullName.trim().take(80),
            "phone" to phone.trim().take(20),
            "otpVerified" to otpVerified,
            "pinHash" to finalPinHash,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveAdminPin(displayName: String, email: String, rawPin: String): Result<Unit> {
        val uid = requireUserId()
        val adminRef = db.collection("admins").document(uid)
        val pinHash = SecurityCrypto.hashSecret(rawPin)
        return try {
            val snap = adminRef.get().await()
            if (snap.exists()) {
                adminRef.update(
                    mapOf(
                        "adminPinHash" to pinHash,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                adminRef.set(
                    mapOf(
                        "adminId" to uid,
                        "email" to email.trim().take(120),
                        "displayName" to displayName.trim().take(80),
                        "adminPinHash" to pinHash,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, adminRef.path)
            Result.failure(e)
        }
    }

    // --- User Scoped Streams ---

    fun observeUserRequests(userId: String): Flow<List<PanelRequest>> {
        val path = "requests"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PanelRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val fsErr = error.unwrapFirestoreException()
                if (fsErr == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeUserTransactions(userId: String): Flow<List<TransactionRecord>> {
        val path = "transactions"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(TransactionRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val fsErr = error.unwrapFirestoreException()
                if (fsErr == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeUserNotifications(userId: String): Flow<List<AppNotification>> {
        val path = "notifications"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val fsErr = error.unwrapFirestoreException()
                if (fsErr == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeUserSupportTickets(userId: String): Flow<List<SupportTicket>> {
        val path = "support_tickets"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SupportTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val fsErr = error.unwrapFirestoreException()
                if (fsErr == null && error is CancellationException) throw error
                if (auth.currentUser?.uid == userId) {
                    handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                }
                throw error
            }
    }

    // --- User Write Actions ---

    suspend fun createRequest(
        userName: String,
        userEmail: String,
        type: String,
        title: String,
        description: String,
        amount: Double
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("requests").document()
        val payload = mapOf(
            "userId" to uid,
            "userName" to userName.trim().ifBlank { "Member" }.take(80),
            "userEmail" to userEmail.trim().take(120),
            "type" to type,
            "title" to title.trim().take(100),
            "description" to description.trim().take(500),
            "amount" to amount.coerceAtLeast(0.0),
            "status" to "pending",
            "adminNote" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("notifications").document(notificationId)
        return try {
            docRef.update("isRead", true).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createSupportTicket(
        userName: String,
        subject: String,
        message: String,
        priority: String
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("support_tickets").document()
        val payload = mapOf(
            "userId" to uid,
            "userName" to userName.trim().ifBlank { "Member" }.take(80),
            "subject" to subject.trim().take(120),
            "message" to message.trim().take(800),
            "priority" to priority,
            "status" to "open",
            "adminReply" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Admin Streams ---

    fun observeAllUsers(): Flow<List<UserProfile>> {
        val path = "users"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val fsErr = error.unwrapFirestoreException()
                if (fsErr == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeAllRequests(): Flow<List<PanelRequest>> {
        val path = "requests"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PanelRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val fsErr = error.unwrapFirestoreException()
                if (fsErr == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeAllTransactions(): Flow<List<TransactionRecord>> {
        val path = "transactions"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(TransactionRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val fsErr = error.unwrapFirestoreException()
                if (fsErr == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeAllSupportTickets(): Flow<List<SupportTicket>> {
        val path = "support_tickets"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SupportTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val fsErr = error.unwrapFirestoreException()
                if (fsErr == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                }
                throw error
            }
    }

    // --- Admin Actions ---

    suspend fun adminUpdateUserStatusAndAccess(
        targetUser: UserProfile,
        newStatus: String,
        newAccessLevel: String,
        newRole: String
    ): Result<Unit> {
        requireUserId()
        val userRef = db.collection("users").document(targetUser.userId)
        val notifRef = db.collection("notifications").document()
        return try {
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "status" to newStatus,
                    "accessLevel" to newAccessLevel,
                    "role" to newRole,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (newRole == "admin") {
                val adminRef = db.collection("admins").document(targetUser.userId)
                batch.set(
                    adminRef,
                    mapOf(
                        "adminId" to targetUser.userId,
                        "email" to targetUser.email,
                        "displayName" to targetUser.fullName,
                        "adminPinHash" to targetUser.pinHash,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.set(
                notifRef,
                mapOf(
                    "userId" to targetUser.userId,
                    "title" to "Account Access Updated",
                    "message" to "Your account status is now ${newStatus.uppercase()} with ${newAccessLevel.uppercase()} access.",
                    "category" to "security",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    suspend fun adminResolveRequest(
        request: PanelRequest,
        approve: Boolean,
        adminNote: String
    ): Result<Unit> {
        val adminUid = requireUserId()
        val reqRef = db.collection("requests").document(request.id)
        val newStatus = if (approve) "approved" else "rejected"
        val cleanNote = adminNote.trim().ifBlank { if (approve) "Approved by Admin" else "Rejected by Admin" }
        return try {
            val batch = db.batch()
            batch.update(
                reqRef,
                mapOf(
                    "status" to newStatus,
                    "adminNote" to cleanNote.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            if (approve && request.amount > 0.0 && (request.type == "deposit" || request.type == "withdrawal")) {
                val userRef = db.collection("users").document(request.userId)
                val userSnap = userRef.get().await()
                val currentBalance = userSnap.getDouble("walletBalance") ?: 0.0
                val isCredit = request.type == "deposit"
                val updatedBalance = if (isCredit) {
                    currentBalance + request.amount
                } else {
                    (currentBalance - request.amount).coerceAtLeast(0.0)
                }
                batch.update(
                    userRef,
                    mapOf(
                        "walletBalance" to updatedBalance,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                val txRef = db.collection("transactions").document()
                val refCode = "MHD-${UUID.randomUUID().toString().take(8).uppercase()}"
                batch.set(
                    txRef,
                    mapOf(
                        "userId" to request.userId,
                        "userName" to request.userName,
                        "referenceCode" to refCode,
                        "type" to if (isCredit) "credit" else "debit",
                        "category" to if (isCredit) "Deposit" else "Withdrawal",
                        "amount" to request.amount,
                        "status" to "completed",
                        "note" to cleanNote.take(300),
                        "createdByUid" to adminUid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            } else if (approve && request.type == "reactivation") {
                val userRef = db.collection("users").document(request.userId)
                batch.update(
                    userRef,
                    mapOf(
                        "status" to "active",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else if (approve && request.type == "access_upgrade") {
                val userRef = db.collection("users").document(request.userId)
                batch.update(
                    userRef,
                    mapOf(
                        "accessLevel" to "premium",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            val notifRef = db.collection("notifications").document()
            batch.set(
                notifRef,
                mapOf(
                    "userId" to request.userId,
                    "title" to "Request ${newStatus.replaceFirstChar { it.uppercase() }}",
                    "message" to "${request.title}: $cleanNote",
                    "category" to "request",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, reqRef.path)
            Result.failure(e)
        }
    }

    suspend fun adminRecordTransaction(
        targetUser: UserProfile,
        type: String,
        category: String,
        amount: Double,
        note: String
    ): Result<Unit> {
        val adminUid = requireUserId()
        if (amount <= 0.0) return Result.failure(IllegalArgumentException("Amount must be greater than 0"))
        val txRef = db.collection("transactions").document()
        val userRef = db.collection("users").document(targetUser.userId)
        val notifRef = db.collection("notifications").document()
        val refCode = "MHD-${UUID.randomUUID().toString().take(8).uppercase()}"
        return try {
            val userSnap = userRef.get().await()
            val currentBalance = userSnap.getDouble("walletBalance") ?: 0.0
            val newBalance = when (type) {
                "credit" -> currentBalance + amount
                "debit" -> (currentBalance - amount).coerceAtLeast(0.0)
                else -> currentBalance + amount
            }

            val batch = db.batch()
            batch.set(
                txRef,
                mapOf(
                    "userId" to targetUser.userId,
                    "userName" to targetUser.fullName.ifBlank { targetUser.email },
                    "referenceCode" to refCode,
                    "type" to type,
                    "category" to category.take(50),
                    "amount" to amount,
                    "status" to "completed",
                    "note" to note.trim().take(300),
                    "createdByUid" to adminUid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                userRef,
                mapOf(
                    "walletBalance" to newBalance,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                notifRef,
                mapOf(
                    "userId" to targetUser.userId,
                    "title" to "New ${category} Recorded",
                    "message" to "$refCode: ${type.uppercase()} of ₹${"%.2f".format(amount)}. ${note.trim()}",
                    "category" to "transaction",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, txRef.path)
            Result.failure(e)
        }
    }

    suspend fun adminSendNotification(
        targetUserId: String,
        title: String,
        message: String,
        category: String = "system"
    ): Result<Unit> {
        requireUserId()
        val notifRef = db.collection("notifications").document()
        return try {
            notifRef.set(
                mapOf(
                    "userId" to targetUserId,
                    "title" to title.trim().take(100),
                    "message" to message.trim().take(400),
                    "category" to category,
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, notifRef.path)
            Result.failure(e)
        }
    }

    suspend fun adminReplySupportTicket(
        ticket: SupportTicket,
        newStatus: String,
        reply: String
    ): Result<Unit> {
        requireUserId()
        val ticketRef = db.collection("support_tickets").document(ticket.id)
        val notifRef = db.collection("notifications").document()
        return try {
            val batch = db.batch()
            batch.update(
                ticketRef,
                mapOf(
                    "status" to newStatus,
                    "adminReply" to reply.trim().take(800),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                notifRef,
                mapOf(
                    "userId" to ticket.userId,
                    "title" to "Support Reply: ${ticket.subject.take(40)}",
                    "message" to reply.trim().take(300),
                    "category" to "system",
                    "isRead" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ticketRef.path)
            Result.failure(e)
        }
    }

    companion object {
        const val BOOTSTRAPPED_ADMIN_EMAIL = "nafeessaudagar7@gmail.com"
    }
}
