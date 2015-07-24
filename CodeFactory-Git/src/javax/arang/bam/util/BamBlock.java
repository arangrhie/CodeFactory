/**
 * 
 */
package javax.arang.bam.util;

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.arang.IO.bambasic.BinaryUtil;
import javax.arang.samtools.util.Constants;

/**
 * @author Arang Rhie
 *
 */
public class BamBlock {

   

    private int compressedBlockSize = -1;
    private long CRC32 = 0;
    private int uncompressedBlockSize = 0;
    private byte[] uncompressedBlock = null;
    private int truncatedByteSize = 0;
    
	private int offsetInUncompressedBlock = -1;
	private int block_size = 0;

	private int l_text;
	private int n_ref;
	
    
    public BamBlock() {
    	
    }
    
    public BamBlock(byte[] truncatedRecord, int remainingByteSize) {
    	this.truncatedByteSize = truncatedRecord.length;
    	uncompressedBlockSize = truncatedByteSize + remainingByteSize;
    	uncompressedBlock = new byte[uncompressedBlockSize];
    	for (int i = 0; i < truncatedByteSize; i++) {
    		uncompressedBlock[i] = truncatedRecord[i];
    	}
    	offsetInUncompressedBlock = 0;
//    	System.out.println(":: DEBUG :: uncompressedBlockSize: " + uncompressedBlockSize);
//    	System.out.print(":: DEBUG :: BamBlock(byte[] truncatedRecord, int remainingByteSize) ");
		setBlockSize();
    }
    
    public void printRecord() {
    	System.out.println(":: DEBUG :: printRecord() ");
    	parseAlignments(0);
    	System.out.println();
    }
	
	public boolean setHeader(byte[] header) {
		if (Constants.GZIP_ID1 == BinaryUtil.toUnsignedInt(header[0])
				&& Constants.GZIP_ID2 == BinaryUtil.toUnsignedInt(header[1])
				&& Constants.GZIP_FLG == BinaryUtil.toUnsignedInt(header[3])
				&& Constants.GZIP_OS_UNKNOWN == BinaryUtil.toUnsignedInt(header[9])
				&& Constants.BGZF_ID1 == BinaryUtil.toUnsignedInt(header[12])
				&& Constants.BGZF_ID2 == BinaryUtil.toUnsignedInt(header[13])) {
			byte[] bsize = new byte[2];
			bsize[0] = header[16];
			bsize[1] = header[17];
			compressedBlockSize = (int) BinaryUtil.toUnsignedInt(bsize) + 1 - 18 - 8;
			//System.out.println(":: DEBUG :: Header is valid. Compressed Block Size: " + compressedBlockSize);
			return true;
		}
		System.out.println("GZIP_ID1 " + BinaryUtil.toUnsignedInt(header[0]));
		System.out.println("GZIP_ID2 " + BinaryUtil.toUnsignedInt(header[1]));
		System.out.println("GZIP_FLG " + BinaryUtil.toUnsignedInt(header[3]));
		System.out.println("GZIP_OS_UNKNOWN " + BinaryUtil.toUnsignedInt(header[9]));
		System.out.println("BGZF_ID1 " + BinaryUtil.toUnsignedInt(header[12]));
		System.out.println("BGZF_ID2 " + BinaryUtil.toUnsignedInt(header[13]));
		return false;
	}


	/**
	 * @return
	 */
	public int getCompressedBlockSize() {
		// TODO Auto-generated method stub
		return compressedBlockSize;
	}

