package javax.arang.sam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FlagTranslator {
	
	public static void printFlag(int flag) {
		System.out.print("0x1\t== ");
		if (SAMUtil.isAligned(flag)) {
			System.out.println("1: Templage having multiple segments in sequencing (has read1, read2)");
		} else {
			System.out.println("0: Templage having multiple segments in sequencing");
		}
		System.out.print("0x2\t== ");
		if (SAMUtil.isBothAligned(flag)) {
			System.out.println("1: Each segment properly aligned according to the aligner (read1 and read2 aligned)");
		} else {
			System.out.println("0: One segment or non is aligned (read1 or read2 aligned, but not both)");
		}
		System.out.print("0x4\t== ");
		if (SAMUtil.isUnmapped(flag)) {
			System.out.println("1: Segment unmapped (unmapped)");
		} else {
			System.out.println("0: Segment mapped (mapped)");
		}
		System.out.print("0x8\t== ");
		if (SAMUtil.isNextUnmapped(flag)) {
			System.out.println("1: Next segment in the template unmapped (pair unmapped)");
		} else {
			System.out.println("0: Next segment in the template mapped (pair mapped)");
		}
		System.out.print("0x10\t== ");
		if (SAMUtil.isReverseComplemented(flag)) {
			System.out.println("1: SEQ being reverse complemented (- strand)");
		} else {
			System.out.println("0: SEQ is not reverse complemented (+ strand)");
		}
		System.out.print("0x20\t== ");
		if (SAMUtil.isNextReverseComplemented(flag)) {
			System.out.println("1: SEQ of the next segment in the template being reversed (- strand)");
		} else {
			System.out.println("0: SEQ of the next segment in the template not reversed (+ strand)");
		}
		System.out.print("0x40\t== ");
		if (SAMUtil.isPair1(flag)) {
			System.out.println("1: The first segment in the template (read 1)");
		} else {
			System.out.println("0: Not the first segment in the template (read 2?)");
		}
		System.out.print("0x80\t== ");
		if (SAMUtil.isPair2(flag)) {
			System.out.println("1: The last segment in the template (read 2)");
		} else {
			System.out.println("0: Not the last segment in the template (read 1?)");
		}
		System.out.print("0x100\t== ");
		if (SAMUtil.isSecondaryAlignment(flag)) {
			System.out.println("1: Secondary alignment");
		} else {
			System.out.println("0: Not secondary. Primary alignment");
		}
		System.out.print("0x200\t== ");
		if (SAMUtil.isUnderQual(flag)) {
			System.out.println("1: Not passing quality controls");
		} else {
			System.out.println("0: Passing quality controls, or NA");
		}
		System.out.print("0x400\t== ");
		if (SAMUtil.isDuplicate(flag)) {
			System.out.println("1: PCR or optical duplicate");
		} else {
			System.out.println("0: Not duplicate or NA");
		}
		System.out.print("0x800\t== ");
		if (SAMUtil.isDuplicate(flag)) {
			System.out.println("1: Supplementary alignment, part of chimeric alignment");
		} else {
			System.out.println("0: Not supplementary (chimeric) alignment");
		}
		System.out.println();
		if (SAMUtil.isPrimaryAlignment(flag)) {
			System.out.println("0x900\t== 0: Primary alignment");
		}
		if (SAMUtil.isPair1(flag) && SAMUtil.isPair2(flag)) {
			System.out.println("0x40 && 0x80 set: Read is part of linear template, neither the first not the last.");
		}
		if (!SAMUtil.isPair1(flag) && !SAMUtil.isPair2(flag)) {
			System.out.println("0x40 && 0x80 unset: Index of the read int the template is unknown, non-linear template or index is lost in data processing(??)");
		}
	}

	public static void main(String[] args) throws IOException {
		int flag;
		System.out.println("Usage: java -jar flagTranslator.jar [flag]");
		System.out.println("\t[flag] could be 1 or more flags, seperated with a white space (bar).");
		System.out.println("\tIf no [flag] is provided, just type a flag and hit enter to see.");
		System.out.println("\tStop this program with Ctrl+C.");
		System.out.println("Arang Rhie, 2014-12-11. arrhie@gmail.com");
		System.out.println();
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				flag = Integer.parseInt(args[i]);
				printFlag(flag);
			}
		} else {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				System.out.print("Flag: ");
				flag = Integer.parseInt(br.readLine());
				printFlag(flag);
				System.out.println();
			}
		}
	}

}
