package javax.arang.genome.indel;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Merge2Indel extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		fm.writeLine("chr\tstart\tend\tref\tobs" +
				"\t" + fr1.getFileName().substring(0, fr1.getFileName().indexOf("_")) +
				"\t" + fr2.getFileName().substring(0, fr2.getFileName().indexOf("_")));

		
		String line1 = fr1.readLine();
		String[] tokens1 = line1.split("\t");
		int pos1start = Integer.parseInt(tokens1[Indel.START]);
		int pos1end = Integer.parseInt(tokens1[Indel.END]);
		String line2 = fr2.readLine();
		String[] tokens2 = line2.split("\t");
		int pos2start = Integer.parseInt(tokens2[Indel.START]);
		int pos2end = Integer.parseInt(tokens2[Indel.END]);
		boolean has1Lines = false;
		
		READ_LOOP : while (true) {
			if (pos1start < pos2start) {
				// tokens1
				Indel.writeLine(fm, tokens1[Indel.CHR], pos1start, pos1end,
						tokens1[Indel.REF], tokens1[Indel.OBS],
						Indel.getGenotype(tokens1[Indel.GENOTYPE]), Indel.NON_OBS);
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
						Indel.NON_OBS, Indel.getGenotype(tokens2[Indel.GENOTYPE]));
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
					Indel.writeLine(fm, tokens1[Indel.CHR], pos1start, pos1end,
							tokens1[Indel.REF], tokens1[Indel.OBS],
							Indel.getGenotype(tokens1[Indel.GENOTYPE]), Indel.getGenotype(tokens2[Indel.GENOTYPE]));
				} else {
					// tokens1
					Indel.writeLine(fm, tokens1[Indel.CHR], pos1start, pos1end,
							tokens1[Indel.REF], tokens1[Indel.OBS],
							Indel.getGenotype(tokens1[Indel.GENOTYPE]), Indel.NON_OBS);
					// tokens2
					Indel.writeLine(fm, tokens2[Indel.CHR], pos2start, pos2end,
							tokens2[Indel.REF], tokens2[Indel.OBS],
							Indel.NON_OBS, Indel.getGenotype(tokens2[Indel.GENOTYPE]));
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
				Indel.writeLine(fm, tokens1[Indel.CHR], pos1start, pos1end,
						tokens1[Indel.REF], tokens1[Indel.OBS],
						Indel.getGenotype(tokens1[Indel.GENOTYPE]), Indel.NON_OBS);
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
						Indel.NON_OBS, Indel.getGenotype(tokens2[Indel.GENOTYPE]));
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
		System.out.println("Usage: java -jar merge2indel.jar <sample1_indel.chr1> <sample2_indel.chr1> <chr1.indel>");
		System.out.println("\t<chr1.indel>: output file name");
		System.out.println("\t*<input>: 2 indel files containing detail counts, Het, Hom.");
		System.out.println("\t*<output>: merged indel table w/ genotype encoded in 0/1/2.");
	}
	
	public static void main(String[] args) {
		if (args.length == 3) {
			new Merge2Indel().go(args[0], args[1], args[2]);
		} else {
			new Merge2Indel().printHelp();
		}
	}
	

}
