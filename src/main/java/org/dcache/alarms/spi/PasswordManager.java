/*
COPYRIGHT STATUS:
Dec 1st 2001, Fermi National Accelerator Laboratory (FNAL) documents and
software are sponsored by the U.S. Department of Energy under Contract No.
DE-AC02-76CH03000. Therefore, the U.S. Government retains a  world-wide
non-exclusive, royalty-free license to publish or reproduce these documents
and software for U.S. Government purposes.  All documents and software
available from this server are protected under the U.S. and Foreign
Copyright Laws, and FNAL reserves all rights.

Distribution of the software available from this server is free of
charge subject to the user following the terms of the Fermitools
Software Legal Information.

Redistribution and/or modification of the software shall be accompanied
by the Fermitools Software Legal Information  (including the copyright
notice).

The user is asked to feed back problems, benefits, and/or suggestions
about the software to the Fermilab Software Providers.

Neither the name of Fermilab, the  URA, nor the names of the contributors
may be used to endorse or promote products derived from this software
without specific prior written permission.

DISCLAIMER OF LIABILITY (BSD):

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED  WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED  WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL FERMILAB,
OR THE URA, OR THE U.S. DEPARTMENT of ENERGY, OR CONTRIBUTORS BE LIABLE
FOR  ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
OF SUBSTITUTE  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY  OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT  OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE  POSSIBILITY OF SUCH DAMAGE.

Liabilities of the Government:

This software is provided by URA, independent from its Prime Contract
with the U.S. Department of Energy. URA is acting independently from
the Government and in its own private capacity and is not acting on
behalf of the U.S. Government, nor as its contractor nor its agent.
Correspondingly, it is understood and agreed that the U.S. Government
has no connection to this software and in no manner whatsoever shall
be liable for nor assume any responsibility or obligation for any claim,
cost, or damages arising out of or resulting from the use of the software
available from this server.

Export Control:

All documents and software available from this server are subject to U.S.
export control laws.  Anyone downloading information from this server is
obligated to secure any necessary Government licenses before exporting
documents or software obtained from this server.
 */
package org.dcache.alarms.spi;

import com.google.common.base.Preconditions;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.dcache.util.Args;

/**
 * <p>Utility for encrypting and decrypting the account password (RSA).</p>
 *
 * <p>Generated keys must be in the DER (PKCS8) format.  This means that
 *    if the keys were generated using ssh-keygen, the -m option needs to
 *    have been applied.</p>
 *
 * <p>The bin/encrypt.sh script is provided to run this class.  bin/keygen.sh
 *    can be used to create the necessary keypair.</p>
 */
public class PasswordManager {
    static final String DEFAULT_PUBLIC_KEY  = "/etc/dcache/admin/alarms_key.pub";

    public static void main(String[] args) throws Exception {
        Args options = new Args(args);
        File file = new File(options.getOption("key", DEFAULT_PUBLIC_KEY));
        System.out.println("Please enter the password to encrypt:");
        String password = new String(System.console().readPassword());
        String encrypted = encrypt(password, file);
        System.out.println("Encrypted password:");
        System.out.println(encrypted);
    }

    static String decrypt(String encrypted, File keyFile, boolean encryptedKey)
                    throws NoSuchPaddingException, NoSuchAlgorithmException,
                    InvalidKeyException, BadPaddingException,
                    IllegalBlockSizeException, InvalidKeySpecException,
                    IOException, InvalidAlgorithmParameterException {
        Preconditions.checkNotNull(encrypted, "password was null.");
        Cipher decrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decrypt.init(Cipher.DECRYPT_MODE, getPrivateKey(keyFile, encryptedKey));
        byte[] decrypted = decrypt.doFinal(Base64.getDecoder()
                                                 .decode(encrypted.getBytes()));
        return new String(decrypted);
    }

    private static String encrypt(String cleartext, File keyFile)
                    throws NoSuchPaddingException, NoSuchAlgorithmException,
                    InvalidKeyException, BadPaddingException,
                    IllegalBlockSizeException, InvalidKeySpecException,
                    IOException {
        Preconditions.checkNotNull(cleartext, "password was null.");
        Cipher encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encrypt.init(Cipher.ENCRYPT_MODE, getPublicKey(keyFile));
        byte[] encrypted = encrypt.doFinal(cleartext.getBytes());
        return new String(Base64.getEncoder().encode(encrypted));
    }

    private static PrivateKey getEncryptedPrivateKey(byte[] keyBytes)
                    throws NoSuchAlgorithmException, InvalidKeySpecException,
                    IOException, NoSuchPaddingException,
                    InvalidAlgorithmParameterException, InvalidKeyException {
        EncryptedPrivateKeyInfo keyInfo = new EncryptedPrivateKeyInfo(keyBytes);
        Cipher cipher = Cipher.getInstance(keyInfo.getAlgName());
        /*
         *  Assumes key encrypted using no password.
         */
        PBEKeySpec pbeKeySpec = new PBEKeySpec("".toCharArray());
        SecretKeyFactory secretKeyFactory
                        = SecretKeyFactory.getInstance(keyInfo.getAlgName());
        Key pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);
        AlgorithmParameters algorithmParameters = keyInfo.getAlgParameters();
        cipher.init(Cipher.DECRYPT_MODE, pbeKey, algorithmParameters);
        KeySpec pkcs8KeySpec = keyInfo.getKeySpec(cipher);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(pkcs8KeySpec);
    }

    private static PrivateKey getPrivateKey(File keyFile, boolean isEncrypted)
                    throws IOException, InvalidKeySpecException,
                    InvalidAlgorithmParameterException,
                    NoSuchAlgorithmException, InvalidKeyException,
                    NoSuchPaddingException {
        byte[] keyBytes = readKey(keyFile);
        if (isEncrypted) {
            return getEncryptedPrivateKey(keyBytes);
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private static PublicKey getPublicKey(File keyFile)
                    throws NoSuchAlgorithmException, InvalidKeySpecException,
                    IOException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(readKey(keyFile)));
    }

    private static byte[] readKey(File keyFile) throws IOException {
        DataInputStream dis = null;
        byte[] keyBytes;
        try {
            FileInputStream fis = new FileInputStream(keyFile);
            dis = new DataInputStream(fis);
            keyBytes = new byte[(int) keyFile.length()];
            dis.readFully(keyBytes);
            return keyBytes;
        } finally {
            if (dis != null) {
                dis.close();
            }
        }
    }
}
