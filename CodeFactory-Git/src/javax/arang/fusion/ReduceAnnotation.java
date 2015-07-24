package javax.arang.fusion;

import java.util.ArrayList;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ReduceAnnotation extends IOwrapper {

	static int numSamples;
	static int GTF_CHR;
	static int GTF_FEATURE;
	static int GTF_START;
	static int GTF_END;
	static int GTF_SCORE;
	static int GTF_STRAND;
	static int GTF_FRAME;
	
	static final int DON_CHR = 0;
	static final int DON_POS = 1;
	static final int ACC_CHR = 2;
	static final int ACC_POS = 3;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String prevFusion = "";	// chr_don_chr_acc
		String fusionKey = "";
		String doner ="";
		String acceptor = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				fm.writeLine(line);
				continue;
			}
			tokens = line.split("\t");
			fusionKey = tokens[DON_CHR] + "_" + tokens[DON_POS] + "_" + tokens[ACC_CHR] + "_" + tokens[ACC_POS];
			
			// new fusion?
			if (!prevFusion.equals(fusionKey)) {
				writeFusion(fm, tokens, doner, acceptor);
				doner = "";
				acceptor = "";
				
				// get doner (or acceptor)
				if (isDoner(Integer.parseInt(tokens[DON_POS]), Integer.parseInt(tokens[GTF_START]), Integer.parseInt(tokens[GTF_END]))) {
					doner = retrieveAnnotation(tokens);
				} else {
					acceptor = retrieveAnnotation(tokens);
				}
			} else {
				if (isDoner(Integer.parseInt(tokens[DON_POS]), Integer.parseInt(tokens[GTF_START]), Integer.parseInt(tokens[GTF_END]))) {
					doner = retrieveAnnotation(tokens);
				} else {
					acceptor = retrieveAnnotation(tokens);
				}	
			}
			prevFusion = fusionKey;
		}
	}
	
	private String retrieveAnnotation(String[] tokens) {
		StringBuffer annotation = new StringBuffer();
		for (int i = 0; i < 25; i++) {
			annotation.append(tokens[GTF_CHR + i] + "\t");
		}
		return annotation.toString().trim();
	}
	
	private boolean isDoner(int pos, int start, int end) {
		if (start <= pos && pos <= end) {
			return true;
		}
		return false;
	}

	private void writeFusion(FileMaker fm, String[] tokens, String doner,
			String acceptor) {
		for (int i = 0; i < (numSamples + 4); i++) {
			fm.write(tokens[i] + "\t");
		}
		fm.writeLine(doner + "\t" + acceptor);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fusionReduceAnnotation.jar <chimera_table.fusion_ann> <num_samples>");
		System.out.println("\tFormat <chimera_table.fusion_ann> with doner acceptor pairs to be presented in one line");
	}

	
	public static void main(String[] args) {
		if (args.length == 2) {
			numSamples = Integer.parseInt(args[1]);
			GTF_CHR = numSamples;
			GTF_FEATURE = GTF_CHR + 2;
			GTF_START = GTF_CHR + 3;
			GTF_END = GTF_CHR + 4;
			GTF_SCORE = GTF_CHR + 5;
			GTF_STRAND = GTF_CHR + 6;
			GTF_FRAME = GTF_CHR + 7;
			new ReduceAnnotation().go(args[0], args[0] + "_red");
		} else {
			new ReduceAnnotation().printHelp();
		}
	}

}
