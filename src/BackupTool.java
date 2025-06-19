import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;

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
        String password = ""; // Your text password
        String salt = ""; // d5
        String ivAndPwd = ""; // d1
        byte[] deviceSri = Base64.getDecoder().decode(""); // d3
        byte[] iv_bytes = Base64.getDecoder().decode(""); // d2

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
            ifs.close();
            ofs.close();

            throw new UnsupportedOperationException("Encryption not implemented in this example");
        }
        ifs.close();
        ofs.close();
    }
}