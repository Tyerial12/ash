package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PublicVaultRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun publishVaultAndObserveLeaderboard_authenticatedUser_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = PublicVaultRepository(firestore)

        val publishResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.publishOrUpdateVaultProfile(
                overseerName = "Overseer Alice",
                vaultName = "Vault 101",
                prestigeLevel = 2,
                commandLevel = 15,
                dayCounter = 32,
                totalSurvivors = 14,
                uniqueDwellersCount = 2,
                totalDefenseRating = 420
            )
        }
        assertTrue(publishResult.isSuccess)
        assertEquals(aliceUid, publishResult.getOrThrow())

        val vaults = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePublicVaults().first { list -> list.any { it.id == aliceUid } }
        }
        val aliceVault = vaults.first { it.id == aliceUid }
        assertEquals("Vault 101", aliceVault.vaultName)
        assertEquals(2L, aliceVault.prestigeLevel)
    }

    @Test
    fun createAndClaimSupplyDrop_authenticatedUsers_enforcesTerminalState() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = PublicVaultRepository(firestore)
        val dropId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createSupplyDrop(
                senderName = "Overseer Alice",
                resourceType = "RATIONS",
                amount = 25,
                message = "Surplus rations from Hydroponics!"
            ).getOrThrow()
        }
        assertTrue(dropId.isNotBlank())

        signInTestUser(BOB_EMAIL)
        val bobRepo = PublicVaultRepository(firestore)
        val claimResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.claimSupplyDrop(dropId = dropId, claimerName = "Overseer Bob")
        }
        assertTrue(claimResult.isSuccess)

        // Second claim attempt on an already-claimed drop must fail with PERMISSION_DENIED
        val secondClaimResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.claimSupplyDrop(dropId = dropId, claimerName = "Overseer Bob")
        }
        assertTrue(secondClaimResult.isFailure)
        val firestoreEx = secondClaimResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun updateAnotherUsersVault_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = PublicVaultRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.publishOrUpdateVaultProfile(
                overseerName = "Overseer Alice",
                vaultName = "Vault 101",
                prestigeLevel = 1,
                commandLevel = 8,
                dayCounter = 12,
                totalSurvivors = 6,
                uniqueDwellersCount = 1,
                totalDefenseRating = 180
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("public_vaults").document(aliceUid)
                    .update(
                        mapOf(
                            "vaultName" to "Spoofed Vault",
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    .await()
            }
            fail("Expected PERMISSION_DENIED when Bob attempts to modify Alice's public_vaults profile")
        } catch (e: Exception) {
            val fsEx = checkNotNull(e.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $e" }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx.code)
        }
    }

    @Test
    fun observePublicVaults_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = PublicVaultRepository(firestore)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observePublicVaults().first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated user")
        } catch (e: Exception) {
            val fsEx = checkNotNull(e.unwrapFirestoreException()) { "Expected nested FirebaseFirestoreException, got $e" }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@vaultcity.test"
        const val BOB_EMAIL = "bob@vaultcity.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
