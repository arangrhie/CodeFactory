package javax.arang.IO.bambasic;

import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import javax.arang.IO.basic.IOUtil;
import javax.arang.IO.zip.DeflaterFactory;
import javax.arang.samtools.util.BinaryCodec;
import javax.arang.samtools.util.BlockCompressedFilePointerUtil;
import javax.arang.samtools.util.Constants;

public class BamMaker {
	String path;
	String dir;
	String filename;
	private final BinaryCodec codec;
	private final Deflater deflater;
	private final byte[] uncompressedBlock = new byte[Constants.DEFAULT_UNCOMPRESSED_BLOCK_SIZE];
	byte[] compressedBlock = new byte[Constants.MAX_COMPRESSED_BLOCK_SIZE - Constants.BLOCK_HEADER_LENGTH];
    private final CRC32 crc32 = new CRC32();
    private int numUncompressedBytes = 0;
    private long mBlockAddress = 0;
    
	/***
	 * A small version of BAM format writer.
	 * @param path
	 */
    public BamMaker(String path) {
    	this.path = path;
    	dir = IOUtil.retrieveDirectory(path);
    	filename = IOUtil.retrieveFileName(path);
    	File newfile = new File(dir);
    	newfile.mkdirs();
    	newfile = new File(dir+"/"+filename);
    	deflater = DeflaterFactory.makeDeflater(true);
    	codec = new BinaryCodec(newfile, true);
    }
    
    public void write(final byte[] bytes) {
    	write(bytes, 0, bytes.length);
    }
    
    public void write(final byte[] bytes, int startIndex, int numBytes) {
        while (numBytes > 0) {
            final int bytesToWrite = Math.min(uncompressedBlock.length - numUncompressedBytes, numBytes);
            System.arraycopy(bytes, startIndex, uncompressedBlock, numUncompressedBytes, bytesToWrite);
            numUncompressedBytes += bytesToWrite;
            startIndex += bytesToWrite;
            numBytes -= bytesToWrite;
            if (numUncompressedBytes == uncompressedBlock.length) {
                deflateBlock();
            }
        }
    }
	
	private int deflateBlock() {
		if (numUncompressedBytes == 0) {
            return 0;
        }
		int bytesToCompress = numUncompressedBytes;
		deflater.reset();
        deflater.setInput(uncompressedBlock, 0, uncompressedBlock.length);
        deflater.finish();
		int compressedBlockSize = deflater.deflate(compressedBlock, 0, compressedBlock.length);
		
		// Data compressed small enough, so write it out.
        crc32.reset();
        crc32.update(uncompressedBlock, 0, bytesToCompress);

        final int totalBlockSize = writeGzipBlock(compressedBlockSize, bytesToCompress, crc32.getValue());

        // Clear out from uncompressedBuffer the data that was written
        if (bytesToCompress == numUncompressedBytes) {
        	numUncompressedBytes = 0;
        } else {
        	System.arraycopy(uncompressedBlock, bytesToCompress, compressedBlock, 0,
        			numUncompressedBytes - bytesToCompress);
        	numUncompressedBytes -= bytesToCompress;
        }
        mBlockAddress += totalBlockSize;
        return totalBlockSize;
	}
	
    /** Encode virtual file pointer
     * Upper 48 bits is the byte offset into the compressed stream of a block.
     * Lower 16 bits is the byte offset into the uncompressed stream inside the block.
     */
    public long getFilePointer(){
        return BlockCompressedFilePointerUtil.makeFilePointer(mBlockAddress, numUncompressedBytes);
    }
	
    /**
     * Writes the entire gzip block, assuming the compressed data is stored in compressedBuffer
     * @return  size of gzip block that was written.
     */
    private int writeGzipBlock(final int compressedSize, final int uncompressedSize, final long crc) {
        // Init gzip header
    	codec.writeByte(Constants.GZIP_ID1);
    	codec.writeByte(Constants.GZIP_ID2);
    	codec.writeByte(Constants.GZIP_CM_DEFLATE);
    	codec.writeByte(Constants.GZIP_FLG);
    	codec.writeInt(0); // Modification time
    	codec.writeByte(Constants.GZIP_XFL);
    	codec.writeByte(Constants.GZIP_OS_UNKNOWN);
    	codec.writeShort(Constants.GZIP_XLEN);
    	codec.writeByte(Constants.BGZF_ID1);
    	codec.writeByte(Constants.BGZF_ID2);
    	codec.writeShort(Constants.BGZF_LEN);
        final int totalBlockSize = compressedSize + Constants.BLOCK_HEADER_LENGTH +
        		Constants.BLOCK_FOOTER_LENGTH;

        // I don't know why we store block size - 1, but that is what the spec says
        codec.writeShort((short)(totalBlockSize - 1));
        codec.writeBytes(compressedBlock, 0, compressedSize);
        codec.writeInt((int)crc);
        codec.writeInt(uncompressedSize);
        return totalBlockSize;
    }
    
    public void flush() {
        while (numUncompressedBytes > 0) {
            deflateBlock();
        }
        try {
			codec.getOutputStream().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public void closeMaker() {
		flush();
		codec.writeBytes(Constants.EMPTY_GZIP_BLOCK);
		codec.close();
	}

	public String getFileName() {
		return filename;
	}
	
	public String getDir() {
		return dir;
	}
	
	public String getFullPath() {
		return path;
	}
	
	
}
