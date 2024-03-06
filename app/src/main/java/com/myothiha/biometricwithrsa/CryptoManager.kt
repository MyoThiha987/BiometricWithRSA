package com.myothiha.biometricwithrsa

import android.content.Context
import javax.crypto.Cipher

/**
 * @Author myothiha
 * Created 04/03/2024 at 12:29 PM.
 **/
interface CryptoManager {
    fun initializedCipherForEncryption(keyName: String): Cipher
    fun initializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher
    fun encryptData(data: String, cipher: Cipher): CiphertextWrapper
    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String
    fun saveEncryptedData(
        ciphertextWrapper: CiphertextWrapper,
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    )

    fun retrieveDecryptedData(
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    ): CiphertextWrapper?

}

data class CiphertextWrapper(val cipherText: ByteArray, val initializationVector: ByteArray)