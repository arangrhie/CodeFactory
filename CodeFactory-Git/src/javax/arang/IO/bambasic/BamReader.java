/**
 * 
 */
package javax.arang.IO.bambasic;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;

import javax.arang.bam.util.BamBlock;
import javax.arang.bam.util.BamRecord;
import javax.arang.bam.util.RefInfo;
import javax.arang.samtools.util.Constants;

/**
 * @author Arang Rhie
 *
 */
public class BamReader {

	RandomAccessFile raf;
//	BufferedInputStream ir;
	String path;
	BamBlock currentBlock = null;
	boolean isFirstBlock = true;
	RefInfo refInfo = null;
	
	public BamReader(String path) {
		this.path = path;
		try {
			raf = new RandomAccessFile(path, "r");
//			ir = new BufferedInputStream(new FileInputStream(path));
			if (readBlockHeader() && !readRefInfo()) {
				System.out.println("No reference info available.");
				System.exit(-1);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public String getDirectory() {
		if (path.contains("/")) {
			return path.substring(0, path.lastIndexOf("/"));
		} else if (path.contains("\\")) {
			return path.substring(0, path.lastIndexOf("\\"));
		} else {
			return ".";
		}
	}
	
	public String getFileName() {
		if (path.contains("/")) {
			return path.substring(path.lastIndexOf("/") + 1);
		} else if (path.contains("\\")) {
			return path.substring(path.lastIndexOf("\\") + 1);
		} else {
			return path;
		}
	}
	
	public void goTo(long cOffsetBeg, long uOffsetBeg) {
		if (cOffsetBeg == 0)	return;
		try {
//			ir.skip(cOffsetBeg - fileTracker);
			raf.seek(cOffsetBeg);
			if (readBlockHeader()) {
				currentBlock = readBamBlock();
			}
			currentBlock.goTo(uOffsetBeg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getCurrentUoffset() {
		return currentBlock.getUoffset();
	}
	
	private boolean readBlockHeader() {
		
		try {
			currentBlock = new BamBlock();
//			byte[] header = BinaryUtil.readBytes(ir, Constants.BLOCK_HEADER_LENGTH);
			byte[] header = new byte[Constants.BLOCK_HEADER_LENGTH];
			raf.readFully(header, 0, Constants.BLOCK_HEADER_LENGTH);
			if (currentBlock.setHeader(header)) {
				// Checking for EOF
				if (currentBlock.getCompressedBlockSize() <= 2) {
					System.out.println("Reaching EOF: " + getFileName() + " has been read through the end.");
					return false;
				} else {
//					if (ir.available() > 0)	return true;
					return true;
				}
//				return false;
			} else {
				System.err.println(":: ERROR :: Not a valid BGZF header. Corrupted or truncated?");
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean hasMoreBlocks(BamBlock block) {
		try {
//			byte[] header = BinaryUtil.readBytes(ir, Constants.BLOCK_HEADER_LENGTH);
			byte[] header = new byte[Constants.BLOCK_HEADER_LENGTH];
			raf.readFully(header, 0, Constants.BLOCK_HEADER_LENGTH);
			if (block.setHeader(header)) {
				// Checking for EOF
				if (block.getCompressedBlockSize() <= 2) {
					System.out.println("Reaching EOF: " + getFileName() + " has been read through the end.");
					return false;
				} else {
//					if (ir.available() > 0)	return true;
					return true;
				}
//				return false;
			} else {
				System.err.println(":: ERROR :: Not a valid BGZF header. Corrupted or truncated?");
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public BamBlock readBamBlock() {
		try {
		int compressedBlockSize = (int) currentBlock.getCompressedBlockSize();
//		byte[] compressedBlock = BinaryUtil.readBytes(ir, compressedBlockSize);
		byte[] compressedBlock = new byte[compressedBlockSize];
		raf.readFully(compressedBlock, 0, compressedBlockSize);
//		byte[] footer = BinaryUtil.readBytes(ir, Constants.BLOCK_FOOTER_LENGTH);
		byte[] footer = new byte[Constants.BLOCK_FOOTER_LENGTH];
		raf.readFully(footer, 0, Constants.BLOCK_FOOTER_LENGTH);
		currentBlock.setFooter(footer);
		currentBlock.uncompressBlock(compressedBlock);
		} catch (DataFormatException e) {
			System.err.println(":: ERROR :: Probably corruped input BAM file. Insufficient to uncompress CDATA.");
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Reaching EOF.");
			return null;
		}
		return currentBlock;
	}
	
	private BamBlock readBamBlock(BamBlock block) {
		try {
			int compressedBlockSize = (int) block.getCompressedBlockSize();
			//		byte[] compressedBlock = BinaryUtil.readBytes(ir, compressedBlockSize);
			byte[] compressedBlock = new byte[compressedBlockSize];
			raf.readFully(compressedBlock, 0, compressedBlockSize);
			//		byte[] footer = BinaryUtil.readBytes(ir, Constants.BLOCK_FOOTER_LENGTH);
			byte[] footer = new byte[Constants.BLOCK_FOOTER_LENGTH];
			raf.readFully(footer, 0, Constants.BLOCK_FOOTER_LENGTH);
			block.setFooter(footer);
			block.uncompressBlock(compressedBlock);
		} catch (DataFormatException e) {
			System.err.println(":: ERROR :: Probably corruped input BAM file. Insufficient to uncompress CDATA.");
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Reaching EOF.");
			return null;
		}
		return block;
	}

	/**
	 * 
	 */
	public void closeReader() {
//		try {
//			ir.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}

	/**
	 * @return
	 */
	public BamRecord getNextAlignmentRecord() {
		BamRecord record = new BamRecord();
		if (currentBlock.isRecordTruncated()) {
			// create an artificial BamBlock containing the rest of the currentBlock record and the first part of the record.
			// This block contains only one record.
			//System.out.println(":: DEBUG :: Record Truncated");
			
			// if block_size is truncated
			if (currentBlock.isBlockSizeTruncated()) {
				//System.out.println(":: DEBUG :: block_size truncated");
				byte[] blockSizeBlock1 = currentBlock.getRestOfBytes();
				try {
				long pos = raf.getFilePointer();
//				ir.mark(1000000000);
				BamBlock tmpBlock = new BamBlock();
				if (hasMoreBlocks(tmpBlock)) {
					tmpBlock = readBamBlock(tmpBlock);
					byte[] blockSizeBlock2 = tmpBlock.readBytes(4 - blockSizeBlock1.length);
					byte[] blockSize = new byte[4];
					for (int i = 0; i < blockSizeBlock1.length; i++) {
						blockSize[i] = blockSizeBlock1[i];
					}
					for (int i = 0; i < blockSizeBlock2.length; i++) {
						blockSize[blockSizeBlock1.length + i] = blockSizeBlock2[i];
					}
					int block_size = (int)BinaryUtil.toUnsignedInt(blockSize);
					//System.out.println(":: DEBUG :: block_size: " + block_size);
					currentBlock.setBlockSize(block_size);
				}
//					ir.reset();
					raf.seek(pos);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}		

			int remainingByteSize = currentBlock.getRemainingTruncatedBlockSize();
			BamBlock tmpBlock = new BamBlock(currentBlock.getTruncatedRecord(), remainingByteSize);
			if (readBlockHeader()) {
				currentBlock = readBamBlock();
				tmpBlock.addTruncatedBytes(currentBlock.getTruncatedRecord(remainingByteSize));
				return tmpBlock.scanRecord(record);
			}
		}
		if (currentBlock.hasNoMoreRecords()) {
			//System.out.println(":: DEBUG :: No more records available. Loading new block..");
			if (readBlockHeader()) {
				currentBlock = readBamBlock();
			}
		}
		return currentBlock.scanRecord(record);
	}
	
	private boolean readRefInfo() {
		if (isFirstBlock) {
			refInfo = new RefInfo();
			isFirstBlock = false;
			currentBlock = readBamBlock();
			refInfo.setRefInfo(currentBlock.getRefInfo(),
					currentBlock.getLText(), currentBlock.getNRef());
			return true;
		}
		return false;
	}
	
	public boolean hasMoreAlignmentRecord() {
		if (currentBlock.hasNoMoreRecords()) {
			//System.out.println(":: DEBUG :: Reaching end of CDATA block. Loading new block..");
			if (readBlockHeader()) {
				currentBlock = readBamBlock();
				if (currentBlock == null)	return false;
				return true;
			}
			return false;
		}
		return true;
	}

	public RefInfo getRefInfo() {
		return refInfo;
	}
}
