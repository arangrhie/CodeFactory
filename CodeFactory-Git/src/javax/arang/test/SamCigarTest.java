package javax.arang.test;

import java.util.ArrayList;

import javax.arang.sam.Sam;

public class SamCigarTest {

	public static void main(String[] args) {
		String cigar = "5S10M2D4I2M7S";
		String seq = "ATCCGCAGGGAAACCAAATATATCCCCGC";
		
		System.out.println(seq + " " + seq.length());
		ArrayList<int[]> cigarArr = Sam.getAllPosition(10, cigar);
		
		for (int[] posArr : cigarArr) {
			System.out.print(posArr[Sam.CIGAR_POS_ALGN_RANGE_START] + " " + posArr[Sam.CIGAR_POS_ALGN_RANGE_END] + "\t" + seq.substring(posArr[Sam.CIGAR_POS_ALGN_RANGE_START], posArr[Sam.CIGAR_POS_ALGN_RANGE_END] + 1));
			System.out.println("\t" + posArr[Sam.CIGAR_POS_REF_START] + " " + posArr[Sam.CIGAR_POS_REF_END] + " " + Sam.getCigarType(posArr[Sam.CIGAR_POS_TYPE]));
		}
		
		System.out.println("Sam.getEndSoftclip(): " + Sam.getEndSoftclip(cigar));
	}

}
