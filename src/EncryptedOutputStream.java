import java.io.IOException;
import java.io.OutputStream;
import javax.crypto.Cipher;

public class EncryptedOutputStream extends OutputStream {
    private static final int BUFFER_SIZE = 3276800;
    private final OutputStream out;
    private final VivoDecryptor decryptor;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private int count = 0;
    private boolean closed = false;

    public EncryptedOutputStream(VivoDecryptor decryptor, OutputStream out) {
        this.decryptor = decryptor;
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        if (count >= BUFFER_SIZE) {
            flushBuffer();
        }
        buffer[count++] = (byte) b;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int space = BUFFER_SIZE - count;
            int toWrite = Math.min(len, space);
            System.arraycopy(b, off, buffer, count, toWrite);
            count += toWrite;
            off += toWrite;
            len -= toWrite;
            if (count >= BUFFER_SIZE) {
                flushBuffer();
            }
        }
    }

    private void flushBuffer() throws IOException {
        if (count == 0) return;
        try {
            byte[] iv = VivoDecryptor.getRandomIv(12);
            Cipher cipher = decryptor.getEncryptionCipher(iv);
            byte[] ciphertext = cipher.doFinal(buffer, 0, count);
            
            // Format: [Length (4)][IV (12)][Ciphertext (N)]
            
            out.write(BytesUtils.toBytes(ciphertext.length));
            out.write(iv);
            out.write(ciphertext);
            
            count = 0;
        } catch (Exception e) {
            throw new IOException("Encryption failed", e);
        }
    }

    @Override
    public void close() throws IOException {
         if (!closed) {
             flushBuffer();
             out.close();
             closed = true;
         }
    }
}
