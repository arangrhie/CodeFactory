/**
 * 
 */
package javax.arang.bam.util;

import java.io.FileNotFoundException;

import javax.arang.samtools.util.BinaryCodec;

/**
 * @author Arang Rhie
 *
 */
public class ParseBai {

	public void go(String path) throws FileNotFoundException {
		
//		int bin = region2Bin(10180, 10190);
//		System.out.println("Bin: " + bin);
		
		BinaryCodec codec = new BinaryCodec(path, false);
		System.out.println("magic: " + codec.readString(4));
		int n_ref = codec.readInt();
		System.out.println("n_ref: " + n_ref);
		System.out.println();
		for (int h = 0; h < n_ref; h++) {
			System.out.println();
			int n_bin = codec.readInt();
			System.out.println("n_ref: " + h + " n_bin: " + n_bin);
			for (int j = 0; j < n_bin; j++) {
				long bin = codec.readUInt();
				int n_chunk = 0;
//				if (j < 2) {
					n_chunk = codec.readInt();
					System.out.println("  bin: " + bin + " | n_chunk: " + n_chunk);
//					System.out.println("  n_chunk: " + n_chunk);
					for (int i = 0; i < n_chunk; i++) {
						int uOffsetBeg = codec.readUShort();
						long cOffsetBeg = codec.read6ByteLong();
						int uOffsetEnd = codec.readUShort();
						long cOffsetEnd = codec.read6ByteLong();
						if (i < 5) {
							System.out.println("\tchunk " + i + ": " + cOffsetBeg + " " + uOffsetBeg + " - " + cOffsetEnd + " " + uOffsetEnd);
						}
					}
//				} else {
//					codec.readUInt();
//					n_chunk = codec.readInt();
//					for (int i = 0; i < n_chunk; i++) {
//						codec.readUShort();
//						codec.read6ByteLong();
//						codec.readUShort();
//						codec.read6ByteLong();
//					}
//				}
			}
			int n_intv = codec.readInt();
			System.out.println("n_intv: " + n_intv);
			System.out.print("ioffset:");
			for (int i = 0; i < n_intv; i++) {
				if (i < 5) {
					int uOffsetBeg = codec.readUShort();
					long cOffsetBeg = codec.read6ByteLong();
					System.out.print(" | " + cOffsetBeg + " " + uOffsetBeg);
				} else {
					codec.readLong();
				}
			}
			System.out.println();
		}
		
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		new ParseBai().go(args[0]);
	}

}