	/**
	 * @param compressedBlock
	 * @throws IOException 
	 */
	public void uncompressBlock(byte[] compressedBlock) throws DataFormatException, IOException {
		final Inflater inflater = new Inflater(true); // GZIP mode
		inflater.reset();
		inflater.setInput(compressedBlock);
		//System.out.println(":: DEBUG :: Uncompressed Block Size: " + uncompressedBlockSize);
		if (uncompressedBlockSize == 0) {
			throw new IOException("EOF signal");
		}
		uncompressedBlock = new byte[uncompressedBlockSize];
		int inflatedBytes = inflater.inflate(uncompressedBlock, 0, uncompressedBlockSize);
		if (inflatedBytes != uncompressedBlockSize) {
			System.err.println(":: ERROR ::  Did not inflate expected amount");
		}
		final CRC32 crc32 = new CRC32();
		crc32.reset();
		crc32.update(uncompressedBlock, 0, uncompressedBlockSize);
		final long crc = crc32.getValue();
        if (crc != this.CRC32) {
        	System.err.println(":: ERROR :: CRC mismatch");
        }
        offsetInUncompressedBlock = 0;
        //System.out.print(":: DEBUG :: uncompressBlock() ");
        //getBlockSize();
	}
	
	public byte[] getUncompressedBlock() {
		return uncompressedBlock;
	}
	
	/**
	 * @param footer
	 */
	public void setFooter(byte[] footer) {
		byte[] crc = {footer[0],footer[1],footer[2],footer[3]};
		byte[] isze = {footer[4],footer[5],footer[6],footer[7]};
		this.CRC32 = BinaryUtil.toUnsignedInt(crc);
		this.uncompressedBlockSize = (int) BinaryUtil.toUnsignedInt(isze);
	}
	
	/***
	 * Test code for parsing BAM formatted block (CDATA)
	 */
	public void parseBamHeaderRecord() {
		String magicString = (char)uncompressedBlock[0]
				+ "" + (char)uncompressedBlock[1]
				+ "" + (char)uncompressedBlock[2];
		System.out.println(":: DEBUG :: Magic String: " + magicString);
		int i_text = BinaryUtil.toInt32(uncompressedBlock[4], uncompressedBlock[5], uncompressedBlock[6], uncompressedBlock[7]);
		System.out.println("I_text: " + i_text);
		for (int i = 0; i < i_text; i++) {
			System.out.print((char)uncompressedBlock[8+i]);
		}
		int n_ref = BinaryUtil.toInt32(uncompressedBlock[8+i_text], uncompressedBlock[8+i_text + 1],
				uncompressedBlock[8+i_text + 2], uncompressedBlock[8+i_text + 3]);
		System.out.println("n_ref: " + n_ref);
		System.out.println("$ List of reference information (n = n_ref) $");
		int offset = 12 + i_text;
		for (int i = 0; i < n_ref; i++) {
			int i_name = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
			System.out.print("i_name: " + i_name + " || ");
			offset += 4;
			for (int j = 0; j < i_name - 1; j++) {
				System.out.print((char) uncompressedBlock[offset + j]);
			}
			offset += i_name;
			int i_ref = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
					uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
			System.out.println(" | i_ref: " + i_ref);
			offset += 4;
		}
		int countAlignmentRecords = 0;
		while (offset < uncompressedBlockSize) {
			countAlignmentRecords++;
			System.out.println("Alignment block # " + countAlignmentRecords);
			offset += parseAlignments(offset);
		}
	}
	
