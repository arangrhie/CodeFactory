package javax.arang.sam;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToFragmentBed extends IOwrapper{

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String readID;
		String chr;
		String start;
		String end;
		int tlen;
		int startInt;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			readID = tokens[Sam.QNAME];
			chr = tokens[Sam.RNAME];
			start = tokens[Sam.POS];
			tlen = Integer.parseInt(tokens[Sam.TLEN]);
			
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			while (!readID.equals(tokens[Sam.QNAME])) {
				readID = tokens[Sam.QNAME];
				chr = tokens[Sam.RNAME];
				start = tokens[Sam.POS];
				tlen = Integer.parseInt(tokens[Sam.TLEN]);
				
				line = fr.readLine();
				tokens = line.split(RegExp.TAB);
			}
			
			end = tokens[Sam.POS];
			
			if(tlen < 0) {
				start = end;
				tlen = Integer.parseInt(tokens[Sam.TLEN]);
			}
			startInt = Integer.parseInt(start) - 1;
			fm.writeLine(chr + "\t" + startInt + "\t" + (startInt + tlen) + "\t" + tlen + "\t" + readID);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bamToFragmentBed.jar <paired.properly.mq60.sam> <out.bed>");
		System.out.println("\t<in.sam>: generated from \"$SAMTOOLS view -f 0x2 -F 0x800 hg38_BACpoolHS2_$poolN.trim.bam | awk \'$5==\"60\" && $7==\"=\"\' -");
		System.out.println("Arang Rhie, 2015-05-10. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToFragmentBed().go(args[0], args[1]);
		} else {
			new ToFragmentBed().printHelp();
		}
	}

}
