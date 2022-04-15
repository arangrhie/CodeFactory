/**
 * 
 */
package javax.arang.bit;

import java.io.UnsupportedEncodingException;


/**
 * @author Arang
 *
 */
public class BitByteTest {
	private static final String utf8 = "UTF-8";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		float floatNum = 3.2f;
		System.out.println(Math.ceil(floatNum));
		
		char at = '@';
		System.out.println("@ is " + (int)at);
		
		
//		int genomePos = 154183401;
		int genomePos = 4318;
		System.out.println("ORIGINAL NUM: " + genomePos);
		System.out.println("=========== int to bytes ==========");
		byte[] keys = toBytes(genomePos);
		System.out.println(genomePos + " -> " + keys[0] + " " + keys[1] + " " + keys[2] + " " + keys[3]);
		keys = Util.to2Bytes(genomePos);
		System.out.println(genomePos + " -> " + keys[0] + " " + keys[1]);
		
		System.out.println("=========== bytes to int ==========");
//		genomePos = toInt(keys);
//		genomePos = toInt(keys[0], keys[1], keys[2], keys[3]);
		genomePos = Util.to2Int(keys[0], keys[1]);
		System.out.println("bytes back to int: " + genomePos);
		
		System.out.println("========== 128 -> -128 -> decoding ==========");
		int m = 233;
		System.out.println("m : " + m);
		byte m0 = (byte) m;
		System.out.println("m0 : " + m0);
		int m1 = toUnsignedByte(m0);
		System.out.println("byte -> int: " + m1);
		System.out.println("(" + m0 + " << 2) >> 2: " + ((m0 << 2) >> 2)
				+ " -> this is not working!!");
		
		int chrom = 3;
		System.out.println(chrom + " : " + (chrom << 6) + " / byte: " + (byte)(chrom << 6));
		
		int genoPos = toInt((byte) 0, (byte) 2, (byte) 126, (byte) -5);
		System.out.println("MAP OUT: genoPos=" + genoPos);
		
		System.out.println("--------------");
		qualTest();
		charTest(keys);
		shiftTest();
		
		maxValues();
		
		utfTest();
		
