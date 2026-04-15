import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class VivoDecryptor {
    public String mPwdWorker;
    public static final String AES_ALGORITHM = "AES";
    public static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";

    private static String getSalt() {
        byte[] bArr = new byte[32];
        new SecureRandom().nextBytes(bArr);
        return Base64.getEncoder().encodeToString(bArr);
    }
    
    public static String getRandomWorkerPwd() throws Exception {
        return getSalt();
    }
    
    public static byte[] getRandomIv() {
        return getRandomIv(12);
    }
    
    public static byte[] getRandomIv(int i2) {
        if (i2 < 0) {
            i2 = 12;
        }
        byte[] bArr = new byte[i2];
        new SecureRandom().nextBytes(bArr);
        return bArr;
    }
    
    public static String decryptPwd(String iv_base64, String strongPasswordHash) throws Exception {
        byte[] arrayOfByte3 = Base64.getDecoder().decode(iv_base64);
        byte[] iv_buf = new byte[12];
        int i = arrayOfByte3.length - 12;
        byte[] arrayOfByte2 = new byte[i];
        System.arraycopy(arrayOfByte3, 0, iv_buf, 0, 12);
        System.arraycopy(arrayOfByte3, 12, arrayOfByte2, 0, i);
        SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(strongPasswordHash),
                VivoDecryptor.AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(VivoDecryptor.AES_TRANSFORMATION);
        cipher.init(2, secretKeySpec, new GCMParameterSpec(128, iv_buf));
        iv_buf = cipher.doFinal(arrayOfByte2);
        return Base64.getEncoder().encodeToString(iv_buf);
    }

    public static String encryptPwd(String workerPwd, String backupPwd) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(backupPwd), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        byte[] randomIv = getRandomIv();
        cipher.init(1, secretKeySpec, new GCMParameterSpec(128, randomIv));
        byte[] bArrDoFinal = cipher.doFinal(Base64.getDecoder().decode(workerPwd));
        byte[] bArr = new byte[randomIv.length + bArrDoFinal.length];
        System.arraycopy(randomIv, 0, bArr, 0, randomIv.length);
        System.arraycopy(bArrDoFinal, 0, bArr, randomIv.length, bArrDoFinal.length);
        return Base64.getEncoder().encodeToString(bArr);
    }

    public static String generateStrongPasswordHash(String password, String salt, int pbkdf2Iters, int pbkdf2KeyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec pBEKeySpec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), pbkdf2Iters,
                pbkdf2KeyLength * 8);
        byte[] arrayOfByte = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pBEKeySpec)
                .getEncoded();
        return Base64.getEncoder().encodeToString(arrayOfByte);
    }

    public byte[] decryptByteArray(String paramString, byte[] iv_buf, byte[] ciphertext_bytes) throws Exception {
        if (paramString != null) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(paramString),
                    VivoDecryptor.AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(VivoDecryptor.AES_TRANSFORMATION);
            cipher.init(2, secretKeySpec, new GCMParameterSpec(128, iv_buf));
            return cipher.doFinal(ciphertext_bytes);
        }
        throw new Exception("decryptByteArray: pwd is null");
    }

    public boolean beginDecryption(String password, String salt, String ivAndPasswd, byte[] ciphertext_bytes,
            byte[] iv_with_data) {
        try {
            password = VivoDecryptor.decryptPwd(ivAndPasswd,
                    VivoDecryptor.generateStrongPasswordHash(password, salt, 10000, 32));
            decryptByteArray(password, iv_with_data, ciphertext_bytes);
            this.mPwdWorker = password;
            return true;
        } catch (Exception exception) {
            // Log.e("VivoDecryptor", "isPwdCorrect: " + exception);
            System.out.println("isPwdCorrect: " + exception);
            return false;
        }
    }

    public Cipher getDecryptionCipher(byte[] bArr) throws Exception {
        if (this.mPwdWorker == null) {
            throw new Exception("Please call beginDecryption first");
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(this.mPwdWorker),
                VivoDecryptor.AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(VivoDecryptor.AES_TRANSFORMATION);
        cipher.init(2, secretKeySpec, new GCMParameterSpec(128, bArr));
        return cipher;
    }
    public Cipher getEncryptionCipher(byte[] iv) throws Exception {
        if (this.mPwdWorker == null) {
            throw new Exception("Worker password is null");
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(this.mPwdWorker),
                AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new GCMParameterSpec(128, iv));
        return cipher;
    }
}
