package javax.arang.bed;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class SummarizeTelo extends Rwrapper {

	private static FileReader frFai;
	
	@Override
	public void hooker(FileReader fr) {
		ArrayList<String> chrs = new ArrayList<String>();
		HashMap<String, Integer> chrTelos = new HashMap<String, Integer>();
		
		String line;
		String[] tokens;
		
		while (frFai.hasMoreLines()) {
			line = frFai.readLine();
			tokens = line.split(RegExp.TAB);
			String chr = tokens[0];
			chrs.add(chr);
			chrTelos.put(chr, 0);
		}
		
		int start, end, fai;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			start = Integer.parseInt(tokens[1]);
			end   = Integer.parseInt(tokens[2]);
			fai   = Integer.parseInt(tokens[3]);
			if (start < 1000) {
				chrTelos.put(tokens[0], chrTelos.get(tokens[0]) + 1);
			} else if (end > (fai - 1000)) {
				chrTelos.put(tokens[0], chrTelos.get(tokens[0]) + 2);
			}
		}
		
		for (int i = 0; i < chrs.size(); i++) {
			String chr = chrs.get(i);
			System.out.println(chr + "\t" + chrTelos.get(chr));
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar -Xmx256m bedSummarizeTelo.jar in.telo.bed in.fa.fai");
		System.err.println("  in.telo.bed  output from seqtk telo");
		System.err.println("  in.fa.fai    fai index from smtools; list of chromosome names");
		System.err.println("");
		System.err.println("  sysout       chr  telo. telo 1=p, 2=q, 3=both");
		System.err.println("2023-10-26. Arang Rhie");
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			new SummarizeTelo().printHelp();
		} else {
			frFai = new FileReader(args[1]);
			new SummarizeTelo().go(args[0]);
		}

	}

}
