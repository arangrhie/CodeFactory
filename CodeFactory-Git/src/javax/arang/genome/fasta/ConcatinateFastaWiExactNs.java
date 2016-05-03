package javax.arang.genome.fasta;

import java.util.ArrayList;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ConcatinateFastaWiExactNs extends IOwrapper {

	@Override
	public void hooker(FileReader frFirstFa, FileMaker fm) {
		String line;
		String outFaName = fm.getFileName().substring(0, fm.getFileName().lastIndexOf(".fa"));
		
		// write header
		fm.writeLine(">" + outFaName);
		
		// copy first fa
		frFirstFa.readLine();	// skip the read name part
		while (frFirstFa.hasMoreLines()) {
			line = frFirstFa.readLine();
			fm.writeLine(line);
		}
		
		// write the rest
		FileReader fr;
		for (int i = 0; i < numNlist.size(); i++) {
			fm.writeLine(getNs(numNlist.get(i)));
			fr = new FileReader(inFaList.get(i));
			fr.readLine();	// skip header line
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				fm.writeLine(line);
			}	
		}
	}

	private String getNs(Integer numNs) {
		StringBuffer nString = new StringBuffer();
		for (int i = 0; i < numNs; i++) {
			nString.append("N");
		}
		return nString.toString();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaConcatinateFastaWiExactNs.jar <out.fa> <in.fa> [<num_Ns> <in.fa> ... ]");
		System.out.println("\t<out.fa>: 1 fasta file containing all the <in.fa> files and Ns");
		System.out.println("\t<in.fa>: regular fa file, with 1 fasta in 1 file" );
		System.out.println("\t<num_Ns>: INTEGER. Number of Ns to put between <in.fa>s.");
		System.out.println("Arang Rhie, 2016-03-05. arrhie@gmail.com");
	}

	private static ArrayList<Integer> numNlist = new ArrayList<Integer>();
	private static ArrayList<String> inFaList = new ArrayList<String>();	// from 2nd <in.fa>
	
	public static void main(String[] args) {
		if (args.length == 2) {
			System.out.println("<in.fa> will be <out.fa>. Nothing is done.");
		} else if (args.length > 2) {
			for (int i = 2; i < args.length; i++) {
				if (i%2==0) {
					numNlist.add(Integer.parseInt(args[i]));
				} else {
					inFaList.add(args[i]);
				}
			}
			new ConcatinateFastaWiExactNs().go(args[1], args[0]);
		} else {
			new ConcatinateFastaWiExactNs().printHelp();
		}
	}

}
