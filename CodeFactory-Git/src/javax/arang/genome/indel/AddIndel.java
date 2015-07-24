package javax.arang.genome.indel;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class AddIndel extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		
		String line1 = fr1.readLine();
		String[] tokens1 = line1.split("\t");
		fm.writeLine(line1 + "\t" + fr2.getFileName().substring(0, fr2.getFileName().indexOf("_")));
		String padd = "0";
		for (int i = Indel.NOTE; i< tokens1.length - 1; i++) {
			padd = padd + "\t0";
		}
		line1 = fr1.readLine();
		tokens1 = line1.split("\t");
		
		int pos1start = Integer.parseInt(tokens1[Indel.START]);
		int pos1end = Integer.parseInt(tokens1[Indel.END]);
		String line2 = fr2.readLine();
		boolean has1Lines = true;
		boolean has2lines = false;
		String[] tokens2 = line2.split("\t");
		int pos2start = 0;
		int pos2end = 0;
		
		if (tokens2.length > 2) {
			pos1start = Integer.parseInt(tokens2[Indel.START]);
			pos2end = Integer.parseInt(tokens2[Indel.END]);
			has2lines = true;
		}
		
		READ_LOOP : while (has2lines) {
			if (pos1start < pos2start) {
				// tokens1
				Indel.writeLine(fm, line1, Indel.NON_OBS);
				if (!fr1.hasMoreLines()) {
					has1Lines = false;
					break READ_LOOP;
				}
				line1 = fr1.readLine();
				tokens1 = line1.split("\t");
				pos1start = Integer.parseInt(tokens1[Indel.START]);
				pos1end = Integer.parseInt(tokens1[Indel.END]);
			}
			
			else if (pos1start > pos2start) {
				// tokens2
				Indel.writeLine(fm, tokens2[Indel.CHR], pos2start, pos2end,
						tokens2[Indel.REF], tokens2[Indel.OBS],
						padd, Indel.getGenotype(tokens2[Indel.GENOTYPE]));
				if (!fr2.hasMoreLines()) {
					has1Lines = true;
					break READ_LOOP;
				}
				line2 = fr2.readLine();
				tokens2 = line2.split("\t");
				pos2start = Integer.parseInt(tokens2[Indel.START]);
				pos2end = Integer.parseInt(tokens2[Indel.END]);
			}
			
			else if(pos1start == pos2start) {
				if (pos1end == pos2end && tokens1[Indel.OBS].equals(tokens2[Indel.OBS])) {
					Indel.writeLine(fm, line1, Indel.getGenotype(tokens2[Indel.GENOTYPE]));
				} else {
					// tokens1
					Indel.writeLine(fm, line1, Indel.NON_OBS);
					// tokens2
					Indel.writeLine(fm, tokens2[Indel.CHR], pos2start, pos2end,
							tokens2[Indel.REF], tokens2[Indel.OBS],
							padd, Indel.getGenotype(tokens2[Indel.GENOTYPE]));
				}
				
				if (!fr1.hasMoreLines()) {
					has1Lines = false;
					break READ_LOOP;
				}
				line1 = fr1.readLine();
				tokens1 = line1.split("\t");
				pos1start = Integer.parseInt(tokens1[Indel.START]);
				pos1end = Integer.parseInt(tokens1[Indel.END]);
				if (!fr2.hasMoreLines()) {
					has1Lines = true;
					break READ_LOOP;
				}
				line2 = fr2.readLine();
				tokens2 = line2.split("\t");
				pos2start = Integer.parseInt(tokens2[Indel.START]);
				pos2end = Integer.parseInt(tokens2[Indel.END]);
			}
		}
		
		if (has1Lines) {
			while(true) {
				Indel.writeLine(fm, line1, Indel.NON_OBS);
				if (!fr1.hasMoreLines()) {
					has1Lines = false;
					break;
				}
				line1 = fr1.readLine();
				tokens1 = line1.split("\t");
				pos1start = Integer.parseInt(tokens1[Indel.START]);
				pos1end = Integer.parseInt(tokens1[Indel.END]);
			}
		} else {
			while(true) {
				Indel.writeLine(fm, tokens2[Indel.CHR], pos2start, pos2end,
						tokens2[Indel.REF], tokens2[Indel.OBS],
						padd, Indel.getGenotype(tokens2[Indel.GENOTYPE]));
				if (!fr2.hasMoreLines()) {
					has1Lines = true;
					break;
				}
				line2 = fr2.readLine();
				tokens2 = line2.split("\t");
				pos2start = Integer.parseInt(tokens2[Indel.START]);
				pos2end = Integer.parseInt(tokens2[Indel.END]);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar addIndel.jar <chr.indel.table> <sampleN_indel.chr> <output>");
		System.out.println("\t<input>: add an indel file on indel table.");
		System.out.println("\t<output>: indel table w/ genotype encoded as 0/1/2.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new AddIndel().go(args[0], args[1], args[2]);
		} else {
			new AddIndel().printHelp();
		}

	}

}
