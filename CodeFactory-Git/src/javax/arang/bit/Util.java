package javax.arang.bit;

public class Util {

	public static int toInt(byte b) {
		return (int) b >= 0 ? b : (int)b + (int) Math.pow(2, 8);
	}

	/***
	 * Parse a 4-byte long int primitive to an unsigned intended
	 * byte array of length 4.
	 * @param intNum 4-byte long int
	 * @return byte[4]
	 */
	public static byte[] to4Bytes(int intNum) {
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
		return keys;
	}

	/***
	 * Parse a 4-byte long int primitive to an unsigned intended
	 * byte array of length 4.
	 * @param intNum 4-byte long int
	 * @return byte[4]
	 */
	public static byte[] to2Bytes(int intNum) {
		byte[] keys = new byte[2];
		int k2 = intNum/(int)Math.pow(2, 8);
		keys[0] = (byte) k2;
		intNum -= k2*(int)Math.pow(2, 8);
		keys[1] = (byte) intNum;
		return keys;
	}

	/***
	 * Parse back from a byte array to a 4-byte long integer number.
	 * @param bytes	bytes to parse, length of 4.
	 * @return 32-bit long, signed integer number.
	 */
	public static int to4Int(byte[] bytes) {
		int intVal = 0;
		int hat2of8 = (int) Math.pow(2, 8);
		int key0 = (int) bytes[0] >= 0 ? bytes[0] : (int)bytes[0] + hat2of8;
		int key1 = (int) bytes[1] >= 0 ? bytes[1] : (int)bytes[1] + hat2of8;
		int key2 = (int) bytes[2] >= 0 ? bytes[2] : (int)bytes[2] + hat2of8;
		int key3 = (int) bytes[3] >= 0 ? bytes[3] : (int)bytes[3] + hat2of8;
		intVal = (key0 << 24) + (key1 << 16) + (key2 << 8) + key3;
		return intVal;
	}

	public static int to4Int(byte byte1, byte byte2, byte byte3, byte byte4) {
		byte[] bytes = new byte[4];
		bytes[0] = byte1;
		bytes[1] = byte2;
		bytes[2] = byte3;
		bytes[3] = byte4;
		return to4Int(bytes);
	}

	/***
	 * Parse back from a byte array to a 2-byte long integer number.
	 * @param bytes	bytes to parse, length of 2.
	 * @return 32-bit long, signed integer number.
	 */
	public static int to2Int(byte[] bytes) {
		int intVal = 0;
		int hat2of8 = (int) Math.pow(2, 8);
		int key0 = (int) bytes[0] >= 0 ? bytes[0] : (int)bytes[0] + hat2of8;
		int key1 = (int) bytes[1] >= 0 ? bytes[1] : (int)bytes[1] + hat2of8;
		intVal = (key0 << 8) + key1;
		return intVal;
	}

	public static int to2Int(byte byte1, byte byte2) {
		byte[] bytes = new byte[2];
		bytes[0] = byte1;
		bytes[1] = byte2;
		return to2Int(bytes);
	}



}
