/**
 * 
 */
package javax.arang.IO.bambasic;

import javax.arang.IO.basic.IOUtil;
import javax.arang.bam.util.Bai;
import javax.arang.bam.util.RefBin;
import javax.arang.samtools.util.BinaryCodec;

/**
 * @author Arang Rhie
 *
 */
public class BaiReader {
	
	Bai bai = new Bai();
	String path;
	
	public BaiReader(String path) {
		this.path = path;
		BinaryCodec codec = new BinaryCodec(path, false);
		String magic = codec.readString(4);
		if (!magic.startsWith("BAI")) {
			System.err.println(path + " is not a valid BAI file.");
			System.exit(-1);
		}
		int n_ref = codec.readInt();
		for (int h = 0; h < n_ref; h++) {
			RefBin refBin = new RefBin();
			int n_bin = codec.readInt();
			for (int j = 0; j < n_bin; j++) {
				long bin = codec.readUInt();
				refBin.addBin(bin);
				int n_chunk = codec.readInt();
				for (int i = 0; i < n_chunk; i++) {
					refBin.addChunk(bin, codec.readLong(), codec.readLong());
				}
			}
			int n_intv = codec.readInt();
			for (int i = 0; i < n_intv; i++) {
				refBin.addIntvIoffset(codec.readLong());
			}
			bai.addRefBin(h, refBin);
		}
		codec.close();
	}
	
	public Bai getBai() {
		return bai;
	}
	
	public String getFileName() {
		return IOUtil.retrieveFileName(path);
	}

}
