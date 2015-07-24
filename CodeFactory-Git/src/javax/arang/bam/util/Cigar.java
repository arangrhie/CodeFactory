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
public class Cigar {

	public static final short COUNT = 0;
	public static final short OP = 1;
	
	public static final short OP_M = 0;
	public static final short OP_I = 1;
	public static final short OP_D = 2;
	public static final short OP_N = 3;
	public static final short OP_S = 4;
	public static final short OP_H = 5;
	public static final short OP_P = 6;
	public static final short OP_EQ = 7;
	public static final short OP_X = 8;
	
	public static ArrayList<Integer[]> parseCigarArray(byte[] cigarBytes, int nCigarOp) {
		ArrayList<Integer[]> cigarArray = new ArrayList<Integer[]>();
		for (int i = 0; i < nCigarOp; i++) {
			int offset = i*4;
			long cigarArr = BinaryUtil.toUnsignedInt32(cigarBytes[offset], cigarBytes[offset + 1], cigarBytes[offset + 2], cigarBytes[offset + 3]);
			Integer[] op = new Integer[2];
			op[Cigar.OP] = (int) cigarArr % 16;
			op[Cigar.COUNT] = (int) (cigarArr >>> 4);
			cigarArray.add(op);
		}
		return cigarArray;
	}
	
	public static String getCigarString(byte[] cigarBytes, int nCigarOp) {
		StringBuffer cigarStr = new StringBuffer();
		int op;
		int count;
		for (int i = 0; i < nCigarOp; i++) {
			int offset = i*4;
			long cigarArr = BinaryUtil.toUnsignedInt32(cigarBytes[offset], cigarBytes[offset + 1], cigarBytes[offset + 2], cigarBytes[offset + 3]);
			op = (int) cigarArr % 16;
			count = (int) (cigarArr >>> 4);
			cigarStr.append(count);
			cigarStr.append(getOp(op));
		}
		return cigarStr.toString();
	}
	
	public static char getOp(int opVal) {
		switch(opVal) {
			case OP_M: return 'M';
			case OP_I: return 'I';
			case OP_D: return 'D';
			case OP_N: return 'N';
			case OP_S: return 'S';
			case OP_H: return 'H';
			case OP_P: return 'P';
			case OP_EQ: return '=';
			case OP_X: return 'X';
		}
		return '?';
	}

	public static String getCigarString(ArrayList<Integer[]> cigarArray) {
		StringBuffer cigarStr = new StringBuffer();
		Integer[] op;
		for (int i = 0; i < cigarArray.size(); i++) {
			op = cigarArray.get(i);
			cigarStr.append(op[Cigar.COUNT]);
			cigarStr.append(getOp(op[Cigar.OP]));
		}
		return cigarStr.toString();
	}
}
