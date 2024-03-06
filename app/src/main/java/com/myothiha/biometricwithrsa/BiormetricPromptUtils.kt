package com.myothiha.biometricwithrsa

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * @Author myothiha
 * Created 03/03/2024 at 2:13 PM.
 **/

object BiometricPromptUtils {
    private const val TAG = "BiometricPromptUtils"
    fun createBiometricPrompt(
        activity: FragmentActivity,
        processSuccess: (BiometricPrompt.AuthenticationResult) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                Log.d(TAG, "errCode is $errCode and errString is: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "User biometric rejected.")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication was successful")
                processSuccess(result)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    fun createPromptInfo(): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle("Register your biometric")
            setDescription("Place your finger on the fingerprint sensor to authenticate.")
            setConfirmationRequired(false)
            setNegativeButtonText("CANCEL")
        }.build()
}