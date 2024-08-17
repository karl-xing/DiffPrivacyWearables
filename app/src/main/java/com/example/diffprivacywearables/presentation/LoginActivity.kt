@file:Suppress("DEPRECATION")

package com.example.diffprivacywearables.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.example.diffprivacywearables.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.example.diffprivacywearables.presentation.theme.DiffPrivacyWearablesTheme


class LoginActivity : ComponentActivity() {
    private val TAG = "GoogleFit"
    private val RC_SIGN_IN = 9001
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiffPrivacyWearablesTheme {
                Scaffold(
                    timeText = {
                        TimeText()
                    },
                    vignette = {
                        Vignette(vignettePosition = VignettePosition.TopAndBottom)
                    }
                ) {
                    LoginScreen()
                }
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestScopes(
                Scope("https://www.googleapis.com/auth/fitness.activity.read"),
                Scope("https://www.googleapis.com/auth/fitness.heart_rate.read")
            )
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    @Composable
    fun LoginScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Google Fit Reader")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    signIn()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "SignIn Google")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Go to Home")
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "signInResult: success, account: ${account?.email}")
            Toast.makeText(this, "Sign-In Successful: ${account?.email}", Toast.LENGTH_LONG).show()
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, "Sign-In Failed", Toast.LENGTH_LONG).show()
        }
    }
}
