package javax.arang.genome.coverage;

import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/***
 * INPUT: output results from base_sort
 * chr	position	ATGC	qual_avg	cov
 * 20	56609659	C	56	13
 * 20	56609660	A	57	13
 * 20	56609661	A	58	10
 * 20	56609662	G	58	14
 * ...
 * 
 * OUTPUT: 
 * chr	range_from	range_to	GC_counts	cov_avg		
 * 20	56609659	56609668	49			...
 * 20	56609669	56609678	56			...
 * 56685559
 * 
 * @author 아랑
 *
 */
public class Coverage extends IOwrapper {


	long cov = 0;
	static int rangeMin;
	static int rangeMax;
	
//	public void go(String inFile, int rangeMin, int rangeMax) {
//		FileReader fr = new FileReader(inFile);
//	
//		long cov = 0;
//		
//		// read file lines
//		while (fr.hasMoreLines()) {
//			String line = fr.readLine().toString();
//			line = line.trim();
//			if (!line.equals("")) {
//				StringTokenizer st = new StringTokenizer(line);
//				String chr = st.nextToken();
//				Integer position = Integer.parseInt(st.nextToken());
//				Character base = st.nextToken().charAt(0);
//				Integer qual = Integer.parseInt(st.nextToken());	// qual
//				Integer coverage = Integer.parseInt(st.nextToken());
//				
//				if (position >= rangeMin && position <= rangeMax && qual > 53 && coverage > 4) {
//					cov++;
//				}
//			}
//		}
//		
//		fr.closeReader();
//
//		System.out.println("Qualified Base Coverage");
//		System.out.println("total_region\tcovered_bases\tcoverage(%)");
//		int region = (rangeMax - rangeMin);
//		System.out.println(region + "\t" + cov + "\t" + (cov * 100/region));
//	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		// read file lines
		while (fr.hasMoreLines()) {
			String line = fr.readLine().toString();
			line = line.trim();
			if (!line.equals("")) {
				StringTokenizer st = new StringTokenizer(line);
				String chr = st.nextToken();
				Integer position = Integer.parseInt(st.nextToken());
				Character base = st.nextToken().charAt(0);
				Integer qual = Integer.parseInt(st.nextToken());	// qual
				Integer coverage = Integer.parseInt(st.nextToken());
				
				if (position >= rangeMin && position <= rangeMax && qual > 53 && coverage > 4) {
					cov++;
				}
			}
		}
		
		System.out.println("Qualified Base Coverage");
		System.out.println("total_region\tcovered_bases\tcoverage(%)");
		int region = (rangeMax - rangeMin);
		System.out.println(region + "\t" + cov + "\t" + (cov * 100/region));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String inFile = "C://Documents and Settings/아랑/바탕 화면/3Platform/g24-2/base_sort/FX_chr20.bas";
		String inFile = "C://Documents and Settings/아랑/바탕 화면/3Platform/2009solexa/base_sort/FX_chr20.bas";
		String outFile = "";
		rangeMin = 56609659;
		rangeMax = 56685559;
		
		if (args.length > 0) {
			inFile = args[0];
			rangeMin = Integer.parseInt(args[1]);
			rangeMax = Integer.parseInt(args[2]);
		}
		new Coverage().go(inFile, outFile);
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		
	}
	

}