		System.out.println("# is " + (int) "#".charAt(0));
		System.out.println("-3 is " + (char) (-1));

	}
	
	public static int toUnsignedByte(byte signedByte) {
		return (int) signedByte >= 0 ? (int) signedByte : (int) signedByte + (int) Math.pow(2, 8);
	}
	
	public static byte[] toBytes(int intNum) {
		byte[] keys = new byte[4];
		int k0 = intNum/(int)Math.pow(2, 24);
		keys[0] = (byte) k0;
		intNum -= k0*(int)Math.pow(2, 24);
		int k1 = intNum/(int)Math.pow(2, 16);
		keys[1] = (byte) k1;
		intNum -= k1*(int)Math.pow(2, 16);
		int k2 = intNum/(int)Math.pow(2, 8);
		keys[2] = (byte) k2;
		intNum -= k2*(int)Math.pow(2, 8);
		keys[3] = (byte) intNum;
		System.out.println("k0 = " + k0 + "\t\t:keys[0] = " + keys[0]);
		System.out.println("k1 = " + k1 + "\t\t:keys[1] = " + keys[1]);
		System.out.println("k2 = " + k2 + "\t:keys[2] = " + keys[2]);
		System.out.println("k3 = " + intNum + "\t:keys[3] = " + keys[3]);
		return keys;
	}
	
	public static int toInt(byte[] bytes) {
		int genomePos = 0;
		int hat2of8 = (int) Math.pow(2, 8);
		int key0 = (int) bytes[0] >= 0 ? bytes[0] : (int)bytes[0] + hat2of8;
		int key1 = (int) bytes[1] >= 0 ? bytes[1] : (int)bytes[1] + hat2of8;
		int key2 = (int) bytes[2] >= 0 ? bytes[2] : (int)bytes[2] + hat2of8;
		int key3 = (int) bytes[3] >= 0 ? bytes[3] : (int)bytes[3] + hat2of8;
		genomePos = (key0 << 24) + (key1 << 16) + (key2 << 8) + key3;
//		System.out.println("154183401 : " + key0 + " " + key1 + " " + key2 + " " + key3);
		return genomePos;
	}
	
	public static int toInt(byte byte1, byte byte2, byte byte3, byte byte4) {
		byte[] bytes = new byte[4];
		bytes[0] = byte1;
		bytes[1] = byte2;
		bytes[2] = byte3;
		bytes[3] = byte4;
		return toInt(bytes);
	}
	
	private static void printUTFbyte(String str) {
		System.out.println("=== " + str + " ===");
		byte[] utfByte;
		try {
			 utfByte = str.getBytes(utf8);
			 System.out.print(str + ": ");
			 for (int i = 0; i < utfByte.length; i++) {
				 System.out.print(utfByte[i] + " ");
			 }
			 System.out.println();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private static void printByte(String str) {
		System.out.println("=== " + str + " ===");
		byte[] utfByte;
		utfByte = str.getBytes();
		System.out.print(str + ": ");
		for (int i = 0; i < utfByte.length; i++) {
			System.out.print(utfByte[i] + " ");
		}
		System.out.println();
	}
	
	private static byte getChrom(String chrom) {
		chrom = chrom.substring(chrom.indexOf("chr") + 3);
		byte chr = 0; 
		if (chrom.equals("X")) {
			chrom = "23";	// X
		} else if (chrom.equals("Y")) {
			chrom = "24";	// Y
		} else if (chrom.equals("M")) {
			chrom = "25";	// Mitocondria
		}
		chr = (byte) Integer.parseInt(chrom);
		return chr;
	}
	
	private static void maxValues() {
		System.out.println("Byte.MAX_VALUE : "+Byte.MAX_VALUE);
		System.out.println("Integer.MAX_VALUE : "+Integer.MAX_VALUE);
		System.out.println("Character.MAX_VALUE : " + Character.MAX_VALUE);
		System.out.println("Long.MAX_VALUE: " + Long.MAX_VALUE);
	}
	
	private static void qualTest() {
		Character qual = '"';
		int q = (int) qual;
		System.out.println(qual + " : " + q);
//		q = (63 + 192) >>> 6;	// 3 = 11
//		q = Integer.MAX_VALUE;		// 	2147483647 2^31 = 4byte
//		q = (int) Math.pow(2, 28);	//	 268435456 2^28 = 3byte
//		q = 3 << 6;	// 192
		qual = (char) q;
		System.out.println(q + " : " + qual);
		
		byte b = -3;
		System.out.println("Byte b : " + (int) b);
		System.out.println("b << 6 : " + (b << 6));
		System.out.println("b >>> 6 : " + (int) (b >>> 6));
		
		qual = '~';
		b = Byte.parseByte(String.valueOf(qual - 61));
		System.out.println("qua " + qual + " : " + b);
		
		String str = "\t";
		System.out.println("qua: " + str.getBytes().length);
	}
	private static void charTest(byte[] keys) {
		// char is too long!!
		System.out.println("2^7 = " + (int) Math.pow(2, 7));
		System.out.println("============ Using char ============");
		char key1C = (char) keys[1];
		System.out.println("keys[1]: " + (int)key1C);
		char key2C = (char) keys[2];
		System.out.println("keys[2]: " + (int)key2C);
		char key3C = (char) keys[3];
		System.out.println("keys[3]: " + (int)key3C);
		int key1 = ((int) key1C) << 16;
		int key2 = ((int) key2C) << 8;
		int key3 = (int) key3C;
		System.out.println("154183401 : " + key1 + " " + key2 + " " + key3);
	}
	private static void shiftTest() {
		byte shif1 = 1;
		System.out.println("shif1 << 1: " + (shif1 << 1));
		System.out.println("shif1 << 2: " + (shif1 << 2));
		System.out.println("shif1 << 3: " + (shif1 << 3));
		System.out.println("shif1 << 4: " + (shif1 << 4));
		System.out.println("shif1 << 5: " + (shif1 << 5));
		System.out.println("shif1 << 6: " + (shif1 << 6));
		System.out.println("shif1 << 7: " + (shif1 << 7));
		System.out.println("shif1 << 8: " + (shif1 << 8));
		String chromosome = "chr1";
		byte chr = getChrom(chromosome);
		int chrom = chr;
		System.out.println(chromosome + " = " + chrom);
		System.out.println("byte chr: " + chr);
	}
	private static void utfTest() {
		printUTFbyte(" ");
		printByte(" ");
		printUTFbyte("\t");
		printByte("\t");
		printUTFbyte("0");
		printByte("0");
		printUTFbyte("1");
		printByte("1");
		printUTFbyte("2");
		printByte("2");
		printUTFbyte("3");
		printByte("3");
		printUTFbyte("4");
		printByte("4");
		printUTFbyte(":");
		printByte(":");
		printUTFbyte("\n");
		printByte("\n");
	}
	
}
