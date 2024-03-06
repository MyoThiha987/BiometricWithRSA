package com.myothiha.biometricwithrsa

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

/**
 * @Author myothiha
 * Created 03/03/2024 at 3:00 PM.
 **/

object RSAHelper {
    private const val ALGORITHM = "RSA"


    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
        keyPairGenerator.initialize(2048) // You can adjust the key size as needed
        return keyPairGenerator.generateKeyPair()
    }

    fun printPublicKey(publicKey: PublicKey): String {
        return String(Base64.encode(publicKey.encoded, Base64.DEFAULT))
    }

    fun printPrivateKey(privateKey: PrivateKey): String {
        val keySpec = PKCS8EncodedKeySpec(privateKey.encoded)
        val keyFactory = KeyFactory.getInstance(privateKey.algorithm)
        val pkcs8EncodedBytes = keyFactory.generatePrivate(keySpec).encoded
        return Base64.encodeToString(pkcs8EncodedBytes, Base64.DEFAULT)
    }


    fun signKey(message: ByteArray, privateKey: PrivateKey): ByteArray {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(message)
        return signature.sign()
    }

    fun verifySignature(message: ByteArray, signature: ByteArray, publicKey: PublicKey): Boolean {
        val verifier = Signature.getInstance("SHA256withRSA")
        verifier.initVerify(publicKey)
        verifier.update(message)
        return verifier.verify(signature)
    }

    /*** Converts String Public Key to PublicKey Object ***/


    /*** Converts String Private Key to PrivateKey Object ***/
    //@Throws(InvalidKeySpecException::class, NoSuchAlgorithmException::class)
    fun stringToPrivateKey(privateKeyString: String): PrivateKey {
        // Decode the Base64-encoded string (if encoded)
        val decodedKeyBytes = Base64.decode(privateKeyString, Base64.DEFAULT)

        // Parse the decoded bytes into a PrivateKey object
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(decodedKeyBytes)
        return keyFactory.generatePrivate(keySpec)
    }

}