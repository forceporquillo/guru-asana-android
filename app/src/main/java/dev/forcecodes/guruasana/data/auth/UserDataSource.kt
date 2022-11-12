package dev.forcecodes.guruasana.data.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dev.forcecodes.guruasana.data.AuthenticatedUserInfoBasic
import dev.forcecodes.guruasana.data.GuruUserInfoBasic
import dev.forcecodes.guruasana.data.Result
import dev.forcecodes.guruasana.di.ApplicationScope
import dev.forcecodes.guruasana.logger.Logger
import dev.forcecodes.guruasana.utils.extensions.tryOffer
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
    fun signup(credentials: UserCredentials): Flow<Result<AuthenticatedUserInfoBasic>>
    fun login(credentials: UserCredentials): Flow<Result<AuthenticatedUserInfoBasic>>
}

interface Credentials {
    val username: String?
    val email: String?
    val password: String?
}

data class UserCredentials(
    override val username: String,
    override val email: String,
    override val password: String
) : Credentials

interface AuthStateListener

class UserAuthDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationScope private val externalScope: CoroutineScope
) : UserDataSource {

    override fun signup(credentials: UserCredentials): Flow<Result<AuthenticatedUserInfoBasic>> {
        return flow {
            emit(Result.Loading)
            val result = try {
                val (username, email, password) = credentials
                val data = auth.createUserWithEmailAndPassword(email, password)
                    .continueWith { setDisplayName(it, username) }
                    .continueWith {
                        firestore.collection("username")
                            .document(username)
                            .set(mapOf("email" to email), SetOptions.merge())
                        it.result
                    }.continueWith {
                        if (it.isSuccessful) {
                            Logger.d("Signing out user id: ${it.result?.user?.uid} after a successful account creation.".trim())
                            auth.signOut()
                        }
                        it.result
                    }
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

    override fun login(credentials: UserCredentials): Flow<Result<AuthenticatedUserInfoBasic>> {
        return flow {
            val (username, email, password) = credentials
            emit(Result.Loading)
            if (username.isNotEmpty()) {
                val task = firestore.collection("username")
                    .document(username)
                    .get()
                    .await()
                val emailStored = task.data?.get("email") as? String
                if (emailStored != null && emailStored.isNotEmpty()) {
                    emit(processAuth(emailStored, password))
                }
            }
            if (email.isNotEmpty()) {
                emit(processAuth(email, password))
            }
        }
    }

    private suspend fun processAuth(email: String, password: String): Result<GuruUserInfoBasic> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.Success(GuruUserInfoBasic(result.user))
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }

    private val authenticatedUserInfoBasic: SharedFlow<Result<AuthenticatedUserInfoBasic?>> =
        callbackFlow {
            val authStateListener: ((FirebaseAuth) -> Unit) = { auth ->
                tryOffer(auth)
            }
            auth.addAuthStateListener(authStateListener)
            awaitClose { auth.removeAuthStateListener(authStateListener) }
        }.map { authState ->
            Result.Success(GuruUserInfoBasic(authState.currentUser))
        }.shareIn(
            scope = externalScope,
            replay = 1,
            started = SharingStarted.WhileSubscribed()
        )
}