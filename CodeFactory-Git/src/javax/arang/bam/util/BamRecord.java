/**
 * 
 */
package javax.arang.bam.util;

import java.util.ArrayList;

import javax.arang.IO.bambasic.BinaryUtil;


/**
 * @author Arang Rhie
 *
 */
public class BamRecord {
	
	public static final int OFFSET_BLOCK_SIZE = 0;
	public static final int OFFSET_REF_ID = 4;
	public static final int OFFSET_POS = 8;
	public static final int OFFSET_BIN_MQ_NL = 12;
	public static final int OFFSET_FLAG_NC = 16;
	public static final int OFFSET_L_SEQ = 20;
	public static final int OFFSET_NEXT_REF_ID = 24;
	public static final int OFFSET_NEXT_POS = 28;
	public static final int OFFSET_TLEN = 32;
	public static final int OFFSET_READ_NAME = 36;
	public int OFFSET_CIGAR = -1;
	public int OFFSET_SEQ = -1;
	public int OFFSET_QUAL = -1;
	public int OFFSET_TAGS = -1;
	
	private byte[] recordBytes = null;
	private int refID = 0;
	private int pos = 0;
	private long bin_mq_nl = 0;
	private int l_read_name = 0;
	private int flag = 0;
	private int n_cigar_op = 0;
	private byte[] readNameByte = null;
	private byte[] seqByte = null;
	private int seqLen = 0;
	private byte[] qualByte = null;
	private String readName = null;
	private String seq = null;
	private String qual = null;
	private ArrayList<Integer[]> cigarArray = null;
	private byte[] cigarByte = null;
	private int refNextID = -1;
	private int posNext = -1;
	private int tlen = 0;
	
	
	public void setRecordBytes(byte[] recordBytes) {
		this.recordBytes = recordBytes;
		readNameByte = null;
		seqByte = null;
		qualByte = null;
		readName = null;
		seq = null;
		qual = null;
		int offset = OFFSET_REF_ID;
		refID = BinaryUtil.toInt32(this.recordBytes[offset], this.recordBytes[offset + 1],
				this.recordBytes[offset + 2], this.recordBytes[offset + 3]);
		offset = OFFSET_POS;
		pos = BinaryUtil.toInt32(this.recordBytes[offset], this.recordBytes[offset + 1],
				this.recordBytes[offset + 2], this.recordBytes[offset + 3]) + 1;
		offset = OFFSET_BIN_MQ_NL;
		bin_mq_nl = BinaryUtil.toUnsignedInt32(this.recordBytes[offset], this.recordBytes[offset + 1],
				this.recordBytes[offset + 2], this.recordBytes[offset + 3]);
		l_read_name = (int) (bin_mq_nl & 0x000000ff);
		OFFSET_CIGAR = OFFSET_READ_NAME + l_read_name;

		offset = OFFSET_FLAG_NC;
		long flag_nc = BinaryUtil.toUnsignedInt32(this.recordBytes[offset], this.recordBytes[offset + 1],
				this.recordBytes[offset + 2], this.recordBytes[offset + 3]);
		flag = (int) ((flag_nc & 0xffff0000) >>> 16);
		n_cigar_op = (int) (flag_nc & 0x0000ffff);
		OFFSET_SEQ = OFFSET_CIGAR + 4*n_cigar_op;
		
		offset = OFFSET_L_SEQ;
		seqLen = BinaryUtil.toInt32(this.recordBytes[offset], this.recordBytes[offset + 1],
				this.recordBytes[offset + 2], this.recordBytes[offset + 3]);
		
		OFFSET_QUAL = OFFSET_SEQ + (seqLen + 1)/2;
		OFFSET_TAGS = OFFSET_QUAL + seqLen;
		
		offset = OFFSET_NEXT_REF_ID;
		refNextID = BinaryUtil.toInt32(this.recordBytes[offset], this.recordBytes[offset + 1],
				this.recordBytes[offset + 2], this.recordBytes[offset + 3]);
		offset = OFFSET_NEXT_POS;
		posNext = BinaryUtil.toInt32(this.recordBytes[offset], this.recordBytes[offset + 1],
				this.recordBytes[offset + 2], this.recordBytes[offset + 3]) + 1;
		
		offset = OFFSET_TLEN;
		tlen = BinaryUtil.toInt32(this.recordBytes[offset], this.recordBytes[offset + 1],
				this.recordBytes[offset + 2], this.recordBytes[offset + 3]);
	}
	
