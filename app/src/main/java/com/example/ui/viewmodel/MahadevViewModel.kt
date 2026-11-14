package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.SecurityCrypto
import com.example.core.unwrapFirestoreException
import com.example.data.model.AdminRecord
import com.example.data.model.AppNotification
import com.example.data.model.PanelRequest
import com.example.data.model.SupportTicket
import com.example.data.model.TransactionRecord
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.repository.MahadevRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OtpChallenge(
    val code: String,
    val expiresAtMillis: Long,
    val destinationHint: String
)

enum class PanelMode {
    USER,
    ADMIN
}

@OptIn(ExperimentalCoroutinesApi::class)
class MahadevViewModel(
    private val repository: MahadevRepository,
    val currentUserId: String,
    val authEmail: String,
    val authDisplayName: String
) : ViewModel() {

    val userProfileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val fsErr = error.unwrapFirestoreException()
            if (fsErr == null && error is CancellationException) throw error
            val root = fsErr ?: error
            Log.w(TAG, "Error observing profile", root)
            emit(UiState.Error(root.message ?: "Failed to load profile"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    private val adminRecordFlow: Flow<AdminRecord?> = repository.observeAdminRecord(currentUserId)
        .catch { error ->
            val fsErr = error.unwrapFirestoreException()
            if (fsErr == null && error is CancellationException) throw error
            emit(null)
        }

    val adminRecord: StateFlow<AdminRecord?> = adminRecordFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    val isUserAdmin: StateFlow<Boolean> = combine(userProfileState, adminRecord) { profileState, adminRec ->
        val profileRole = (profileState as? UiState.Success)?.data?.isAdminRole == true
        repository.isBootstrappedAdmin() || profileRole || adminRec != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), repository.isBootstrappedAdmin())

    val userRequestsState: StateFlow<UiState<List<PanelRequest>>> =
        wrapListStream(repository.observeUserRequests(currentUserId), "requests")

    val userTransactionsState: StateFlow<UiState<List<TransactionRecord>>> =
        wrapListStream(repository.observeUserTransactions(currentUserId), "transactions")

    val userNotificationsState: StateFlow<UiState<List<AppNotification>>> =
        wrapListStream(repository.observeUserNotifications(currentUserId), "notifications")

    val userSupportTicketsState: StateFlow<UiState<List<SupportTicket>>> =
        wrapListStream(repository.observeUserSupportTickets(currentUserId), "support tickets")

    // Admin streams are only attached once isUserAdmin evaluates to true
    val allUsersState: StateFlow<UiState<List<UserProfile>>> = isUserAdmin
        .flatMapLatest { isAdmin ->
            if (!isAdmin) flowOf(UiState.Success(emptyList()))
            else wrapListFlow(repository.observeAllUsers(), "all users")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val allRequestsState: StateFlow<UiState<List<PanelRequest>>> = isUserAdmin
        .flatMapLatest { isAdmin ->
            if (!isAdmin) flowOf(UiState.Success(emptyList()))
            else wrapListFlow(repository.observeAllRequests(), "all requests")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val allTransactionsState: StateFlow<UiState<List<TransactionRecord>>> = isUserAdmin
        .flatMapLatest { isAdmin ->
            if (!isAdmin) flowOf(UiState.Success(emptyList()))
            else wrapListFlow(repository.observeAllTransactions(), "all transactions")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val allSupportTicketsState: StateFlow<UiState<List<SupportTicket>>> = isUserAdmin
        .flatMapLatest { isAdmin ->
            if (!isAdmin) flowOf(UiState.Success(emptyList()))
            else wrapListFlow(repository.observeAllSupportTickets(), "all support tickets")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    private val _panelMode = MutableStateFlow(PanelMode.USER)
    val panelMode: StateFlow<PanelMode> = _panelMode.asStateFlow()

    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    private val _activeOtpChallenge = MutableStateFlow<OtpChallenge?>(null)
    val activeOtpChallenge: StateFlow<OtpChallenge?> = _activeOtpChallenge.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun switchPanelMode(mode: PanelMode) {
        _panelMode.value = mode
    }

    fun lockAdminConsole() {
        _isAdminUnlocked.value = false
        _panelMode.value = PanelMode.USER
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun generateOtpChallenge(hint: String) {
        val code = SecurityCrypto.generateOtpCode()
        _activeOtpChallenge.value = OtpChallenge(
            code = code,
            expiresAtMillis = System.currentTimeMillis() + 5 * 60_000L,
            destinationHint = hint
        )
    }

    fun clearOtpChallenge() {
        _activeOtpChallenge.value = null
    }

    fun verifyOtpCode(enteredCode: String): Boolean {
        val challenge = _activeOtpChallenge.value ?: return false
        if (System.currentTimeMillis() > challenge.expiresAtMillis) {
            _actionMessage.value = "OTP expired. Please request a fresh verification code."
            return false
        }
        if (enteredCode.trim() == challenge.code) {
            _activeOtpChallenge.value = null
            return true
        }
        _actionMessage.value = "Invalid OTP code. Please double-check and try again."
        return false
    }

    fun verifyOrSetAdminPin(rawPin: String) {
        val trimmed = rawPin.trim()
        if (trimmed.length < 4) {
            _actionMessage.value = "Admin PIN must be at least 4 digits."
            return
        }
        val existingHash = adminRecord.value?.adminPinHash?.takeIf { it.isNotBlank() }
            ?: (userProfileState.value as? UiState.Success)?.data?.pinHash?.takeIf { it.isNotBlank() }

        if (existingHash != null) {
            if (SecurityCrypto.verifySecret(trimmed, existingHash)) {
                _isAdminUnlocked.value = true
                _panelMode.value = PanelMode.ADMIN
                _actionMessage.value = "Admin Console unlocked."
            } else {
                _actionMessage.value = "Incorrect Admin PIN."
            }
        } else {
            viewModelScope.launch {
                _isBusy.value = true
                val res = repository.saveAdminPin(
                    displayName = authDisplayName.ifBlank { "Administrator" },
                    email = authEmail,
                    rawPin = trimmed
                )
                _isBusy.value = false
                res.onSuccess {
                    _isAdminUnlocked.value = true
                    _panelMode.value = PanelMode.ADMIN
                    _actionMessage.value = "Encrypted Admin PIN saved & Console unlocked."
                }.onFailure {
                    _actionMessage.value = it.localizedMessage ?: "Failed to save Admin PIN."
                }
            }
        }
    }

    fun registerProfile(fullName: String, phone: String, rawPin: String, otpVerified: Boolean) {
        if (fullName.trim().length < 2) {
            _actionMessage.value = "Full name must be at least 2 characters."
            return
        }
        if (rawPin.trim().length < 4) {
            _actionMessage.value = "Security PIN must be at least 4 digits."
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.registerUserProfile(
                fullName = fullName,
                email = authEmail.ifBlank { "member@mahadevpanel.app" },
                phone = phone,
                rawPin = rawPin.trim(),
                otpVerified = otpVerified
            )
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = "Profile activated with SHA-256 PIN encryption."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Registration failed."
            }
        }
    }

    fun updateProfile(
        fullName: String,
        phone: String,
        otpVerified: Boolean,
        newRawPin: String?,
        existingPinHash: String
    ) {
        if (fullName.trim().length < 2) {
            _actionMessage.value = "Full name must be at least 2 characters."
            return
        }
        if (!newRawPin.isNullOrBlank() && newRawPin.trim().length < 4) {
            _actionMessage.value = "New PIN must be at least 4 digits."
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.updateOwnProfile(
                fullName = fullName,
                phone = phone,
                otpVerified = otpVerified,
                newRawPin = newRawPin?.trim(),
                existingPinHash = existingPinHash
            )
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = "Profile updated securely."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Profile update failed."
            }
        }
    }

    fun markOtpVerifiedOnProfile(profile: UserProfile) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.updateOwnProfile(
                fullName = profile.fullName,
                phone = profile.phone,
                otpVerified = true,
                newRawPin = null,
                existingPinHash = profile.pinHash
            )
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = "OTP verified! Account security upgraded."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Failed to verify OTP."
            }
        }
    }

    fun submitRequest(
        profile: UserProfile,
        type: String,
        title: String,
        description: String,
        amountText: String
    ) {
        if (title.trim().length < 3) {
            _actionMessage.value = "Request title must be at least 3 characters."
            return
        }
        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.createRequest(
                userName = profile.fullName,
                userEmail = profile.email,
                type = type,
                title = title,
                description = description,
                amount = parsedAmount
            )
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = "Request submitted for Admin approval."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Failed to submit request."
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(notificationId)
        }
    }

    fun submitSupportTicket(
        profile: UserProfile,
        subject: String,
        message: String,
        priority: String
    ) {
        if (subject.trim().length < 3 || message.trim().length < 5) {
            _actionMessage.value = "Please enter a valid subject and detailed message."
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.createSupportTicket(
                userName = profile.fullName,
                subject = subject,
                message = message,
                priority = priority
            )
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = "Support ticket opened."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Failed to submit ticket."
            }
        }
    }

    // Admin actions
    fun adminUpdateUser(
        targetUser: UserProfile,
        newStatus: String,
        newAccessLevel: String,
        newRole: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.adminUpdateUserStatusAndAccess(targetUser, newStatus, newAccessLevel, newRole)
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = "${targetUser.fullName} updated to ${newStatus.uppercase()}."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Failed to update user."
            }
        }
    }

    fun adminResolveRequest(request: PanelRequest, approve: Boolean, note: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.adminResolveRequest(request, approve, note)
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = if (approve) "Request approved & ledger updated." else "Request rejected."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Failed to resolve request."
            }
        }
    }

    fun adminRecordTransaction(
        targetUser: UserProfile,
        type: String,
        category: String,
        amountText: String,
        note: String
    ) {
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _actionMessage.value = "Enter a valid positive amount."
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.adminRecordTransaction(targetUser, type, category, amount, note)
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = "Transaction recorded in ledger."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Failed to record transaction."
            }
        }
    }

    fun adminSendNotification(targetUserId: String, title: String, message: String) {
        if (title.trim().length < 2 || message.trim().length < 2) {
            _actionMessage.value = "Enter both notification title and message."
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.adminSendNotification(targetUserId, title, message)
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = "Notification sent to user."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Failed to send notification."
            }
        }
    }

    fun adminReplyTicket(ticket: SupportTicket, newStatus: String, reply: String) {
        if (reply.trim().isEmpty()) {
            _actionMessage.value = "Please write a reply message."
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.adminReplySupportTicket(ticket, newStatus, reply)
            _isBusy.value = false
            res.onSuccess {
                _actionMessage.value = "Support ticket updated."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Failed to update support ticket."
            }
        }
    }

    private fun <T> wrapListFlow(source: Flow<List<T>>, label: String): Flow<UiState<List<T>>> =
        source
            .map<List<T>, UiState<List<T>>> { UiState.Success(it) }
            .catch { error ->
                val fsErr = error.unwrapFirestoreException()
                if (fsErr == null && error is CancellationException) throw error
                val root = fsErr ?: error
                Log.w(TAG, "Error observing $label", root)
                emit(UiState.Error(root.message ?: "Failed to load $label"))
            }

    private fun <T> wrapListStream(source: Flow<List<T>>, label: String): StateFlow<UiState<List<T>>> =
        wrapListFlow(source, label)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    private companion object {
        const val TAG = "MahadevVM"
        const val STOP_TIMEOUT_MS = 5000L
    }
}