	/***
	 * Test code
	 * @param initOffset
	 * @return
	 */
	public int parseAlignments(int initOffset) {
		int offset = initOffset;
		int block_size = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
		offset += 4;
		System.out.println("block_size: " + block_size);
		int ref_id = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
		offset += 4;
		System.out.println("refID: " + ref_id);
		int pos = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
		offset += 4;
		System.out.println("pos: " + pos);
		long bin_mq_nl = BinaryUtil.toUnsignedInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
		offset += 4;
		int I_read_name = (int) (bin_mq_nl & 0x000000ff);
		System.out.println("bin_mq_nl: " + bin_mq_nl + " | bin: " + (bin_mq_nl >> 16) + " | MAPQ: " + ((bin_mq_nl & 0x0000ff00) >> 8) + " | I_read_name: " + I_read_name);
		long flag_nc = BinaryUtil.toUnsignedInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
		offset += 4;
		int n_cigar_op = (int) (flag_nc & 0x0000ffff);
		System.out.println("flag_nc: " + flag_nc + " | FLAG: " + (flag_nc >> 16) + " | n_cigar_op: " + n_cigar_op);
		int I_seq = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
		offset += 4;
		System.out.println("I_seq: " + I_seq);
		int next_refID = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
		offset += 4;
		System.out.println("next_refID: " + next_refID);
		int next_pos = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
		offset += 4;
		System.out.println("next_pos: " + next_pos);
		int tlen = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
		offset += 4;
		System.out.println("tlen: " + tlen);
		
		System.out.print("read_name: ");
		for (int i = 0; i < I_read_name - 1; i++) {
			System.out.print((char)uncompressedBlock[i + offset]);
		}
		System.out.println();
		offset += I_read_name;
		
		if (n_cigar_op > 20)	return 0;
		for (int i = 0; i < n_cigar_op; i++) {
			long cigar = BinaryUtil.toUnsignedInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
					uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
			System.out.print("cigar: " + cigar + " | op_len: " + (cigar >> 4) + " | op: " + (cigar & 0x0000000f));
			offset += 4;
		}
		System.out.println();
		
		System.out.print("seq: ");
		for (int i = 0; i < (I_seq + 1)/2; i++) {
			System.out.print(BinaryUtil.toSeqBase(uncompressedBlock[offset + i]));
		}
		System.out.println();
		offset += (I_seq + 1)/2;
		
		System.out.print("qual(+33): ");
		for (int i = 0; i < I_seq; i++) {
			System.out.print((char) (uncompressedBlock[offset + i] + 33));
		}
		System.out.println();
		offset += I_seq;
		
		System.out.print((char)uncompressedBlock[offset] + (char)uncompressedBlock[offset + 1]
				+ ":" + (char)uncompressedBlock[offset + 2]);
		offset += 3;
		for (int i = offset; i < initOffset + block_size + 4; i++) {
			char charVal = (char) uncompressedBlock[i];
			if (charVal == '\0') {
				System.out.print("\t");
			} else {
				System.out.print(charVal);
			}
		}
		System.out.println();
		offset = initOffset + block_size + 4;
		System.out.println("offset: " + offset);
		return block_size + 4;
	}

	/**
	 * Skip RefInfo section. This method should be called only once at the first block of entire BAM file.
	 * @return RefInfo bytes from uncompressedBlock.
	 */
	public byte[] getRefInfo() {
		int offset = 4;
		l_text = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
		offset += 4 + l_text;
		n_ref = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
				uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
		offset += 4;
		for (int i = 0; i < n_ref; i++) {
			int l_name = BinaryUtil.toInt32(uncompressedBlock[offset], uncompressedBlock[offset + 1],
					uncompressedBlock[offset + 2], uncompressedBlock[offset + 3]);
			offset += 4 + l_name + 4;
		}
		byte[] refInfo = new byte[offset];
		System.arraycopy(uncompressedBlock, 0, refInfo, 0, offset);
		offsetInUncompressedBlock = offset;
		setBlockSize();
		return refInfo;
	}
	
	public int getLText() {
		return l_text;
	}
	
	public int getNRef() {
		return n_ref;
	}
	
	public int setBlockSize() {
		if (offsetInUncompressedBlock + 3 < uncompressedBlockSize) {
			block_size = BinaryUtil.toInt32(uncompressedBlock[offsetInUncompressedBlock], uncompressedBlock[offsetInUncompressedBlock + 1],
					uncompressedBlock[offsetInUncompressedBlock + 2], uncompressedBlock[offsetInUncompressedBlock + 3]);
			//System.out.println("block_size: " + block_size);
		}
		return block_size;
	}
	
	public void setBlockSize(int blockSize) {
		this.block_size = blockSize;
	}
	