	public int getBin() {
		return (int)(bin_mq_nl >>> 16);
	}
	
	public byte[] getRecordBytes() {
		return recordBytes;
	}

	public byte[] getReadNameBytes() {
		return initBytes(OFFSET_READ_NAME, readNameByte, l_read_name);
	}
	
	public byte[] getSeqBytes() {
		return initBytes(OFFSET_SEQ, seqByte, (seqLen+1)/2);
	}
	
	public byte[] getQualBytes() {
		return initBytes(OFFSET_QUAL, qualByte, seqLen);
	}

	public byte[] getCigarBytes() {
		return initBytes(OFFSET_CIGAR, cigarByte, 4*n_cigar_op);
	}
	
	private byte[] initBytes(int offset, byte[] dest, int len) {
		if (dest == null) {
			dest = new byte[len];
			System.arraycopy(recordBytes, offset, dest, 0, len);
		}
		return dest;
	}
	
	
	
	public String getReadName() {
		if (readName == null) {
			StringBuffer strBuff = new StringBuffer();
			byte[] readNameBytes = getReadNameBytes();
			for (int i = 0; i < l_read_name - 1; i++) {
				strBuff.append((char)readNameBytes[i]);
			}
			readName = strBuff.toString();
		}
		return readName;
	}
	
	public int getSeqLength() {
		return l_read_name;
	}

	public String getSeq() {
		if (seq == null) {
			StringBuffer strBuff = new StringBuffer();
			byte[] seqBytes = getSeqBytes();
			for (int i = 0; i < seqBytes.length; i++) {
				strBuff.append(BinaryUtil.toSeqBase(seqBytes[i]));
			}
			seq = strBuff.substring(0, seqLen);
		}
		return seq;
	}
	
	public String getQual() {
		if (qual == null) {
			StringBuffer strBuff = new StringBuffer();
			byte[] qualBytes = getQualBytes();
			for (int i = 0; i < seqLen; i++) {
				strBuff.append((char) (qualBytes[i] + 33));
			}
			qual = strBuff.toString();
		}
		return qual;
	}
	
	public int getRefNextID() {
		return refNextID;
	}
	
	public int getNextPos() {
		return posNext;
	}

	/**
	 * @return String representation of the reference name.
	 */
	public String getRefName(RefInfo ref) {
		return ref.getRefName(refID);
	}
	
	/**
	 * @return String representation of the reference name.
	 */
	public String getRefName(RefInfo ref, int refId) {
		return ref.getRefName(refId);
	}
	
	/***
	 * 
	 * @return Integer value reference ID of the reference.
	 * For String representation, use getRefName(). 
	 */
	public int getRefID() {
		if (refID == 2113929215)	return -1;
		return refID;
	}
	
	/***
	 * 
	 * @return 1-based position. (POS in sam format)
	 */
	public int getPos() {
		return pos;
	}
	
	/***
	 * 
	 * @return Flag in integer format
	 */
	public int getFlag() {
		return flag;
	}
	
	public String getRecordLine(RefInfo ref) {
		StringBuffer line = new StringBuffer();
		line.append(getReadName() + "\t");
		line.append(this.getFlag() + "\t");
		line.append(this.getRefName(ref) + "\t");
		line.append(this.getPos() + "\t");
		line.append(this.getQual() + "\t");
		line.append(this.getCigarString() + "\t");
		line.append(this.getRefName(ref, refNextID) + "\t");
		line.append(this.getNextPos() + "\t");
		line.append(this.getTLen() + "\t");
		line.append(this.getSeq() + "\t");
		line.append(this.getQual());
		return line.toString();
	}
	
	private int getTLen() {
		return tlen;
	}

	public String getCigarString() { 
		return Cigar.getCigarString(cigarArray);
	}
	
	public ArrayList<Integer[]> getCigar() {
		if (cigarArray == null) {
			cigarArray = Cigar.parseCigarArray(getCigarBytes(), n_cigar_op);
		}
		return cigarArray;
	}
}
