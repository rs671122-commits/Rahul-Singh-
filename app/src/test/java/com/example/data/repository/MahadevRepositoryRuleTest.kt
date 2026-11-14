package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class MahadevRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun registerProfile_and_createRequest_succeedsForOwner() = runBlocking {
        val uid = signInTestUser("user_${UUID.randomUUID()}@test.com")
        val repo = MahadevRepository(firestore)

        val regResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.registerUserProfile(
                fullName = "Arjun Rao",
                email = "arjun@test.com",
                phone = "9876543210",
                rawPin = "1234",
                otpVerified = true
            )
        }
        assertTrue("Registration should succeed: ${regResult.exceptionOrNull()}", regResult.isSuccess)

        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(uid).first { it != null }
        }
        assertNotNull(profile)
        assertEquals("Arjun Rao", profile?.fullName)

        val reqResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createRequest(
                userName = "Arjun Rao",
                userEmail = "arjun@test.com",
                type = "deposit",
                title = "Initial Deposit",
                description = "UPI Ref 4455",
                amount = 1500.0
            )
        }
        assertTrue("Request creation should succeed: ${reqResult.exceptionOrNull()}", reqResult.isSuccess)
        val reqId = reqResult.getOrThrow()

        val requests = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserRequests(uid).first { list -> list.any { it.id == reqId } }
        }
        assertTrue(requests.any { it.id == reqId && it.status == "pending" })
    }

    @Test
    fun observeUserRequests_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = MahadevRepository(firestore)

        var caught: Exception? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeUserRequests("some_uid_123").first()
            }
        } catch (e: Exception) {
            caught = e
        }
        val fsEx = caught?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException, got $caught", fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun observeAllUsers_nonAdmin_failsWithPermissionDenied() = runBlocking {
        signInTestUser("regular_${UUID.randomUUID()}@test.com")
        val repo = MahadevRepository(firestore)

        var caught: Exception? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeAllUsers().first()
            }
        } catch (e: Exception) {
            caught = e
        }
        val fsEx = caught?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException, got $caught", fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