	public boolean hasNoMoreRecords() {
		if (offsetInUncompressedBlock == uncompressedBlockSize) {
			return true;
		}
		return false;
	}
	
	public boolean isRecordTruncated() {
		if (offsetInUncompressedBlock == uncompressedBlockSize)	return false;
		// in case block_size is truncated
		if (offsetInUncompressedBlock + 3 >= uncompressedBlockSize)	return true;
		//System.out.print(":: DEBUG :: isRecordTruncated() ");
		setBlockSize();
		return offsetInUncompressedBlock + block_size + 4 > uncompressedBlockSize;
	}
	
	public int getRemainingTruncatedBlockSize() {
//		System.out.println(":: DEBUG :: getRemainingTruncatedBlockSize() offset: " + offsetInUncompressedBlock
//				+ " | block_size: " + block_size 
//				+ " | uncompressedBlockSize: " + uncompressedBlockSize
//				+ " | remainingByteSize: " + (offsetInUncompressedBlock + block_size + 4 - uncompressedBlockSize));
		return offsetInUncompressedBlock + block_size + 4 - uncompressedBlockSize;
	}
	
	public byte[] getTruncatedRecord() {
		byte[] truncatedBytes = new byte[uncompressedBlockSize - offsetInUncompressedBlock];
		for (int i = 0; i < truncatedBytes.length; i++) {
			truncatedBytes[i] = uncompressedBlock[offsetInUncompressedBlock + i];
		}
		return truncatedBytes;
	}
	
	public byte[] getTruncatedRecord(int size) {
		byte[] truncatedBytes = new byte[size];
		for (int i = 0; i < size; i++) {
			truncatedBytes[i] = uncompressedBlock[i];
		}
		offsetInUncompressedBlock += size;
		//System.out.print(":: DEBUG :: getTruncatedRecord(int size) ");
		setBlockSize();
		return truncatedBytes;
	}
	
	public BamRecord scanRecord(BamRecord record) {
		//System.out.print(":: DEBUG :: scanRecord() ");
		setBlockSize();
		byte[] recordBytes = new byte[block_size + 4];
		System.arraycopy(uncompressedBlock, offsetInUncompressedBlock, recordBytes, 0, block_size + 4);
		record.setRecordBytes(recordBytes);
		offsetInUncompressedBlock += block_size + 4;
		return record;
	}
	
	public void goTo(long uOffset) {
		offsetInUncompressedBlock = (int) uOffset;
	}
	
	public int getUoffset() {
		return offsetInUncompressedBlock;
	}

	public void addTruncatedBytes(byte[] uncompressedBytes) {
		for (int i = 0; i < uncompressedBytes.length; i++) {
			uncompressedBlock[this.truncatedByteSize + i] = uncompressedBytes[i];
		}
		uncompressedBlockSize = uncompressedBlock.length;
	}

	public boolean isBlockSizeTruncated() {
		if (offsetInUncompressedBlock + 3 >= uncompressedBlockSize) {
//			System.out.println("isBlockSizeTruncated() = T; offsetInUncompressedBlock: " + offsetInUncompressedBlock
//					+ "uncompressedBlockSize: " + uncompressedBlockSize);
			return true;
		}
		return false;
	}

	public byte[] getRestOfBytes() {
		int restSize = uncompressedBlockSize - offsetInUncompressedBlock;
		byte[] restOfBytes = new byte[restSize];
		for (int i = 0; i < restSize; i++) {
			restOfBytes[i] = uncompressedBlock[offsetInUncompressedBlock + i];
		}
		return restOfBytes;
	}

	public byte[] readBytes(int size) {
		//System.out.println(":: DEBUG :: readBytes(int " + size + " )");
		byte[] bytes = new byte[size];
		for (int i = 0; i < size; i++) {
			bytes[i] = uncompressedBlock[i];
		}
		return bytes;
	}
	
	
}
