package javax.arang.bam;

import javax.arang.IO.bam.BamIOwrapper;
import javax.arang.IO.bambasic.BamMaker;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.bam.util.BamRecord;
import javax.arang.bam.util.RefInfo;

public class ConvertQualPhred64To33 extends BamIOwrapper {

	@Override
	public void hooker(BamReader fr, BamMaker fm) {
		
		if (fr.hasMoreAlignmentRecord()) {
			RefInfo ref = fr.getRefInfo();
			fm.write(ref.getRefInfo());
		}
		
		while (fr.hasMoreAlignmentRecord()) {
			BamRecord record = fr.getNextAlignmentRecord();
			byte[] qualByte = record.getQualBytes();
			for (int j = 0; j < qualByte.length; j++) {
				if (qualByte[j] - 31 < 0) {
					throw new RuntimeException("Quality < 31 detected. Input bam file may not be phred+64.");
				}
				qualByte[j] = (byte) (qualByte[j] - 31);
			}
			byte[] recordBytes = record.getRecordBytes();
			System.arraycopy(qualByte, 0, recordBytes, record.OFFSET_QUAL, qualByte.length);
			record.setRecordBytes(recordBytes);
			fm.write(recordBytes);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bamConvertQualPhred64To33.jar <in.bam> <out.bam>");
		System.out.println("Converts quality encoded in phred+64 (old illumina format) to phred+33 (sanger format)");
		System.out.println("Arang Rhie, 2014-02-02. arrhie@gmail.com");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new ConvertQualPhred64To33().go(args[0], args[1]);
		} else {
			new ConvertQualPhred64To33().printHelp();
		}

	}

}
