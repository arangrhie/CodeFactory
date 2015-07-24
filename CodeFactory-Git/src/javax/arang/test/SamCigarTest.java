package javax.arang.test;

import java.util.ArrayList;

import javax.arang.genome.sam.Sam;

public class SamCigarTest {

	public static void main(String[] args) {
		String cigar = "5S10M1D4I2M8S";
		String seq = "ATCCGCAGGGAAACCAAATATATCCCCGC";
		
		System.out.println(seq + " " + seq.length());
		ArrayList<int[]> cigarArr = Sam.getAllPosition(10, cigar);
		for (int[] posArr : cigarArr) {
			System.out.print(posArr[Sam.ALGN_RANGE_START] + " " + posArr[Sam.ALGN_RANGE_END] + "\t" + seq.substring(posArr[Sam.ALGN_RANGE_START], posArr[Sam.ALGN_RANGE_END] + 1));
			System.out.println("\t" + posArr[Sam.REF_START_POS] + " " + posArr[Sam.REF_END_POS]);
		}
		
		System.out.println("Sam.getEndSoftclip(): " + Sam.getEndSoftclip(cigar));
	}

}
