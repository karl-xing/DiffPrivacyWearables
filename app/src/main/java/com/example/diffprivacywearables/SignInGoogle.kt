package com.example.diffprivacywearables

import android.content.Context
import android.util.Log
//import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SignInGoogle(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onGoogleIdTokenCredentialUpdated: (GoogleIdTokenCredential?) -> Unit
) {

    private val credentialManager = CredentialManager.create(context)

    private val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId("1002620828847-7bhp6u2im9st49fsqk63bqi883s3o16d.apps.googleusercontent.com")
        .build()

    private val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    fun signUpWithGoogle() {
        signUpCredentialManager()
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        onGoogleIdTokenCredentialUpdated(googleIdTokenCredential)
                        Log.d("SignInGoogle", "Successfully signed in with Google ID token")
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("SignInGoogle", "Received an invalid google id token response", e)
                        onGoogleIdTokenCredentialUpdated(null)
                    }
                } else {
                    Log.e("SignInGoogle", "Unexpected type of credential")
                    onGoogleIdTokenCredentialUpdated(null)
                }
            }
            else -> {
                Log.e("SignInGoogle", "Unexpected type of credential")
                onGoogleIdTokenCredentialUpdated(null)
            }
        }
    }

    private fun signUpCredentialManager() {
        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e("SignInGoogle", "Error getting credential: ${e.message}", e)
                onGoogleIdTokenCredentialUpdated(null)
            }
        }
    }
}
