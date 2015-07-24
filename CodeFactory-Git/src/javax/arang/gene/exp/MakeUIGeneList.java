package javax.arang.gene.exp;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class MakeUIGeneList extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		StringTokenizer st;
		int originalTotalLength = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			st = new StringTokenizer(line);
			String gene = st.nextToken();
			originalTotalLength += Integer.parseInt(st.nextToken());
			st.nextToken();	// num starts
			String chr = st.nextToken();
			String strand = st.nextToken();
			st.nextToken();	// num ends
			Vector<Integer[]> exons = getExons(st.nextToken(), st.nextToken());
			
		}
	}
	
	private Vector<Integer[]> getExons(String startPos, String endPos) {
		Vector<Integer[]> exons = new Vector<Integer[]>();
		StringTokenizer st1 = new StringTokenizer(startPos, ",");
		StringTokenizer st2 = new StringTokenizer(endPos, ",");
		while (st1.hasMoreTokens()) {
			Integer start = Integer.parseInt(st1.nextToken());
			Integer end = Integer.parseInt(st2.nextToken());
			Integer[] pos = new Integer[2];
			pos[0] = start;
			pos[1] = end;
			exons.add(pos);
		}
		return exons;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			new MakeUIGeneList().printHelp();
		} else {
			new MakeUIGeneList().go(args[0], args[1]);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Create Union Intersection (UI) gene model from Union gene model");
		System.out.println("UI gene model: a composite gene-level region of interest\n" +
				"\tconsisting of the union of constitutive exons\n" +
				"\tthat do not overlapwith coding exons of other genes.");
		System.out.println("Usage: java -jar makeUIgenelist.jar <inFile> <outFile>");
		System.out.println("\t<inFile>: a line containing gene\tlength\t# of exons\tchr\tstrand\t# of exons\tstart positions(,seperated)\tend positions\n" +
				"uc001aaa.3      1652    3       chr1    +       3       11873,12612,13220,      12227,12721,14409,");
		System.out.println("\t<outFile>: a line containing gene without any overlap");
		
	}

}
