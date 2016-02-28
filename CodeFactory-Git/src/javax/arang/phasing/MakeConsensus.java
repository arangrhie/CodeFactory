package javax.arang.phasing;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.fasta.Seeker;
import javax.arang.genome.fasta.Writer;
import javax.arang.phasing.util.PhasedSV;

public class MakeConsensus extends I2Owrapper {

	@Override
	public void hooker(FileReader frSV, FileReader frFA, FileMaker fmFa) {
		FileMaker fmBed = new FileMaker(outPrefix + ".bed");
		
		String line;
		String[] tokens;
		Seeker faSeeker = new Seeker(frFA);
		Writer newFaWriter = new Writer(fmFa);
		
		String contig;
		int posNewFaPrevBlockStart = 0;
		int posNewFa = 0;	// ready to write (0-based)
		int posFrom = 0;
		int posTo = 0;
		int posOldFa = 0;
		int len;
		boolean hasPhasedBlock = false;
		while (frSV.hasMoreLines()) {
			line = frSV.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[PhasedSV.CONTIG];
			posFrom = Integer.parseInt(tokens[PhasedSV.START]);
			posTo = Integer.parseInt(tokens[PhasedSV.END]);
			len = posTo - posFrom;
			
			if (posOldFa < posFrom) {
				newFaWriter.write(faSeeker.getBases(posNewFa, posFrom - posOldFa));
				posNewFa += (posFrom - posOldFa);
				posOldFa = posFrom;
			}
			
			if (tokens[PhasedSV.TYPE].equals("NOT_PHASED")) {
				// copy scaffold into new fa
				newFaWriter.write(faSeeker.getBases(posFrom, len));
				if (hasPhasedBlock) {
					fmBed.writeLine(contig + "\t" + posNewFaPrevBlockStart + "\t" + posNewFa);
				}
				hasPhasedBlock = true;
				posNewFa += len;
				posNewFaPrevBlockStart = posNewFa;
			} else if (tokens[PhasedSV.TYPE].equals("SUBSTITUTION")) {
				
			} else if (tokens[PhasedSV.TYPE].equals("DELETION")) {
				
			}
			
			posOldFa = posTo;
			
			
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingMakeConsensus.jar <in.sv> <in.scaffold.fasta> <out_prefix>");
		System.out.println("\t<in.sv>: generated with phasingPhasedBaseToSV.jar");
		System.out.println("\t<in.scaffold.fa>: original scaffold fasta tamplate used for phasing");
		System.out.println("\t<out_prefix>.fa: re-constructed haplotig");
		System.out.println("\t<out_prefix>.bed: regions phased. the rest of <in.sv> NOT_PHASED region.");
		System.out.println("\t\tNOT_PHASED region will be a copy of <in.scaffold.fa>.");
		System.out.println("Arang Rhie, 2015-12-02. arrhie@gmail.com");
	}

	private static String outPrefix;
	public static void main(String[] args) {
		if (args.length == 3) {
			outPrefix = args[2];
			new MakeConsensus().go(args[0], args[1], outPrefix + ".fa");
		} else {
			new MakeConsensus().printHelp();
		}
	}

}
