package javax.arang.genome.snp;

import java.io.IOException;

import javax.arang.IO.I2OBufferedWrapper;
import javax.arang.IO.basic.BufferedFileReader;
import javax.arang.IO.basic.FileMaker;
import javax.arang.annovar.util.ANNOVAR;

public class AddHomopoly extends I2OBufferedWrapper {

	@Override
	public void hooker(BufferedFileReader fr1, BufferedFileReader fr2, FileMaker fm) {
		String line;
		String[] tokens;
		
		fm.writeLine(fr1.readLine() + "\tHomopolyLen\tHomopolyType\tHomopolyBases\tGClen\tGCtype\tGCbases");
		
		line = fr2.readLine();
		tokens = line.split(" ");
		int offset = Integer.parseInt(tokens[ANNOVAR.POS_FROM].substring(0, tokens[ANNOVAR.POS_FROM].indexOf("_")));
		int from = offset;
		int to = offset;
		int gcFrom = offset;
		int gcTo = offset;
		String homo = "";
		String gcRich = "";

		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			if (!line.startsWith("chr")) {
				line = "chr" + line;
			}
			tokens = line.split("\t");
			if (tokens[ANNOVAR.REF].length() > 1 || tokens[ANNOVAR.ALT].length() > 1) {
				// skip deletions and insertions
				continue;
			}
			char ref = tokens[ANNOVAR.REF].charAt(0);
			int pos = Integer.parseInt(tokens[ANNOVAR.POS_FROM]);
			if (pos < to) {
				if (gcRich.length() > 3 || homo.length() > 3) {
					fm.write(line + "\t");
					if (homo.length() > gcRich.length()) {
						fm.write( 
								homo.length() + "\tPoly" + ref + " " + "(" + from + "-" + to + ")\t" + homo + "\t");
					} else {
						fm.write("\t\t\t");
					}
					if (gcRich.length() > 3) {
						fm.writeLine(gcRich.length() + "\tGCcontents" + " " + "(" + gcFrom + "-" + gcTo + ")\t" + gcRich);
					} else {
						fm.writeLine("\t\t");
					}
				} else {
					fm.writeLine(line);
				}
				continue;
			}
			homo = "";
			pos -= offset;
			seek(pos, fr2);
			// check homopolymer stretch
			for (int i = leftBuffer.length() - 1; i>0; i--) {
				if(leftBuffer.charAt(i) == ref) {
					homo = leftBuffer.charAt(i) + homo;
				} else {
					from = offset + pos - (leftBuffer.length() - i) + 1;
					break;
				}
			}
			for (int i = 0; i < rightBuffer.length(); i++) {
				if (rightBuffer.charAt(i) == ref) {
					homo = homo + rightBuffer.charAt(i);
				} else {
					to = offset + pos + i - 1;
					break;
				}
			}
			
			// check GC rich region
			gcRich = "";
			for (int i = leftBuffer.length() - 1; i>0; i--) {
				if(leftBuffer.charAt(i) == 'G' || leftBuffer.charAt(i) == 'C') {
					gcRich = leftBuffer.charAt(i) + gcRich;
				} else {
					gcFrom = offset + pos - (leftBuffer.length() - i) + 1;
					break;
				}
			}
			gcRich += ref;
			for (int i = 1; i < rightBuffer.length(); i++) {
				if (rightBuffer.charAt(i) == 'G' || rightBuffer.charAt(i) == 'C') {
					gcRich = gcRich + rightBuffer.charAt(i);
				} else {
					gcTo = offset + pos + i - 1;
					break;
				}
			}
			
			if (gcRich.length() > 3 || homo.length() > 3) {
				fm.write(line + "\t");
				if (homo.length() > gcRich.length()) {
					fm.write( 
							homo.length() + "\tPoly" + ref + " " + "(" + from + "-" + to + ")\t" + homo + "\t");
				} else {
					fm.write("\t\t\t");
				}
				if (gcRich.length() > 3) {
					fm.writeLine(gcRich.length() + "\tGCcontents" + " " + "(" + gcFrom + "-" + gcTo + ")\t" + gcRich);
				} else {
					fm.writeLine("\t\t");
				}
			} else {
				fm.writeLine(line);
			}
		}
	}
	
	int readChars = 0;
	String leftBuffer = "";
	String rightBuffer = "";
	int prevPos = 0;
	private void seek(int pos, BufferedFileReader fr) {
		pos--;	// 1-base to 0-base coordinate
		if (readChars > pos) {
			int movingSeqLen = pos - prevPos;
			leftBuffer = leftBuffer + rightBuffer.substring(0, movingSeqLen);
			try {
				rightBuffer = rightBuffer.substring(movingSeqLen) + fr.seekForward(80*3, 3);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error occured!!");
			}
			prevPos = pos;
			return;
		}
		
		String line;
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			readChars += line.length();
			if (readChars > pos) {
				leftBuffer = leftBuffer + line.substring(0, line.length() - (readChars - pos));
				try {
					rightBuffer = line.substring(line.length() - (readChars - pos)) + fr.seekForward(line.length()*3, 3);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Error occured!!");
				}
				break;
			}
			leftBuffer = leftBuffer + line;
			if (leftBuffer.length() > 40) {
				leftBuffer = leftBuffer.substring(leftBuffer.length() - 40);
			}
		}
		prevPos = pos;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar addHomopoly.jar <inFile> <ref.fa> <outFile>");
		System.out.println("\t<inFile>: ANNOVAR input file");
		System.out.println("\t<ref.fa>: reference fasta file");
		System.out.println("\t<outFile>: ANNOVAR input file, with homopoly info added");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new AddHomopoly().go(args[0], args[1], args[2]);
		} else {
			new AddHomopoly().printHelp();
		}
	}

}
