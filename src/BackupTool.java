import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;
import javax.crypto.Cipher;

public class BackupTool {
    public static void main(String[] args) throws Exception {
        String operation = args[0];
        if (!operation.equals("decrypt") && !operation.equals("encrypt")) {
            System.out.println("Usage: VivoDecryptor decrypt <inputFile> <outputFile>");
            return;
        }
        String inputFileName = args[1];
        String outputFileName = args[2];

        // Get these from the json metadata file
        String password = "vivovn123"; // Your text password
        String salt = "0/tlBrMm7zxYZX7pClVPowLW5dsGETaHPaSazBqisHE"; // d5
        String ivAndPwd = "QJ0cj05C6Q08JnfGz8Bz0gSezGXGFol+0xPiEIor/kW6jupBwNocpM+1MpV+bYAn/Te2ZnIOTYhhLPlE"; // d1
        byte[] deviceSri = Base64.getDecoder().decode(
                "KL7Y7qXxdm/ZMTa6UcG2KOBuy63X8j66zgQxp1WUb+5TEui4yLgAhv9ohT4X4NgpUQTgkY3qAXrMdQb1jCncXuM/+jDni0h5Sds+LNAC6Se3788tW8VvhT9m0VO4VbS7sEt9PatCgfIo/h7BiVqB7fYR1bimLwKxqffRHzK+fvEb6394KxJ0NRJCvhPK7iGrbhM4GJmQhyxAKKAgsd+QsXctuzDHLT8AdhvS18iGxhxrH5ylshzGdRTm6dRCAJSSOHxMXvTSMkqdKErx1iLGr0tlJk8rmXQbjnjo+6yAn5hR+0+O5iN91xNsdDNBSn6a0/6e1wtWyjbsoiZTephW5HYPN86fKmYRhJJMeWad7Qs"); // d3
        byte[] iv_bytes = Base64.getDecoder().decode("ZxDwzhSjZUOnTQW1"); // d2

        VivoDecryptor main = new VivoDecryptor();
        if (main.beginDecryption(password, salt, ivAndPwd, deviceSri, iv_bytes)) {
            System.out.println("Got password: " + main.mPwdWorker);
        } else {
            System.out.println("PwdWorker: null");
        }

        FileInputStream ifs = new FileInputStream(inputFileName);
        FileOutputStream ofs = new FileOutputStream(outputFileName);
        if (operation.equals("decrypt")) {
            DecryptedInputStream dis = new DecryptedInputStream(main, iv_bytes, ifs);

            // Read and write in chunks to avoid out of memory errors
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = dis.read(buffer)) != -1) {
                ofs.write(buffer, 0, bytesRead);
            }
            dis.close();
        } else if (operation.equals("encrypt")) {
            if (main.mPwdWorker != null) {
                System.out.println("Using existing keys from metadata...");
            } else {
                if (args.length > 3) {
                    password = args[3];
                }
                if (password.isEmpty()) {
                    System.out.println("Warning: using empty password. Provide 4th arg for password.");
                }

                System.out.println("Generating new keys...");
                // Generate distinct salt and worker pwd (using same random source provided by
                // getRandomWorkerPwd)
                String newSalt = VivoDecryptor.getRandomWorkerPwd(); // Use as salt
                String newWorkerPwd = VivoDecryptor.getRandomWorkerPwd();

                String strongHash = VivoDecryptor.generateStrongPasswordHash(password, newSalt, 10000, 32);
                String newIvAndPwd = VivoDecryptor.encryptPwd(newWorkerPwd, strongHash);

                // Generate device verification data
                byte[] deviceSriIV = VivoDecryptor.getRandomIv(12);

                main.mPwdWorker = newWorkerPwd;
                Cipher verificationCipher = main.getEncryptionCipher(deviceSriIV);
                byte[] newDeviceSri = verificationCipher.doFinal("VIVO_BACKUP_VERIFY".getBytes());

                System.out.println("--- NEW METADATA (Update your metadata file with these) ---");
                System.out.println("password: " + password);
                System.out.println("d5 (salt): " + newSalt);
                System.out.println("d1 (ivAndPwd): " + newIvAndPwd);
                System.out.println("d2 (iv_bytes): " + Base64.getEncoder().encodeToString(deviceSriIV));
                System.out.println("d3 (deviceSri): " + Base64.getEncoder().encodeToString(newDeviceSri));
                System.out.println("---------------------------------------------------------");
            }

            EncryptedOutputStream eos = new EncryptedOutputStream(main, ofs);
            byte[] buff = new byte[8192];
            int read;
            while ((read = ifs.read(buff)) != -1) {
                eos.write(buff, 0, read);
            }
            eos.close();
            System.out.println("Encryption complete.");
        }
        ifs.close();
        ofs.close();
    }
}