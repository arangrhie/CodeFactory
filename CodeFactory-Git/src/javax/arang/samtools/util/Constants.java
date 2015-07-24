package javax.arang.samtools.util;


public class Constants {
	
	 // Number of bytes in the gzip block before the deflated data.
    // This is not the standard header size, because we include one optional subfield,
    // but it is the standard for us.
	public static final int BLOCK_HEADER_LENGTH = 18;
	
	// Number of bytes that follow the deflated data
	public static final int BLOCK_FOOTER_LENGTH = 8;
	
    // Magic numbers
    public static final byte GZIP_ID1 = 31;
    public static final int GZIP_ID2 = 139;

    // FEXTRA flag means there are optional fields
    public static final int GZIP_FLG = 4;

    // extra flags
    public static final int GZIP_XFL = 0;

    // length of extra subfield
    public static final short GZIP_XLEN = 6;

    // The deflate compression, which is customarily used by gzip
    public static final byte GZIP_CM_DEFLATE = 8;

    // We don't care about OS because we're not doing line terminator translation
    public static final int GZIP_OS_UNKNOWN = 255;

    // The subfield ID
    public static final byte BGZF_ID1 = 66;
    public static final byte BGZF_ID2 = 67;

    // subfield length in bytes
    public static final byte BGZF_LEN = 2;
    
    // We require that a compressed block (including header and footer, be <= this)
    public static final int MAX_COMPRESSED_BLOCK_SIZE = 64 * 1024;

    // Gzip overhead is the header, the footer, and the block size (encoded as a short).
    public static final int GZIP_OVERHEAD = BLOCK_HEADER_LENGTH + BLOCK_FOOTER_LENGTH + 2;

    // If Deflater has compression level == NO_COMPRESSION, 10 bytes of overhead (determined experimentally).
    public static final int NO_COMPRESSION_OVERHEAD = 10;

    // Push out a gzip block when this many uncompressed bytes have been accumulated.
    // This size is selected so that if data is not compressible,  if Deflater is given
    // compression level == NO_COMPRESSION, compressed size is guaranteed to be <= MAX_COMPRESSED_BLOCK_SIZE.
    public static final int DEFAULT_UNCOMPRESSED_BLOCK_SIZE = 64 * 1024 - (GZIP_OVERHEAD + NO_COMPRESSION_OVERHEAD);
	
//    public static final byte[] EMPTY_GZIP_BLOCK = {
//    	GZIP_ID1,
//    	(byte) GZIP_ID2,
//    	GZIP_CM_DEFLATE,
//    	GZIP_FLG,
//    	0, 0, 0, 0, // Modification time
//    	GZIP_XFL,
//    	(byte) GZIP_OS_UNKNOWN,
//    	GZIP_XLEN, 0, // Little-endian short
//    	BGZF_ID1,
//    	BGZF_ID2,
//    	BGZF_LEN, 0, // Little-endian short
//    	// Total block size - 1
//    	BLOCK_HEADER_LENGTH + BLOCK_FOOTER_LENGTH - 1 + 2, 0, // Little-endian short
//    	// Dummy payload?
//    	3, 0,
//    	0, 0, 0, 0, // crc
//    	0, 0, 0, 0, // uncompressedSize
//    };
    
    public static final byte[] EMPTY_GZIP_BLOCK = {
    	0x1f, (byte) 0x8b, 0x8, 0x4, 0x0, 0x0, 0x0, 0x0, 0x0, (byte) 0xff, 0x6, 0x0, 0x42, 0x43, 0x2, 0x0, 0x1b, 0x0, 
    	0x3, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
    };
}
