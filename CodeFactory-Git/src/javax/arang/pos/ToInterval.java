package javax.arang.pos;

import java.util.HashSet;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToInterval extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String seqName = "";
		double posSeq = -1;
		double posAsm = -1;
		
		String prevSeqName = "";
		double prevPosSeq = -1;
		double prevPosAsm = -1;
		
		double asmPosFrom = 0;
		double asmPosTo = 0;
		
		double intervalAsm = 0;
		double intervalSeq = 0;
		
		boolean isFirst = true;
		
		HashSet<Double> posAsmSet = new HashSet<Double>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			seqName = tokens[Pos.SEQ];
			posSeq = Double.parseDouble(tokens[Pos.POS_SEQ]);
			posAsm = Double.parseDouble(tokens[Pos.VALUE]);
			
			if (!seqName.equals(prevSeqName)) {
				// New read
				posAsmSet = new HashSet<Double>();;

				// First line/read
				if (isFirst) {
					prevSeqName = seqName;
					prevPosSeq = posSeq;
					prevPosAsm = posAsm;
					isFirst = false;
					continue;
				}
			} else {
				// Same read
				if (posAsmSet.contains(posAsm)) {
					intervalAsm = 0;
					asmPosFrom = posAsm;
					asmPosTo = posAsm;
				} else {
					if (prevPosAsm > posAsm) {
						asmPosFrom = posAsm;
						asmPosTo = prevPosAsm;
					} else {
						asmPosFrom = prevPosAsm;
						asmPosTo = posAsm;
					}
					intervalAsm = asmPosTo - asmPosFrom;
					posAsmSet.add(posAsm);
				}
				intervalSeq = posSeq - prevPosSeq;
				System.out.println(seqName + "\t" +
						String.format("%.0f", asmPosFrom) + "\t" + String.format("%.0f", asmPosTo) + "\t" +
						String.format("%.0f", intervalAsm) + "\t" + String.format("%.0f", intervalSeq));
			}
			
			prevSeqName = seqName;
			prevPosSeq = posSeq;
			prevPosAsm = posAsm;
			
		}
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar posToInterval.jar <in.pos>");
		System.out.println("\t<in.pos>: Generated with meryl-lookup -dump");
		System.out.println("\t\tSeqName\tPosInSeq\tPosInAsm(Value)");
		System.out.println("\t<stdout>: Interval between PosInAsm and PosInSeq");
		System.out.println("\t\tFormat: SeqName\tPosAsmFrom[1-base]\tPosAsmTo[1-base]\tIntervalInAsm\tIntervalInSeq");
		System.out.println("\t\tIntervalInAsm: 0 if seen > 1 in the same SeqName (read)");
		System.out.println("\t\t\tMultiple values are placed with a delimitor : .");
		System.out.println("Arang Rhie, 2019-02-28. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToInterval().go(args[0]);
		} else {
			new ToInterval().printHelp();
		}
	}

}
