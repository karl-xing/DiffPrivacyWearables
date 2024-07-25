package com.example.diffprivacywearables

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.fitness.FitnessOptions
import kotlinx.coroutines.*

class SignInGoogle(
    private val context: Context,
    private val signInLauncher: ActivityResultLauncher<Intent>,
    private val onSignInSuccess: (GoogleSignInAccount) -> Unit,
    private val onSignInTimeout: () -> Unit
) {
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(com.google.android.gms.fitness.data.DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .build()

    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("1002620828847-7bhp6u2im9st49fsqk63bqi883s3o16d.apps.googleusercontent.com")
                .build()
        )
    }

    private var signInJob: Job? = null

    fun requestGoogleSignIn(timeoutMillis: Long = 60000) {
        signInJob?.cancel()
        signInJob = CoroutineScope(Dispatchers.Main).launch {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                signInLauncher.launch(googleSignInClient.signInIntent)
                withTimeoutOrNull(timeoutMillis) {
                    delay(timeoutMillis)
                }
                onSignInTimeout()
            } else {
                onSignInSuccess(account)
            }
        }
    }

    fun handleSignInResult(result: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result)
            val account = task.getResult(ApiException::class.java)
            if (account != null && GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                signInJob?.cancel()
                onSignInSuccess(account)
            } else {
                requestGoogleSignIn()
            }
        } catch (e: ApiException) {
            Log.e("SignInGoogle", "Sign-in failed", e)
            requestGoogleSignIn()
        }
    }
}
