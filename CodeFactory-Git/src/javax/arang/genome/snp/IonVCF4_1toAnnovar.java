package javax.arang.genome.snp;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.ANNOVAR;

public class IonVCF4_1toAnnovar extends I2Owrapper {

	final static int CHROM = 0;
	final static int POS = 1;
	final static int REF = 3;
	final static int ALT = 4;
	final static int QUAL = 5;
	final static int FORMAT = 8;
	final static int SAMPLE = 9;
	static int POS_OFFSET = 0;
	
	@Override
	public void hooker(FileReader fr, FileReader refFr, FileMaker fm) {
		
		String header = "Chromosome\tStartPos\tEndPos\tRef\tObserved";
		
		String line;
		String[] tokens;
		line = refFr.readLine();
		tokens = line.split(" ");
		int REF_OFFSET = Integer.parseInt(tokens[ANNOVAR.POS_FROM].substring(0, tokens[ANNOVAR.POS_FROM].indexOf("_")));

		Vector<String> formats = new Vector<String>();
		HashMap<String, String> valueTable = new HashMap<String, String>();
		boolean hasPrintHeader = false;
		
		boolean hadHomopolymer = false; 
		int totalNumVar = 0;
		int droppedVar = 0;
		
		READ_LINE : while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("##FORMAT")) {
				// parse format line
				String id = parseFormat(line, formats);
				fm.writeLine("#" + id + "=" + line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")));
				header = header + "\t" + id;
				continue READ_LINE;
			} else if (line.startsWith("#")) {
				continue READ_LINE;
			}
			if (!hasPrintHeader) {
				fm.writeLine("#AF=Allele Frequency");
				fm.writeLine("#AFF=Allele Frequency with Allelic Unique Starts");
				fm.writeLine(header + "\tAF\tAFF");
				hasPrintHeader = true;
			}
			tokens = line.split("\t");
			if (!ref.equals("") && !tokens[CHROM].equals(ref)) {
				continue READ_LINE;
			}
			
			int pos = Integer.parseInt(tokens[POS]);
			pos += POS_OFFSET;
			if (pos < REF_OFFSET) {
				droppedVar++;
				continue READ_LINE;
			}
			
			String newLine = tokens[CHROM] + "\t" + pos + "\t" + pos + "\t" + 
									tokens[REF] + "\t" + tokens[ALT];
			valueTable = parseFormatSample(tokens[FORMAT], tokens[SAMPLE]);
			String AF = "";
			for (int i = 0; i < formats.size(); i++) {
				if (formats.get(i).equals("GT")) {
					String value = valueTable.get("GT");
					if (value.equals("Het")) {
						int DDP = 0;
						int DFDP = 0; 
						String[] dpTokens = valueTable.get("AD").split(",");
						DDP = Integer.parseInt(dpTokens[1]) - Integer.parseInt(dpTokens[0]);
						dpTokens = valueTable.get("AST").split(",");
						DFDP = Integer.parseInt(dpTokens[1]) - Integer.parseInt(dpTokens[0]);
						if (DDP < -90 || DFDP < -35) {
							droppedVar++;
							continue READ_LINE;
						}

						// filter out homopolymer region > 5 6 bases
						if (pos < Homopolymer.getTo() && hadHomopolymer) {
							droppedVar++;
							continue READ_LINE;
						}
						Homopolymer.seek(pos - REF_OFFSET, refFr);
						if (Homopolymer.isInHomopolymerRegion(pos, tokens[REF].charAt(0), tokens[ALT].charAt(0))) {
							if ((tokens[REF].equals("A") || tokens[REF].equals("T")) && Homopolymer.getHomLen() > 6) {
								hadHomopolymer = true;
								droppedVar++;
								continue READ_LINE;
							}
							if ((tokens[REF].equals("G") || tokens[REF].equals("C")) && Homopolymer.getHomLen() > 5) {
								hadHomopolymer = true;
								droppedVar++;
								continue READ_LINE;
							}
//							AF = AF + "\t" + Homopolymer.getHomLen();
						}
						hadHomopolymer = false;
						
						value = valueTable.get("AMQV");
						String[] qual = value.split(",");
						if (Integer.parseInt(qual[1]) < 20) {
							droppedVar++;
							continue READ_LINE;
						}
						
						value = valueTable.get("AST");
						String[] freq = value.split(",");
						if (Integer.parseInt(freq[0]) < 10 || Integer.parseInt(freq[1]) < 15) {
							droppedVar++;
							continue READ_LINE;
						}
					} else {
						value = valueTable.get("AST");
						String[] freq = value.split(",");
						if (Integer.parseInt(freq[1]) < 10) {
							droppedVar++;
							continue READ_LINE;
						}
					}
				}
				if (formats.get(i).equals("AD") || formats.get(i).equals("AST")) {
					String value = valueTable.get(formats.get(i));
					String[] freq = value.split(",");
					float frequency = ((float) Integer.parseInt(freq[1]) * 100) / (Integer.parseInt(freq[0]) + Integer.parseInt(freq[1]));
					if (frequency < 20) {
						droppedVar++;
						continue READ_LINE;
					}
					AF = AF + "\t" + String.format("%,.2f", frequency);  
				}
				newLine = newLine + "\t" + valueTable.get(formats.get(i));
			}
			
			fm.writeLine(newLine + AF);
			totalNumVar++;
		}
		
		System.out.println(droppedVar + "\tdropped");
		System.out.println("Total # of Variants\t" + totalNumVar);
		
	}

	private HashMap<String, String> parseFormatSample(String format,
			String sample) {
		HashMap<String, String> valueTable = new HashMap<String, String>();
		String[] formatTokens = format.split(":");
		String[] sampleTokens = sample.split(":");
		for (int i = 0; i < formatTokens.length; i++) {
			String form = formatTokens[i];
			String value = sampleTokens[i];
			if (form.equals("GT")) {
				value = (value.equals("1/1") ? "Hom" : "Het");
			}
			valueTable.put(form, value);
		}
		return valueTable;
	}

	/***
	 * Returns the id in the line
	 * @param line
	 * @param formatTable
	 * @return
	 */
	private String parseFormat(String line, Vector<String> formatTable) {
		StringTokenizer st = new StringTokenizer(line, "#<>=,\"");
		String id = "";
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			if (token.startsWith("ID"))	{
				id = st.nextToken();
				formatTable.add(id);
				return id;
			}
		}
		return "";
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcf4_1toAnnovar.jar <in.vcf> <ref.fa> <ref> [OFFSET=0]");
		System.out.println("\t<out>: ANNOVAR input format file");
	}
	
	static String ref = "";
	
	public static void main(String[] args) {
		if (args.length == 2) {
			new IonVCF4_1toAnnovar().go(args[0], args[1], args[0].replace(".vcf", ".snp"));
		} else if (args.length == 3) {
			ref = args[2];
			new IonVCF4_1toAnnovar().go(args[0], args[1], args[0].replace(".vcf", ".snp"));
		} else if (args.length == 4) {
			ref = args[2];
			POS_OFFSET = Integer.parseInt(args[3]);
			new IonVCF4_1toAnnovar().go(args[0], args[1], args[0].replace(".vcf", ".snp"));
		} else {
			new IonVCF4_1toAnnovar().printHelp();
		}
	}

}
