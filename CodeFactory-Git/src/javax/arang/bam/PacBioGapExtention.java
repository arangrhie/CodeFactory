package javax.arang.bam;

import javax.arang.IO.bam.BamRwrapper;
import javax.arang.IO.bambasic.BamReader;

public class PacBioGapExtention extends BamRwrapper {


	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bamPacBioGapExtention.jar <in.bam> <out.bed> <gap.bed>");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void hooker(BamReader fr) {
		// TODO Auto-generated method stub
		
	}

}
