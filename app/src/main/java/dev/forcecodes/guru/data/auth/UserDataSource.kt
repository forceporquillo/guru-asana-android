package dev.forcecodes.guru.data.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.userProfileChangeRequest
import dev.forcecodes.guru.data.AuthenticatedUserInfoBasic
import dev.forcecodes.guru.data.GuruUserInfoBasic
import dev.forcecodes.guru.data.Result
import dev.forcecodes.guru.di.ApplicationScope
import dev.forcecodes.guru.utils.extensions.tryOffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface UserDataSource {
    suspend fun signup(credentials: UserCredentials): Flow<Result<AuthenticatedUserInfoBasic>>
    suspend fun login(credentials: UserCredentials)
}

data class UserCredentials(
    val username: String, val email: String, val password: String
)

class UserAuthDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @ApplicationScope
    private val externalScope: CoroutineScope
) : UserDataSource {

    override suspend fun signup(credentials: UserCredentials): Flow<Result<AuthenticatedUserInfoBasic>> {
        return flow {
            emit(Result.Loading)
            val result = try {
                val (username, email, password) = credentials
                val data = firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .continueWith { setDisplayName(it, username) }
                    .await()
                Result.Success(GuruUserInfoBasic(data?.user))
            } catch (e: FirebaseAuthException) {
                Result.Error(e.message)
            }
            emit(result)
        }
    }

    private fun setDisplayName(task: Task<AuthResult>, username: String): AuthResult? {
        val changeRequest = userProfileChangeRequest { displayName = username }
        task.result.user?.updateProfile(changeRequest)
        return task.result
    }

    override suspend fun login(credentials: UserCredentials) {

    }

    private val authenticatedUserInfoBasic: SharedFlow<Result<AuthenticatedUserInfoBasic?>> =
        callbackFlow {
            val authStateListener: ((FirebaseAuth) -> Unit) = { auth -> tryOffer(auth) }

            firebaseAuth.addAuthStateListener(authStateListener)

            awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }

        }.map { auth ->
           Result.Success(GuruUserInfoBasic(auth.currentUser))
        }.shareIn(
            scope = externalScope,
            replay = 1,
            started = SharingStarted.WhileSubscribed()
        )
}