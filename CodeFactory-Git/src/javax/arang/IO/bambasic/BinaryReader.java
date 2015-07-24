/**
 * 
 */
package javax.arang.IO.bambasic;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.arang.IO.basic.IOUtil;

/**
 * @author Arang Rhie
 *
 */
public class BinaryReader {

	RandomAccessFile ir;
	String path;
	
	public BinaryReader(String path) {
		try {
			this.path = path;
			ir = new RandomAccessFile(path, "r");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public String getFileName() {
		return IOUtil.retrieveFileName(path);
	}
	
	public String getDir() {
		return IOUtil.retrieveDirectory(path);
	}
	
	public int readInt() {
		byte[] intByte = new byte[4];
		try {
			ir.read(intByte);
			return ByteBuffer.wrap(intByte).getInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("Cannot read integer value.");
		System.exit(-1);
		return -1;
	}
	
	public String readChars(int numBytes) {
		byte[] charBytes = new byte[numBytes];
		StringBuffer val = new StringBuffer();
		try {
			ir.read(charBytes);
			for (int i = 0; i < numBytes; i++) {
				val.append((char)charBytes[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return val.toString();
	}
	
	public int readByte() {
		try {
			return ir.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public void skipBytes(int numBytes) {
		try {
			ir.seek(ir.getFilePointer() + numBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int seekEnter() {
		long pos;
		int enterPos = 0;
		try {
			pos = ir.getFilePointer();
			char c = '0';
			ir.seek(pos + 21*4);
			for (int i = 0; i < 10; i++ ) {
				c = (char) ir.read();
				if (c == '\n') {
					enterPos = 21*4 + i;
				}
			}
			ir.seek(pos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return enterPos;
	}
	
	public boolean hasMoreBytes() {
		try {
			long pos = ir.getFilePointer();
			if (ir.read() == -1)	return false;
			ir.seek(pos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public byte[] readBytes(int numBytes) {
		byte[] readBytes = new byte[numBytes];
		try {
			ir.read(readBytes, 0, numBytes);
		} catch (IOException e) {
			// TODO: handle exception
		}
		return readBytes;
	}
	
	public long toUnsignedInt(byte[] inBytes) {
		long val = 0;
		int numBytes = inBytes.length;
		StringBuffer bits = new StringBuffer();

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
		//System.out.println(bits.reverse());
		return val;
	}
	
	public static String printBytes(byte inByte) {
		StringBuffer bits = new StringBuffer();
		int toUint = (int) inByte;
		//System.out.println("toUint:\t" + toUint);
		for (int i = 0; i < 8; i++) {
			bits.append(toUint%2);
			toUint = toUint >>> 1;
		}
		bits = bits.reverse();
		//System.out.println(bits);
		return bits.toString();
	}
	
	public void close() {
		try {
			ir.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BinaryReader br = new BinaryReader(args[0]);
		byte[] uInt = br.readBytes(1);
		System.out.println("ID1\t" + br.toUnsignedInt(uInt));
		System.out.println("ID2\t" + br.toUnsignedInt(br.readBytes(1)));
		System.out.println("CM\t" + br.toUnsignedInt(br.readBytes(1)));
		System.out.println("FLG\t" + br.toUnsignedInt(br.readBytes(1)));
		uInt = br.readBytes(4);
		System.out.println("MTIME\t" + br.toUnsignedInt(uInt));
		System.out.println("XFL\t" + br.toUnsignedInt(br.readBytes(1)));
		System.out.println("OS\t" + br.toUnsignedInt(br.readBytes(1)));
		long xlen = br.toUnsignedInt(br.readBytes(2));
		System.out.println("XLEN\t" + xlen);
		System.out.println("SI1\t" + br.toUnsignedInt(br.readBytes(1)));
		System.out.println("SI2\t" + br.toUnsignedInt(br.readBytes(1)));
		uInt = br.readBytes(2);
		System.out.println("SLEN\t" + br.toUnsignedInt(uInt));
		uInt = br.readBytes(2);
		long bsize = br.toUnsignedInt(uInt) + 1;
		System.out.println("BSIZE\t" + bsize);
		int deflatedSize = (int)(bsize - 18 - 8);
		System.out.println("deflateSize (bsize - 18 - 8):\t" + deflatedSize);
		final Inflater inflater = new Inflater(true); // GZIP mode
		inflater.reset();
		byte[] compressedBlock = br.readBytes(deflatedSize);
		inflater.setInput(compressedBlock);
		uInt = br.readBytes(4);
//		System.out.println("CRC32 printBytes:\t" + printBytes(uInt[3]) + " " + printBytes(uInt[2])
//				 + " " + printBytes(uInt[1]) + " " + printBytes(uInt[0]));
		long expectedCrc = br.toUnsignedInt(uInt);
		System.out.println("CRC32\t" + expectedCrc);
		int iSize = (int) br.toUnsignedInt(br.readBytes(4));
		System.out.println("ISIZE\t" + iSize);
		byte[] uncompressedBlock = new byte[iSize];
		try {
			int inflatedBytes = inflater.inflate(uncompressedBlock, 0, iSize);
			System.out.println("Inflated Bytes:\t" + inflatedBytes);
			if (inflatedBytes != iSize) {
				System.out.println("ERROR: Did not inflate expected amount");
			}
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		final CRC32 crc32 = new CRC32();
		crc32.reset();
		crc32.update(uncompressedBlock, 0, iSize);
		final long crc = crc32.getValue();
		System.out.println("crc:\t" + crc);
        if (crc != expectedCrc) {
        	System.out.println("CRC mismatch");
        }
		System.out.println((char) uncompressedBlock[0] + " " + (char) uncompressedBlock[1]
				 + " " + (char) uncompressedBlock[2] + " " + (char) uncompressedBlock[3]);
	}

}
