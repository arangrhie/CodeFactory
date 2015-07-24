/**
 * 
 */
package javax.arang.IO.bambasic;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.arang.bam.util.SeqBase;

/**
 * @author Arang Rhie
 *
 */
public class BinaryUtil {
	
	public static byte[] readBytes(BufferedInputStream ir, int numBytes) {
		byte[] readBytes = new byte[numBytes];
		try {
			ir.read(readBytes, 0, numBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return readBytes;
	}
	
	public static long toUnsignedInt(byte[] inBytes) {
		long val = 0;
		int numBytes = inBytes.length;
//		StringBuffer bits = new StringBuffer();

		for (int i = 0; i < numBytes; i++) {
//			bits.append(" ");
			int toUint = (int)inBytes[i] & 0xff;
//			if (toUint < 0) {
//				toUint = toUint << 25;
//				toUint = toUint >>> 25;
//				toUint += 128;
//			}
			for (int j = 0; j < 8; j++) {
				val += (toUint%2) * Math.pow(2, j + i*8);
//				bits.append(toUint%2);
				toUint = toUint >>> 1;
			}
		}
		return val;
	}
	
	public static long toUnsignedInt32(byte inByte0, byte inByte1, byte inByte2, byte inByte3) {
		long val = 0;
		int numBytes = 4;
		StringBuffer bits = new StringBuffer();
		
		byte[] inBytes = {inByte0, inByte1, inByte2, inByte3};

		for (int i = 0; i < numBytes; i++) {
			bits.append(" ");
			int toUint = (int)inBytes[i];
			if (toUint < 0) {
				toUint = toUint << 25;
				toUint = toUint >>> 25;
				toUint += 128;
			}
			for (int j = 0; j < 8; j++) {
				val += (toUint%2) * Math.pow(2, j + i*8);
				bits.append(toUint%2);
				toUint = toUint >>> 1;
			}
		}
		return val;
	}
	
	public static long toUnsignedLong(byte[] inBytes) {
		long val = 0;
		if (inBytes.length > 8) {
			throw new RuntimeException("inBytes.length > 8");
		}
		int toUint = 0;
		if (inBytes.length != 8) {
			for (int i = 0; i < inBytes.length; i++) {
				toUint = (int)inBytes[i];
				if (toUint < 0) {
					toUint = toUint << 25;
					toUint = toUint >>> 25;
					toUint += 128;
				}
				for (int j = 0; j < 8; j++) {
					val += (toUint%2) * Math.pow(2, j + i*8);
					toUint = toUint >>> 1;
				}
			}
		} else {
			// sign bit convergion needed
			for (int i = 0; i < 7; i++) {
				toUint = (int)inBytes[i];
				if (toUint < 0) {
					toUint = toUint << 25;
					toUint = toUint >>> 25;
					toUint += 128;
				}
				for (int j = 0; j < 8; j++) {
					val += (toUint%2) * Math.pow(2, j + i*8);
					toUint = toUint >>> 1;
				}
			}
			toUint = (int)inBytes[7];
			for (int j = 0; j < 7; j++) {
				val += (toUint%2) * Math.pow(2, j + 7*8);
				toUint = toUint >>> 1;
			}
			if (toUint == 1) {
				val *= -1;
			}
		}
		return val;
	}
	
	public static int toInt32(byte inByte0, byte inByte1, byte inByte2, byte inByte3) {
		int val = 0;
		int numBytes = 4;
//		StringBuffer bits = new StringBuffer();
		
		byte[] inBytes = {inByte0, inByte1, inByte2, inByte3};
		int toUint = 0;
		for (int i = 0; i < numBytes - 1; i++) {
//			bits.append(" ");
			toUint = (int)inBytes[i];
			if (toUint < 0) {
				toUint = toUint << 25;
				toUint = toUint >>> 25;
				toUint += 128;
			}
			for (int j = 0; j < 8; j++) {
				val += (toUint%2) * Math.pow(2, j + i*8);
//				bits.append(toUint%2);
				toUint = toUint >>> 1;
			}
		}
		toUint = (int)inBytes[numBytes - 1];
		for (int j = 0; j < 7; j++) {
			val += (toUint%2) * Math.pow(2, j + 3*8);
//			bits.append(toUint%2);
			toUint = toUint >>> 1;
		}
		if (toUint == 1) {
			val *= -1;
		}
		return val;
	}
	
	public static int toUnsignedInt(byte inByte) {
		int val = 0;
		StringBuffer bits = new StringBuffer();

		bits.append(" ");
		int toUint = (int)inByte;
		if (toUint < 0) {
			toUint = toUint << 25;
			toUint = toUint >>> 25;
			toUint += 128;
		}
		for (int j = 0; j < 8; j++) {
			val += (toUint%2) * Math.pow(2, j);
			bits.append(toUint%2);
			toUint = toUint >>> 1;
		}
		return val;
	}
	
	/***
	 * 4-bit encoded read: '=ACMGRSVTWYHKDBN' -> [0,15];
	 * Other characters mapped to 'N';
	 * highly nybble first (1st base in the highest 4-bit of the 1st byte)
	 * @param twoBases two bases encoded in 1 byte
	 * @return two bases as a string representation
	 */
	public static String toSeqBase(byte twoBases) {
		int unsignedBases = toUnsignedInt(twoBases);
		//System.out.println(unsignedBases + " " + ((unsignedBases & 0xf0) >> 4) + " " + (unsignedBases & 0x0f));
		return convertEncodedSeqToChar((unsignedBases & 0xf0) >> 4)
				+ convertEncodedSeqToChar(unsignedBases & 0x0f);
	}
	
	public static String convertEncodedSeqToChar(int base) {
		SeqBase charBase = SeqBase.valueOf(base);
		if (charBase.toString() == "eq")	return "=";
		return charBase.toString();
	}
}
