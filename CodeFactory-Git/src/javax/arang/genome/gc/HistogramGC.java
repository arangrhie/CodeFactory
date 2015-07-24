package javax.arang.genome.gc;

import java.util.StringTokenizer;

import javax.arang.IO.basic.FileReader;

public class HistogramGC {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inFile = "C://Documents and Settings/아랑/바탕 화면/3Platform/gc_contents/gc_coverage.txt";
		new HistogramGC().go(inFile);
	}
	
	public void go(String inFile) {
		FileReader fr = new FileReader(inFile);
		int[] ionHisto = new int[101];
		int[] ionCount = new int[101];
		
		int[] gaHisto = new int[101];
		int[] gaCount = new int[101];
		
		
		while(fr.hasMoreLines()) {
			String line = fr.readLine().toString();
			StringTokenizer st = new StringTokenizer(line);
			int idx = Integer.parseInt(st.nextToken());
			int ion = Integer.parseInt(st.nextToken());
			int ga = Integer.parseInt(st.nextToken());
			
			ionHisto[idx] += ion;
			ionCount[idx] += 1;
			
			gaHisto[idx] += ga;
			gaCount[idx] += 1;
		}
		
		System.out.println("Histogram of GC");
		System.out.println("GC_contents\tIontorrent\tGA");
		for (int i = 0; i < 101; i++) {
			System.out.print(i + "\t");
			if (ionCount[i] > 0) {
				System.out.print((ionHisto[i]/ionCount[i]) + "\t");
			} else {
				System.out.print("0\t");
			}
			
			if (gaCount[i] > 0) {
				System.out.println(gaHisto[i]/gaCount[i] + "");
			} else {
				System.out.println("0");
			}
			
		}
		
	}

}
