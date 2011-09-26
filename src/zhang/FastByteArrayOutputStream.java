package zhang;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A faster ByteArrayInputStream, used for serializing objects. Copied from
 * <http>http://javatechniques.com/blog/faster-deep-copies-of-java-objects/</http>.
 */
public class FastByteArrayOutputStream extends OutputStream {

    /**
     * Buffer and size
     */
    protected byte[] buf;
    protected int size = 0;

    public FastByteArrayOutputStream() {
        this(5 * 1024);
    }

    public FastByteArrayOutputStream(int initSize) {
        this(new byte[initSize]);
    }

    public FastByteArrayOutputStream(byte[] buf) {
        super();
        this.buf = buf;
    }

    /**
     * Ensures that we have a large enough buffer for the given size.
     */
    private void verifyBufferSize(int sz) {
        if (sz > buf.length) {
            byte[] old = buf;
            buf = new byte[Math.max(sz, 2 * buf.length)];
            System.arraycopy(old, 0, buf, 0, old.length);
            old = null;
        }
    }

    /**
     * The number of used bytes in the array.
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the byte array containing the written data. Note that this
     * array will almost always be larger than the amount of data actually
     * written.
     */
    public byte[] getByteArray() {
        return buf;
    }

    @Override
    public final void write(byte[] b) {
        verifyBufferSize(size + b.length);
        System.arraycopy(b, 0, buf, size, b.length);
        size += b.length;
    }

    @Override
    public final void write(byte[] b, int off, int len) {
        verifyBufferSize(size + len);
        System.arraycopy(b, off, buf, size, len);
        size += len;
    }

    public final void write(int b) {
        verifyBufferSize(size + 1);
        buf[size++] = (byte) b;
    }

    public void reset() {
        size = 0;
    }

    /**
     * Returns a ByteArrayInputStream for reading back the written data
     */
    public InputStream getInputStream() {
        return new FastByteArrayInputStream(buf, size);
    }

    /**
     * Copied from java.io.ByteArrayOutputStream, line 147-164
     * Converts the buffer's contents into a string decoding bytes using the
     * platform's default character set. The length of the new <tt>String</tt>
     * is a function of the character set, and hence may not be equal to the
     * size of the buffer.
     *
     * <p> This method always replaces malformed-input and unmappable-character
     * sequences with the default replacement string for the platform's
     * default character set. The {@linkplain java.nio.charset.CharsetDecoder}
     * class should be used when more control over the decoding process is
     * required.
     *
     * @return String decoded from the buffer's contents.
     * @since  JDK1.1
     */
    public synchronized String toString() {
	return new String(buf, 0, size);
    }
}
