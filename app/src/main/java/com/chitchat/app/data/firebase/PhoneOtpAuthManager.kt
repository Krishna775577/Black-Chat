package com.chitchat.app.data.firebase

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneOtpAuthManager {
    private fun authOrNull(): FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var activePhoneNumber: String = ""

    fun startVerification(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onAutoVerified: (PhoneAuthCredential) -> Unit,
        onTimeout: () -> Unit = {},
        onError: (Throwable) -> Unit,
    ) {
        val auth = authOrNull() ?: run {
            onError(IllegalStateException("Firebase Auth is not ready. Check google-services.json and Firebase initialization."))
            return
        }
        activePhoneNumber = phoneNumber
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInOrLink(credential, onSuccess = { onAutoVerified(credential) }, onError = onError)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                onError(exception)
            }

            override fun onCodeSent(
                newVerificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                verificationId = newVerificationId
                resendToken = token
                onCodeSent(newVerificationId)
            }

            override fun onCodeAutoRetrievalTimeOut(expiredVerificationId: String) {
                verificationId = expiredVerificationId
                onTimeout()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setActivity(activity)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(
        code: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val currentVerificationId = verificationId ?: run {
            onError(IllegalStateException("Verification ID missing. Start verification first."))
            return
        }
        if (code.length != 6) {
            onError(IllegalArgumentException("Enter the 6-digit OTP."))
            return
        }
        val credential = PhoneAuthProvider.getCredential(currentVerificationId, code)
        signInOrLink(credential, onSuccess = onSuccess, onError = onError)
    }

    fun resendCode(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onTimeout: () -> Unit = {},
        onError: (Throwable) -> Unit,
    ) {
        val auth = authOrNull() ?: run {
            onError(IllegalStateException("Firebase Auth is not ready. Check google-services.json and Firebase initialization."))
            return
        }
        val token = resendToken ?: run {
            onError(IllegalStateException("Resend token unavailable. Start verification first."))
            return
        }
        activePhoneNumber = phoneNumber
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) = Unit

            override fun onVerificationFailed(exception: FirebaseException) {
                onError(exception)
            }

            override fun onCodeSent(
                newVerificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                verificationId = newVerificationId
                resendToken = token
                onCodeSent(newVerificationId)
            }

            override fun onCodeAutoRetrievalTimeOut(expiredVerificationId: String) {
                verificationId = expiredVerificationId
                onTimeout()
            }
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setActivity(activity)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun reset() {
        verificationId = null
        resendToken = null
        activePhoneNumber = ""
    }

    fun activePhoneNumber(): String = activePhoneNumber

    private fun signInOrLink(
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val auth = authOrNull() ?: run {
            onError(IllegalStateException("Firebase Auth is not ready. Check google-services.json and Firebase initialization."))
            return
        }
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.isAnonymous) {
            currentUser.linkWithCredential(credential)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { error ->
                    if (error is FirebaseAuthUserCollisionException) {
                        auth.signInWithCredential(credential)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener(onError)
                    } else {
                        onError(error)
                    }
                }
            return
        }
        auth.signInWithCredential(credential)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error ->
                if (error is FirebaseAuthInvalidCredentialsException) {
                    onError(IllegalArgumentException("Wrong OTP. Please check the code and try again."))
                } else {
                    onError(error)
                }
            }
    }
}
