import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/* loaded from: classes2.dex */
public class DecryptedInputStream extends InputStream {
    protected static final int BUFFER_SIZE = 3276800;
    protected static final String TAG = "DecryptedInputStream";
    long item;
    protected VivoDecryptor mDecryptor;
    protected InputStream mInputStream;
    protected byte[] mIv;
    protected byte[] mOutputBuffer;
    protected byte[] mBuffer = new byte[3276816];
    protected int readIndex = -1;
    protected int readLimit = -1;
    protected boolean isOver = false;
    protected byte[] headLengthBytes = new byte[4];
    protected byte[] ivBytes = new byte[12];

    private void ensureFileLength() {
    }

    public DecryptedInputStream(VivoDecryptor vivoDecryptor, byte[] bArr, InputStream inputStream) {
        this.mDecryptor = vivoDecryptor;
        this.mIv = bArr;
        this.mInputStream = inputStream;
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        int readInner = readInner();
        if (this.isOver) {
            // Log.e(TAG, "isOver after read " + this.readIndex + " " + this.readLimit);
            System.err.println("isOver after read " + this.readIndex + " " + this.readLimit);
            this.mBuffer = null;
            this.mOutputBuffer = null;
        }
        return readInner;
    }

    public int readInner() throws IOException {
        if (this.isOver) {
            // Log.e(TAG, "isOver " + this.readIndex + " " + this.readLimit);
            System.err.println("isOver " + this.readIndex + " " + this.readLimit);
            return -1;
        }
        int readIndex = this.readIndex;
        if (readIndex != -1 && readIndex < this.readLimit) {
            this.readIndex = readIndex + 1;
            int i3 = get(readIndex);
            if (i3 == -1) {
                // Log.e(TAG, "cache " + this.readIndex + " " + this.readLimit);
                System.err.println("cache " + this.readIndex + " " + this.readLimit);
            }
            return i3;
        }
        Arrays.fill(this.headLengthBytes, (byte) 0);
        int headBytesRead = this.mInputStream.read(this.headLengthBytes);
        if (headBytesRead < this.headLengthBytes.length) {
            this.isOver = true;
            return -1;
        }
        int ivBytesRead = this.mInputStream.read(this.ivBytes);
        if (ivBytesRead < this.ivBytes.length) {
            this.isOver = true;
            return -1;
        }
        int i4 = BytesUtils.toInt(this.headLengthBytes);
        byte[] bArr = this.mBuffer;
        if (bArr.length < i4) {
            this.mBuffer = new byte[i4];
        } else {
            Arrays.fill(bArr, (byte) 0);
        }

        int encryptedBytesRead = 0;
        try {
            encryptedBytesRead = this.mInputStream.read(this.mBuffer, 0, i4);
        } catch (IndexOutOfBoundsException e2) {
            encryptedBytesRead = this.mInputStream.read(this.mBuffer);
        }
        this.readLimit = encryptedBytesRead;
        this.readIndex = 0;
        if (encryptedBytesRead == -1) {
            this.isOver = true;
            // Log.e(TAG, "cache final real end " + this.readIndex + " " + this.readLimit);
            System.err.println("cache final real end " + this.readIndex + " " + this.readLimit);
            return -1;
        }
        readBuffer();
        if (this.isOver) {
            // Log.e(TAG, "cache final isOver " + this.readIndex + " " + this.readLimit);
            System.err.println("cache final isOver " + this.readIndex + " " + this.readLimit);
            return -1;
        }
        int i5 = this.readIndex;
        this.readIndex = i5 + 1;
        int i6 = get(i5);
        if (i6 == -1) {
            // Log.e(TAG, "cache final " + this.readIndex + " " + this.readLimit);
            System.err.println("cache final " + this.readIndex + " " + this.readLimit);
        }
        return i6;
    }

    private int get(int i2) {
        return this.mOutputBuffer[i2] & 0xFF;
    }

    public void readBuffer() {
        byte[] bArr;
        VivoDecryptor vivoDecryptor = this.mDecryptor;
        if (vivoDecryptor == null || (bArr = this.ivBytes) == null) {
            return;
        }
        try {
            byte[] doFinal = vivoDecryptor.getDecryptionCipher(bArr).doFinal(this.mBuffer, 0, this.readLimit);
            this.mOutputBuffer = doFinal;
            if (doFinal == null) {
                this.isOver = true;
                return;
            }
            int length = doFinal.length;
            this.readLimit = length;
            this.item += length;
            // Log.e(TAG, "readBuffer normal readLimit " + this.readLimit + " " + this.item + " " + this.ivBytes.length);
            System.err.println("readBuffer normal readLimit " + this.readLimit + " " + this.item + " " + this.ivBytes.length);
        } catch (Throwable th) {
            this.isOver = true;
            // Log.e(TAG, "readBuffer " + th.getMessage() + " readLimit " + this.readLimit + " " + this.item + " " + this.ivBytes.length);
            System.err.println("readBuffer " + th.getMessage() + " readLimit " + this.readLimit + " " + this.item + " " + this.ivBytes.length);
        }
    }

    private String toStringBuffer(byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b2 : bArr) {
            stringBuffer.append((int) b2).append(",");
        }
        return stringBuffer.toString();
    }

    @Override // java.io.InputStream
    public int read(byte[] bArr) throws IOException {
        return super.read(bArr);
    }

    @Override // java.io.InputStream
    public int read(byte[] bArr, int i2, int i3) throws IOException {
        return super.read(bArr, i2, i3);
    }

    @Override // java.io.InputStream
    public long skip(long j2) throws IOException {
        return super.skip(j2);
    }

    @Override // java.io.InputStream
    public int available() throws IOException {
        int available = this.mInputStream.available();
        int i2 = (available / 3276832) * 3276800;
        int i3 = available % 3276832;
        int i4 = i3 - 32;
        int i5 = i4 > 0 ? i2 + i4 : i2 + i3;
        // Log.i(TAG, "available " + available + " reduce " + i5);
        System.out.println("available " + available + " reduce " + i5);
        return i5;
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        try {
            this.mInputStream.close();
        } catch (Throwable th) {
            // Log.e(TAG, "forceClosed " + th.getMessage());
            System.err.println("forceClosed " + th.getMessage());
        }
        this.mInputStream = null;
        this.mBuffer = null;
        this.mOutputBuffer = null;
    }

    @Override // java.io.InputStream
    public void mark(int i2) {
        this.mInputStream.mark(i2);
    }

    @Override // java.io.InputStream
    public void reset() throws IOException {
        this.mInputStream.reset();
    }

    @Override // java.io.InputStream
    public boolean markSupported() {
        return this.mInputStream.markSupported();
    }

    /**
     * Reads all remaining bytes from this {@code InputStream} and returns them as a byte array.
     * This method will block until all bytes have been read or an I/O error occurs.
     *
     * @return a byte array containing all the remaining bytes from this input stream.
     * Returns an empty byte array if this input stream reaches the end before reading any bytes.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] readAllBytes() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[BUFFER_SIZE];
        while ((nRead = this.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}