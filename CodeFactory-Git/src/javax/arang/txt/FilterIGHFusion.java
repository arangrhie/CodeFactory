package javax.arang.txt;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class FilterIGHFusion extends IOwrapper {

	static int sampleNum = 101;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		StringBuffer sampleInfo = new StringBuffer();
		StringBuffer gtfInfo = new StringBuffer();
		
		line = fr.readLine();
		tokens = line.split("\t");
		fm.write(tokens[0]);
		for (int i = 1; i < sampleNum + 4; i++) {
			fm.write("\t" + tokens[i]);
		}
		fm.writeLine("\tDONOR\tACCEPTOR");
		String donor = "";
		String acceptor = "";
		boolean isDonor = true;
		READ_LOOP : while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			sampleInfo = new StringBuffer(tokens[0]);
			for (int i = 1; i < sampleNum + 4; i++) {
				sampleInfo.append("\t" + tokens[i]);
			}
			gtfInfo = new StringBuffer();
			donor = "";
			acceptor = "";
			for (int i = sampleNum + 4; i < tokens.length-1; i++) {
				if (tokens[i].equals("DONOR"))	isDonor = true;
				else if (tokens[i].equals("ACCEPTOR"))	isDonor = false;
				if (tokens[i].equals("gene_name")) {
					if (tokens[i+1].startsWith("IGH"))	continue READ_LOOP;
					if (isDonor)	donor = tokens[i+1];
					else acceptor = tokens[i+1];
				}
				gtfInfo.append("\t" + tokens[i]);
			}
			gtfInfo.append("\t" + tokens[tokens.length - 1]);
			fm.writeLine(sampleInfo.toString() + "\t" + donor + "\t" + acceptor + "\t" + gtfInfo.toString());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtFilterIGHFusion.jar <in_fusion_ann> <out.txt> <sample_num>");
		System.out.println("\tRemoves the lines containing gene_name starting with IGH");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new FilterIGHFusion().go(args[0], args[1]);
		} else {
			new FilterIGHFusion().printHelp();
		}
	}

}
