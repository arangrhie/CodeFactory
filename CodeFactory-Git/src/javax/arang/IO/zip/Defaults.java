package javax.arang.IO.zip;

public class Defaults {
	 /** Should BAM index files be created when writing out coordinate sorted BAM files?  Default = false. */
    public static final boolean CREATE_INDEX;

    /** Should MD5 files be created when writing out SAM and BAM files?  Default = false. */
    public static final boolean CREATE_MD5;

    /** Should asynchronous I/O be used when writing out SAM and BAM files (one thread per file).  Default = false. */
    public static final boolean USE_ASYNC_IO;

    /** Compresion level to be used for writing BAM and other block-compressed outputs.  Default = 5. */
    public static final int COMPRESSION_LEVEL;

    /** Buffer size, in bytes, used whenever reading/writing files or streams.  Default = 128k. */
    public static final int BUFFER_SIZE;

    /** Should BlockCompressedOutputStream attempt to load libIntelDeflater? */
    public static final boolean TRY_USE_INTEL_DEFLATER;

    /** Path to libIntelDeflater.so.  If this is not set, the library is looked for in the directory
     * where the executable jar lives. */
    public static final String INTEL_DEFLATER_SHARED_LIBRARY_PATH;

    static {
        CREATE_INDEX      = getBooleanProperty("create_index", false);
        CREATE_MD5        = getBooleanProperty("create_md5", false);
        USE_ASYNC_IO      = getBooleanProperty("use_async_io", false);
        COMPRESSION_LEVEL = getIntProperty("compression_level", 5);
        BUFFER_SIZE       = getIntProperty("buffer_size", 1024 * 128);
        TRY_USE_INTEL_DEFLATER = getBooleanProperty("try_use_intel_deflater", true);
        INTEL_DEFLATER_SHARED_LIBRARY_PATH = getStringProperty("intel_deflater_so_path", null);
    }

    /** Gets a string system property, prefixed with "samjdk." using the default if the property does not exist.*/
    private static String getStringProperty(final String name, final String def) {
        return System.getProperty("samjdk." + name, def);
    }

    /** Gets a boolean system property, prefixed with "samjdk." using the default if the property does not exist.*/
    private static boolean getBooleanProperty(final String name, final boolean def) {
        final String value = getStringProperty(name, new Boolean(def).toString());
        return Boolean.parseBoolean(value);
    }

    /** Gets an int system property, prefixed with "samjdk." using the default if the property does not exist.*/
    private static int getIntProperty(final String name, final int def) {
        final String value = getStringProperty(name, new Integer(def).toString());
        return Integer.parseInt(value);
    }
}
