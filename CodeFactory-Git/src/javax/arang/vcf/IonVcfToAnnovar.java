package javax.arang.vcf;

import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class IonVcfToAnnovar extends IOwrapper {


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new IonVcfToAnnovar().go(args[0], args[0] + ".snp");
		} else {
			new IonVcfToAnnovar().printHelp();
			System.exit(1);
		}
	}

	private static final int COUNT_A = 0;
	private static final int COUNT_C = 1;
	private static final int COUNT_G = 2;
	private static final int COUNT_T = 3;
	private static final int COUNT_SNP = 4;
	private static final int COUNT_OTHERS = 5;
	private static final int MAPQ = 6;
	
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		StringTokenizer st;
		
		FileMaker indelFm = new FileMaker(fm.getDir(), fm.getFileName().replace("snp", "indel"));
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				// skip
				continue;
			}

			st = new StringTokenizer(line);
			String chr = st.nextToken();
			chr = chr.replace("chr", "");
			Integer pos = Integer.parseInt(st.nextToken());
			st.nextToken();	// ID
			String ref = st.nextToken();	// REF
			String obs = st.nextToken();	// ALT
			st.nextToken();	// qual
			st.nextToken();	// FILTER
			String info = st.nextToken();	// INFO
			if (info.startsWith("INDEL")) {
				indelFm.writeLine(chr + "\t" + pos + "\t" + ref + "\t" + obs + "\t" + ((ref.length() > obs.length()) ? "Deletion\t" : "Insertion\t") + getHomHet(line));
				continue;
			}
			int[] counts = parseInfo(info, getIntVal(ref), getIntVal(obs.substring(0, 1)));
			
			float af = (float)counts[COUNT_SNP]*100 / (counts[COUNT_SNP] + counts[COUNT_OTHERS]);
			String type = getSnpType(af);
			fm.writeLine(chr + "\t" + pos + "\t" + (pos + ref.length() - 1) + "\t" + ref + "\t" + obs + "\tsnp\t"
					+ counts[COUNT_A] + "\t" + counts[COUNT_C] + "\t" + counts[COUNT_G] + "\t" + counts[COUNT_T] + "\t"
					+ counts[COUNT_SNP] + "\t" + counts[COUNT_OTHERS] + "\t" + type + "\t" + String.format("%,.2f", af) + "\t" + counts[MAPQ]);
		}
	}

	private String getHomHet(String info) {
		if (info.contains("/")) {
			String genoType = info.substring(info.lastIndexOf("/") - 1, info.lastIndexOf("/") + 2);
			if (genoType.equals("1/1")) return "Hom";
			else return "Het";
		}
		else return "Het?";
	}

	private Integer getIntVal(String base) {
		if (base.equals("A"))	return COUNT_A;
		if (base.equals("C"))	return COUNT_C;
		if (base.equals("G"))	return COUNT_G;
		if (base.equals("T"))	return COUNT_T;
		System.out.println("Base is " + base);
		return -1;
	}

	private String getSnpType(float af) {
		if (af < 20) {
			return "";
		} else if (af < 80) {
			return "Het";
		} else {
			return "Hom";
		}
	}

	private int[] parseInfo(String info, Integer ref, Integer obs) {
		int[] counts = new int[7];
		StringTokenizer st = new StringTokenizer(info, ";");
		int totalCount = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.startsWith("DP4")) {
				StringTokenizer dpTokens = new StringTokenizer(token.substring(token.indexOf("=") + 1), ",");
				counts[ref] = Integer.parseInt(dpTokens.nextToken()) + Integer.parseInt(dpTokens.nextToken());
				counts[obs] = Integer.parseInt(dpTokens.nextToken()) + Integer.parseInt(dpTokens.nextToken());
				int others = totalCount - counts[ref] - counts[obs];
				if (ref == COUNT_A) {
					if (obs == COUNT_C) {
						counts[COUNT_G] = others/2;
						counts[COUNT_T] = others/2;
					} else if (obs == COUNT_G) {
						counts[COUNT_C] = others/2;
						counts[COUNT_T] = others/2;
					} else if (obs == COUNT_T) {
						counts[COUNT_C] = others/2;
						counts[COUNT_G] = others/2;
					}
				} else if (ref == COUNT_C) {
					if (obs == COUNT_A) {
						counts[COUNT_G] = others/2;
						counts[COUNT_T] = others/2;
					} else if (obs == COUNT_G) {
						counts[COUNT_A] = others/2;
						counts[COUNT_T] = others/2;
					} else if (obs == COUNT_T) {
						counts[COUNT_A] = others/2;
						counts[COUNT_G] = others/2;
					}
				} else if (ref == COUNT_G) {
					if (obs == COUNT_A) {
						counts[COUNT_C] = others/2;
						counts[COUNT_T] = others/2;
					} else if (obs == COUNT_C) {
						counts[COUNT_A] = others/2;
						counts[COUNT_T] = others/2;
					} else if (obs == COUNT_T) {
						counts[COUNT_A] = others/2;
						counts[COUNT_C] = others/2;
					}
				} else if (ref == COUNT_T) {
					if (obs == COUNT_A) {
						counts[COUNT_G] = others/2;
						counts[COUNT_C] = others/2;
					} else if (obs == COUNT_G) {
						counts[COUNT_A] = others/2;
						counts[COUNT_C] = others/2;
					} else if (obs == COUNT_C) {
						counts[COUNT_A] = others/2;
						counts[COUNT_G] = others/2;
					}
				}
				counts[COUNT_SNP] = counts[obs];
				counts[COUNT_OTHERS] = others + counts[ref];
			} else if (token.startsWith("DP")) {
				totalCount = Integer.parseInt(token.substring(token.indexOf("=") + 1));
			} else if (token.startsWith("MQ")) {
				counts[MAPQ] = Integer.parseInt(token.substring(token.indexOf("=") + 1));
			}
		}
		return counts;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcf2annovar.jar <VCF file>");
		System.out.println("Convert a vcf file to ANNOVAR input format. Optimized for iontorrent suit.");
	}

}
