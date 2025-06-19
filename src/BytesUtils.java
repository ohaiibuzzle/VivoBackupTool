public class BytesUtils {
    public static final int INTEGER_SIZE = 4;
    public static final int LONG_SIZE = 8;

    public static byte[] toBytes(int i2) {
        byte[] bArr = new byte[4];
        for (int i3 = 0; i3 < 4; i3++) {
            bArr[i3] = (byte) (i2 >> ((3 - i3) * 8));
        }
        return bArr;
    }

    public static int toInt(byte[] bArr) {
        if (bArr == null) {
            return 0;
        }
        int i2 = 0;
        for (int i3 = 0; i3 < 4 && i3 < bArr.length; i3++) {
            i2 |= (bArr[i3] & -1) << ((3 - i3) * 8);
        }
        return i2;
    }

    public static byte[] toBytes(long j2) {
        byte[] bArr = new byte[8];
        for (int i2 = 0; i2 < 8; i2++) {
            bArr[i2] = (byte) (j2 >> ((7 - i2) * 8));
        }
        return bArr;
    }

    public static long toLong(byte[] bArr) {
        long j2 = 0;
        if (bArr == null) {
            return 0L;
        }
        for (int i2 = 0; i2 < 8 && i2 < bArr.length; i2++) {
            j2 |= (bArr[i2] & 255) << ((7 - i2) * 8);
        }
        return j2;
    }
}