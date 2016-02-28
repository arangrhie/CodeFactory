package javax.arang.phasing;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.Base;
import javax.arang.bed.util.Bed;
import javax.arang.genome.fasta.Seeker;
import javax.arang.phasing.util.PhasedSNP;

public class PhasedBaseToSV extends I2Owrapper {

	private static boolean isHapA = true;
	private static boolean isHapH = false;
	
	@Override
	public void hooker(FileReader frBase, FileReader frSnp, FileMaker fmSV) {
		String line;
		String[] tokens;
		
		// read phased snp
		HashMap<Integer, PhasedSNP> phasedSnps = PhasedSNP.readSNPsStoreSNPs(frSnp, false);
		
		// set fa seeker
		FileReader faReader = new FileReader(scaffoldFaPath);
		Seeker faSeeker = new Seeker(faReader);
		
		// read interval bed
		Bed intervals = null;
		FileReader frBed = null;
		if (hasInterval) {
			frBed = new FileReader(intervalBedPath);
			intervals = new Bed(frBed);
		}
		
		String contig = "";
		int pos = -1;
		int prevPos = 0;
		int maxDepth;
		int totalDepth;
		int delDepth = 0;
		int delTotalDepth = 0;
		String base;
		boolean wasDel = false;
		int posDelFrom = 0;
		float af;
		
		frBase.readLine();	// skip header line
		while (frBase.hasMoreLines()) {
			line = frBase.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[Base.CHR];
			pos = Integer.parseInt(tokens[Base.POS]);
			
			if (hasInterval && !intervals.isInRegion(contig, pos)) {
				continue;
			}
			
			if (!isHapH && !hasInterval && prevPos < pos - 1) {
				fmSV.writeLine(contig + "\t" + prevPos + "\t" + (pos - 1) + "\tNOT_PHASED");
			}
			totalDepth = Base.getTotalDepth(tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D]);
			maxDepth = Base.maxLikelyBaseCov(tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D]);
			af = (float) maxDepth / (float) totalDepth;
			if (maxDepth > 10 || maxDepth > 4 && af > 0.8) {
				// Priority1: when depth > 10, take the maximum likely base
				base = Base.maxLikelyBase(contig, pos, tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D]);
				if (base.equals("D")) {
					if (!wasDel) {
						wasDel = true;
						posDelFrom = pos - 1;
						delDepth = maxDepth;
						delTotalDepth = totalDepth;
					} else {
						// D && wasDel : do nothing
						delDepth = Math.max(maxDepth, delDepth);
						delTotalDepth = Math.max(delTotalDepth, totalDepth);
					}
				} else {// not D
					if (wasDel) {
						writeDel(fmSV, contig, posDelFrom, prevPos, delDepth, delTotalDepth);
						wasDel = false;
					} else {
						writeBase(fmSV, faSeeker, contig, pos, base, maxDepth, totalDepth);
					}
				}
			} else if (maxDepth > 5) {
				base = Base.maxLikelyBase(contig, pos, tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D]);
				if (base.equals("D")) {
					if (!wasDel) {
						wasDel = true;
						posDelFrom = pos - 1;
						delDepth = maxDepth;
						delTotalDepth = totalDepth;
					} else {
						// D && wasDel : do nothing
						delDepth = Math.max(maxDepth, delDepth);
						delTotalDepth = Math.max(delTotalDepth, totalDepth);
					}
				} else {
					if (phasedSnps.containsKey(pos)) {
						// Priority2: when depth > 0, take phased snp if exists
						if (wasDel) {
							writeDel(fmSV, contig, posDelFrom, prevPos, delDepth, delTotalDepth);
							wasDel = false;
						}
						if (!isHapH) {
							if (isHapA && phasedSnps.get(pos).getHaplotypeA().equalsIgnoreCase(base)
								|| !isHapA && phasedSnps.get(pos).getHaplotypeB().equalsIgnoreCase(base)) {
								writeBase(fmSV, faSeeker, contig, pos, (isHapA ? phasedSnps.get(pos).getHaplotypeA() : phasedSnps.get(pos).getHaplotypeB()), maxDepth, totalDepth);	
							} else {
								writeBase(fmSV, faSeeker, contig, pos, base, maxDepth, totalDepth);	
							}
						} else {
							writeBase(fmSV, faSeeker, contig, pos, base, maxDepth, totalDepth);
						}
					} else {
						if (wasDel) {
							writeDel(fmSV, contig, posDelFrom, prevPos, delDepth, delTotalDepth);
							wasDel = false;
						} 
						writeBase(fmSV, faSeeker, contig, pos, base, maxDepth, totalDepth);
					}
				}
			} else {
					// depth <= 5
					// Low depth: will be placed with scaffold bases when no phased snp exists.
					// If it was deletion, then will met the next >5 point of braek
			}
			prevPos = pos;
		}
		int endPos = faSeeker.getEndPos();
		//System.err.println("[DEBUG] :: Reaching end of file : " + pos + "\t" + endPos);
		if (!isHapH && pos < endPos - 1 && !hasInterval) {
			fmSV.writeLine(contig + "\t" + pos + "\t" + (endPos - 1) + "\tNOT_PHASED");
		}
		
		faReader.closeReader();
		if (hasInterval) {
			frBed.closeReader();
		}
	}
	
	private void writeDel(FileMaker fm, String contig, int posFrom, int pos, int delDepth, int totalDepth) {
		fm.writeLine(contig + "\t" + posFrom + "\t" + pos + "\tDELETION\t" + (pos - posFrom) + "\t" + delDepth + "\t" + totalDepth);
	}
	
	public static void writeBaseMulti(FileMaker fm, Seeker faSeeker, String contig, int pos, String base, String maxDepth, int totalDepth) {
		char scaffoldBase = faSeeker.baseAt(pos);
		fm.write(contig + "\t" + (pos - 1) + "\t" + pos + "\tMULTI_ALLELE\t" + scaffoldBase + ">" + base.charAt(0));
		for (int i = 1; i < base.length(); i++) {
			fm.write("|" + base.charAt(i));
		}
		fm.writeLine("\t" + maxDepth + "\t" + totalDepth);
	}
	
	public void writeBase(FileMaker fm, Seeker faSeeker, String contig, int pos, String base, int maxDepth, int totalDepth) {
		if (base.equals("D")) {
			writeDel(fm, contig, (pos-1), pos, maxDepth, totalDepth);
		} else if (base.length() == 2) {
			writeBaseMulti(fm, faSeeker, contig, pos, base, maxDepth + "", totalDepth);
		} else {
			char scaffoldBase = faSeeker.baseAt(pos);
			if (Character.toUpperCase(scaffoldBase) == 'N') {
				// scaffoldBase == N
				fm.writeLine(contig + "\t" + (pos-1) + "\t" + pos + "\tSUBSTITUTION\t" + scaffoldBase + ">" + base + "\t" + maxDepth + "\t" + totalDepth);
			} else if (!base.equals("N") && Character.toUpperCase(scaffoldBase) != base.charAt(0)) {
				// scaffoldBase != N && base != N && scaffoldBase != base
				fm.writeLine(contig + "\t" + (pos-1) + "\t" + pos + "\tSUBSTITUTION\t" + scaffoldBase + ">" + base + "\t" + maxDepth + "\t" + totalDepth);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedBaseToSV.jar <in.base> <phased.snp> <out_prefix> <haplotype> <scaffold.fa> [interval.bed]");
		System.out.println("\t*This is the preliminary test code for building haplotig fasta*");
		System.out.println("\t<in.base>: generated with samToBaseDepth.jar or bamToBaseDepth.jar");
		System.out.println("\t<phased.snp>: CONTIG\tPOS\tHaplotypeA\tHaplotypeB");
		System.out.println("\t<haplotype>: A or B");
		System.out.println("\t<scaffold.fa>: scaffold fa file");
		System.out.println("\t[interval.bed]: only SVs in [interval.bed] will be reported");
		System.out.println("\t\tWhen interval.bed is provided, NOT_PHASED will not be checked.");
		System.out.println("\t<out_prefix>.sv: het sv containing snp, deletion");
		System.out.println("\t#CONTIG\tSTART\tEND\tTYPE\tSCAFFOLD_BASE>PHASED_BASE|LEN\tMAX_DEPTH(x)\tTOTAL_DEPTH(x)");
		System.out.println("\tNOT_PHASED: Regions with no haplotype block.");
		System.out.println("\tDELETION: Deletion in phased block");
		System.out.println("\tSUBSTITUTION: when depth>10 || depth > 4x && af > 0.8, then priority1: maximum likely base out of base coverage, 2: phased.snp allele, 3: no substitution (as it is in scaffold)");
		System.out.println("\t\twhen depth>5 then priority1: phased.snp allele when it equals maximum likely base, 2. maximum likely base out of base coverage, 3: no substitution (as it is in scaffold");
		System.out.println("Arang Rhie, 2015-12-04. arrhie@gmail.com");
	}

	private static String outPrefix;
	private static String scaffoldFaPath;
	private static String haplotype;
	private static String intervalBedPath;
	private static boolean hasInterval = false;
	
	public static void main(String[] args) {
		if (args.length == 5) {
			outPrefix = args[2];
			haplotype = args[3];
			scaffoldFaPath = args[4];
			if (haplotype.equalsIgnoreCase("A")) {
				isHapA = true;
			} else if (haplotype.equalsIgnoreCase("B")) {
				isHapA = false;
			} else if (haplotype.equalsIgnoreCase("H")) {
				isHapH = true;
			} else {
				new PhasedBaseToSV().printHelp();
				System.exit(-1);
			}
			new PhasedBaseToSV().go(args[0], args[1], outPrefix + ".sv");
		} else if (args.length == 6) {
			hasInterval = true;
			intervalBedPath = args[5];
			outPrefix = args[2];
			haplotype = args[3];
			scaffoldFaPath = args[4];
			if (haplotype.equalsIgnoreCase("A")) {
				isHapA = true;
			} else if (haplotype.equalsIgnoreCase("B")) {
				isHapA = false;
			} else if (haplotype.equalsIgnoreCase("H")) {
				isHapH = true;
			} else {
				new PhasedBaseToSV().printHelp();
				System.exit(-1);
			}
			new PhasedBaseToSV().go(args[0], args[1], outPrefix + ".sv");
		} else {
			new PhasedBaseToSV().printHelp();
		}
	}

}
