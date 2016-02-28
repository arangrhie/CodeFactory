package javax.arang.test;

import javax.arang.sam.SAMUtil;

public class TestSamUtil {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String cigar = "3S10M1I1M3D1M7S";
		String seq = "ACGTACGTACGTACGTACGTATT";
		String[] seqData = new String[3];
		seqData[0] = cigar;
		seqData[1] = seq;
		
		System.out.println(cigar);
		System.out.println("ACG TACGTACGTA C G T ACGTATT");
		System.out.println("01234567890123456789012");
		System.out.println("Matched Len: " + SAMUtil.getMatchedBases(cigar));
		System.out.println("Mapped Len: " + SAMUtil.getMappedBases(cigar));
		String read = SAMUtil.getRead(60001, seqData, 60009, 60017);
		System.out.println(read);
		
		System.out.println();
		int[] mdi = SAMUtil.getMDI(60001, cigar, 60002, 60013);
		System.out.println("60009-60017 M: " + mdi[0]);
		System.out.println("60009-60017 D: " + mdi[1]);
		System.out.println("60009-60017 I: " + mdi[2]);
	}

}
