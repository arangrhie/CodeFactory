package javax.arang.phasing;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.Allele;
import javax.arang.base.util.Base;
import javax.arang.bed.util.Bed;
import javax.arang.genome.fasta.Seeker;

public class PhasedBaseToSV2ndCycle extends I2Owrapper {

	@Override
	public void hooker(FileReader frBase, FileReader frFa, FileMaker fmSV) {
		String line;
		String[] tokens;
		
		// set fa seeker
		Seeker faSeeker = new Seeker(frFa);
		
		// print options
		System.err.println("SUBSTITUTION: >" + substitutionDepthThreshold + "x, AF>" + substitutionAlleleFrequencyThreshold);
		System.err.println("MULTI_ALLELE: >" + multiDepthThreshold + "x, AF>" + multiAlleleFrequencyThreshold + " | Del AF>" + multiDeletionAlleleFrequencyThreshold);

		// read interval bed
		Bed intervals = null;
		FileReader frBed = null;
		if (hasInterval) {
			System.err.println("-bed " + intervalBedPath);
			frBed = new FileReader(intervalBedPath);
			intervals = new Bed(frBed);
		}
		
		
		String contig = "";
		int pos = -1;
		String depths;
		int totalDepth;
		String bases;
		int numOver25;
		Allele allele;
		
		frBase.readLine();	// skip header line
		
		while (frBase.hasMoreLines()) {
			line = frBase.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[Base.CHR];
			pos = Integer.parseInt(tokens[Base.POS]);
			
			if (hasInterval && !intervals.isInRegion(contig, pos)) {
				continue;
			}
			
			totalDepth = Base.getTotalDepth(tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D]);
			if (totalDepth < substitutionDepthThreshold)	continue;
			
			allele = new Allele(tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D], multiAlleleFrequencyThreshold, multiDeletionAlleleFrequencyThreshold);
			numOver25 = allele.getNumAlleles();
			if (numOver25 == 1 && allele.getMaxAlleleFreq() > substitutionAlleleFrequencyThreshold) {
				// Substitution
				bases = allele.getMaxAlleleFreqBase() + "";
				depths = allele.getAlleleDepthsInString();
				writeBaseSubstitution(fmSV, faSeeker, contig, pos, haplotype, bases, depths, totalDepth);
			}
			
			if (totalDepth < multiDepthThreshold) 	continue;
			if (numOver25 > 1) {
				// Multiallele: >=2 alleles with af >0.25
				bases = allele.getAllelesInString();
				depths = allele.getAlleleDepthsInString();
				writeBaseMulti(fmSV, faSeeker, contig, pos, haplotype, numOver25, bases, depths, totalDepth);
			}
			
		}
		
		if (hasInterval) {
			frBed.closeReader();
		}
	}
	
	private void writeBaseSubstitution(FileMaker fm, Seeker faSeeker, String contig, int pos,
			String haplotype, String base, String depth, int totalDepth) {
			char scaffoldBase = faSeeker.baseAt(pos);
			if (Character.toUpperCase(scaffoldBase) == 'N' || (!base.equals("N") && Character.toUpperCase(scaffoldBase) != base.charAt(0))) {
				fm.writeLine(contig + "\t" + (pos-1) + "\t" + pos + "\tSUBSTITUTION\t" + 
						haplotype + "\t" + 1 + "\t" + scaffoldBase + ">" + base + "\t" + depth + "\t" + totalDepth);
			}
	}

	private void writeBaseMulti(FileMaker fm, Seeker faSeeker, String contig, int pos,
			String haplotype,
			int numBases, String bases, String depths, int totalDepth) {
		char scaffoldBase = faSeeker.baseAt(pos);
		fm.writeLine(contig + "\t" + (pos - 1) + "\t" + pos + "\tMULTI_ALLELE\t" +
				haplotype + "\t" + numBases + "\t" + scaffoldBase + ">" + bases + "\t" + depths + "\t" + totalDepth);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedBaseToSV2ndCycle.jar <in.M.base or in.H.base> <scaffold.fa> <haplotype> <out.sv> "
				+ "[-dps sub_depth_threshold] [-dpm multi_depth_threshold] "
				+ "[-afs sub_af] [-afm multi_af] [-afmd multi_af_d] [-bed interval.bed]");
		System.out.println("\tFind multiallelic sites with");
		System.out.println("\t\tSUBSTITUTION:  depth >= [sub_depth_threshold]x, AF > [sub_af]");
		System.out.println("\t\tMULTI_ALLELE: depth >= [multi_depth_threshold]x, AF > [multi_af] for ACGT, AF > [multi_af_d] for D");
		System.out.println("\t\tsub_depth_threshold: DEFAULT=5");
		System.out.println("\t\tmulti_depth_threshold: DEFAULT=20");
		System.out.println("\t\tsub_af: DEFAULT=0.8");
		System.out.println("\t\tmulti_af: DEFAULT=0.25");
		System.out.println("\t\tmulti_af_d: DEFAULT=0.40");
		System.out.println("\t\tinterval.bed: any bed file. when given, only regions within interval.bed will be reported.");
		System.out.println("\t<out.sv> will be in format:");
		System.out.println("\t\tCONTIG\tSTART\tEND\tMULTI_ALLELE\tHAPLOTYPE\tNUM_BASES\tBASES\tDEPTHS\tTOTAL_DEPTH");
		System.out.println("\t\tDeletions will be reported in 1-base position coordinate.");
		System.out.println("Arang Rhie, 2015-12-11. arrhie@gmail.com");
	}

	private static String haplotype;
	private static String intervalBedPath;
	private static int substitutionDepthThreshold = 5;
	private static int multiDepthThreshold = 20;
	private static float substitutionAlleleFrequencyThreshold = 0.80f;
	private static float multiAlleleFrequencyThreshold = 0.25f;
	private static float multiDeletionAlleleFrequencyThreshold = 0.40f;
	private static boolean hasInterval = false;
	
	public static void main(String[] args) {
		if (args.length == 4) {
			haplotype = args[2];
			new PhasedBaseToSV2ndCycle().go(args[0], args[1], args[3]);
		} else if (args.length > 4) {
			haplotype = args[2];
			for (int i = 4; i < args.length - 1; i+=2) {
				if (args[i].equals("-bed")) {
					hasInterval = true;
					intervalBedPath = args[i + 1];
				} else if (args[i].equalsIgnoreCase("-dps")) {
					substitutionDepthThreshold = Integer.parseInt(args[i + 1]);
				} else if (args[i].equalsIgnoreCase("-dpm")) {
					multiDepthThreshold = Integer.parseInt(args[i + 1]);
				} else if (args[i].equalsIgnoreCase("-afs")) {
					substitutionAlleleFrequencyThreshold = Float.parseFloat(args[i + 1]);
				} else if (args[i].equalsIgnoreCase("-afm")) {
					multiAlleleFrequencyThreshold = Float.parseFloat(args[i + 1]);
				} else if (args[i].equalsIgnoreCase("-afmd")) {
					multiDeletionAlleleFrequencyThreshold = Float.parseFloat(args[i + 1]);
				} else {
					System.err.println("Un-recognized arguement: " + args[i]);
					new PhasedBaseToSV2ndCycle().printHelp();
					System.exit(-1);
				}
			}
			new PhasedBaseToSV2ndCycle().go(args[0], args[1], args[3]);
		} else {
			new PhasedBaseToSV2ndCycle().printHelp();
		}
	}

}
