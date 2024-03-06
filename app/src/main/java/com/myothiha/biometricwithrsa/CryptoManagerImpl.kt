package com.myothiha.biometricwithrsa

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.gson.Gson
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * @Author myothiha
 * Created 04/03/2024 at 12:30 PM.
 **/
class CryptoManagerImpl : CryptoManager {
    private val KEY_SIZE = 256
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    private val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    private val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES

    private fun getOrGenerateSecretKey(keyName: String): SecretKey {
        // If Secretkey was previously created for that keyName, then grab and return it.
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        keyStore.getKey(keyName, null)?.let { return it as SecretKey }
        // if you reach here, then a new SecretKey must be generated for that keyName
        val paramBuilder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        paramBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(true)
        }

        val keyGenSpecs = paramBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
        )
        keyGenerator.init(keyGenSpecs)
        return keyGenerator.generateKey()
    }

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    override fun initializedCipherForEncryption(
        keyName: String
    ): Cipher {
        val cipher = getCipher()
        val secretKey = getOrGenerateSecretKey(keyName)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    override fun initializedCipherForDecryption(
        keyName: String,
        initializationVector: ByteArray
    ): Cipher {
        val cipher = getCipher()
        val secretKey = getOrGenerateSecretKey(keyName)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        return cipher
    }

    override fun encryptData(data: String, cipher: Cipher): CiphertextWrapper {
        val cipherText = cipher.doFinal(data.toByteArray(Charset.forName("UTF-8")))
        return CiphertextWrapper(cipherText = cipherText, initializationVector = cipher.iv)
    }

    override fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
        val data = cipher.doFinal(ciphertext)
        return String(data, Charset.forName("UTF-8"))
    }

    override fun saveEncryptedData(
        ciphertextWrapper: CiphertextWrapper,
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    ) {
        val json = Gson().toJson(ciphertextWrapper)
        context.getSharedPreferences(filename, mode).edit().putString(prefKey, json).apply()
    }

    override fun retrieveDecryptedData(
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    ): CiphertextWrapper? {
        val json =
            context.getSharedPreferences(filename, mode).getString(prefKey, null)
        return Gson().fromJson(json, CiphertextWrapper::class.java)
    }
}

fun CryptographyManager(): CryptoManager = CryptoManagerImpl()
