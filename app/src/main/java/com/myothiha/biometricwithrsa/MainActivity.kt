package com.myothiha.biometricwithrsa

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.myothiha.biometricwithrsa.ui.theme.BiometricWithRSATheme

const val SHARED_PREFS_FILENAME = "biometric_prefs"
const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"
const val SECRET_KEY = "I am Liam97"

class MainActivity : FragmentActivity() {
    private var privateKey =""
    private lateinit var biometricPrompt: BiometricPrompt
    private val cryptoManager = CryptographyManager()
    private val ciphertextWrapper
        get() = cryptoManager.retrieveDecryptedData(
            applicationContext,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            CIPHERTEXT_WRAPPER
        )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometricWithRSATheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Greeting(name = "Myo Thiha", modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            if (ciphertextWrapper == null) {
                                showBiometricPromptForEncryption()
                            } else {
                                showBiometricPromptForDecryption()
                            }

                        })
                }
            }
        }
    }


    private fun showBiometricPromptForEncryption() {
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val cipher = cryptoManager.initializedCipherForEncryption(keyName = SECRET_KEY)
            biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(this, ::encryptAndStoreData)
            val promptInfo = BiometricPromptUtils.createPromptInfo()
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun showBiometricPromptForDecryption() {
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            ciphertextWrapper?.let { ciphertextWrapper ->
                val cipher = cryptoManager.initializedCipherForDecryption(
                    keyName = SECRET_KEY,
                    initializationVector = ciphertextWrapper.initializationVector
                )

                biometricPrompt =
                    BiometricPromptUtils.createBiometricPrompt(this, ::decryptAndRetrieveData)

                val promptInfo = BiometricPromptUtils.createPromptInfo()
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }

    }

    private fun encryptAndStoreData(authResult: BiometricPrompt.AuthenticationResult) {
        val keyPair = RSAHelper.generateKeyPair()
        val publicKey =
            "-----BEGIN PUBLIC KEY-----\n${RSAHelper.printPublicKey(keyPair.public)}\n-----END PUBLIC KEY-----"

       privateKey = RSAHelper.printPrivateKey(keyPair.private)
        val privateKeyString =
            "-----BEGIN RSA PRIVATE KEY-----\n${RSAHelper.printPrivateKey(keyPair.private)}\n-----END RSA PRIVATE KEY-----"
        Log.d("PUBLIC", "$publicKey")
        Log.d("PPIVATEKEY", "$privateKeyString")

        authResult.cryptoObject?.cipher?.let {
            val encryptedPrivateKey = cryptoManager.encryptData(data = privateKey, cipher = it)
            cryptoManager.saveEncryptedData(
                encryptedPrivateKey,
                applicationContext,
                SHARED_PREFS_FILENAME,
                Context.MODE_PRIVATE,
                CIPHERTEXT_WRAPPER
            )
            Toast.makeText(
                this@MainActivity,
                "Private Key : ${encryptedPrivateKey.cipherText}",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    private fun decryptAndRetrieveData(authResult: BiometricPrompt.AuthenticationResult) {
        ciphertextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let { cipher ->
                val plaintext =
                    cryptoManager.decryptData(ciphertext = textWrapper.cipherText, cipher = cipher)
                Log.d("P_DEC", "$plaintext")
                val privateKey = RSAHelper.stringToPrivateKey(plaintext)
                val sign = RSAHelper.signKey(
                    message = "I am Myo Thiha".toByteArray(),
                    privateKey = privateKey
                )
                Log.d(
                    "P_SIGN",
                    "${Base64.encodeToString(sign, Base64.DEFAULT)}"
                )


            }
        }
    }

}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(
            text = "Hello $name!",
            modifier = Modifier.align(alignment = Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BiometricWithRSATheme {
        Greeting("Android")
    }
}
