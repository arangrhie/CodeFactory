package javax.arang.genome.gc;

import java.util.StringTokenizer;

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
public class FrameGC {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inFile = "C://Documents and Settings/아랑/바탕 화면/3Platform/g24-2/base_sort/FX_chr20.bas";
//		String inFile = "C://Documents and Settings/아랑/바탕 화면/3Platform/2009solexa/base_sort/FX_chr20.bas";
		int rangeMin = 56609659;
		int rangeMax = 56685559;
		int offset = 100;
		
		if (args.length > 0) {
			inFile = args[0];
			rangeMin = Integer.parseInt(args[1]);
			rangeMax = Integer.parseInt(args[2]);
		}
		new FrameGC().go(inFile, rangeMin, rangeMax, offset);
	}
	
	int[] gcArray;
	int[] covArray;
	int[] covCounts;
	
	public void go(String inFile, int rangeMin, int rangeMax, int offset) {
		FileReader fr = new FileReader(inFile);
	
		int numFrames = (int) Math.ceil((rangeMax - rangeMin) / offset) + 1;
		System.out.println("numFrames: " + numFrames);
		gcArray = new int[numFrames];
		covArray = new int[numFrames];
		covCounts = new int[numFrames];
		String chr = "";
		
		// read file lines
		while (fr.hasMoreLines()) {
			String line = fr.readLine().toString();
			line = line.trim();
			if (!line.equals("")) {
				StringTokenizer st = new StringTokenizer(line);
				chr = st.nextToken();
				Integer position = Integer.parseInt(st.nextToken());
				Character base = st.nextToken().charAt(0);
				st.nextToken();	// qual
				Integer coverage = Integer.parseInt(st.nextToken());
				
				if (position >= rangeMin && position <= rangeMax) {
					addToFrame(((position - rangeMin) / offset), getGC(base), coverage);
				}
			}
		}
		
		fr.closeReader();

		System.out.println("GC FRAMING");
		System.out.println("chr\trange_from\trange_to\tgc_count\tcov_avg");
		for (int i = 0; i < numFrames; i++) {
			System.out.println(chr + "\t"
					+ (rangeMin + (offset * i)) + "\t"
					+ (rangeMin + (offset * (i + 1)) - 1) + "\t"
					+ gcArray[i] + "\t" + (covArray[i]/covCounts[i]));
		}
		
	}
	
	private void addToFrame(int frameIdx, int gcCount, int coverage) {
		gcArray[frameIdx] += gcCount;
		covArray[frameIdx] += coverage;
		covCounts[frameIdx] += 1;
	}

	private int getGC(char base) {
		switch(base) {
		case 'G': return 1;
		case 'C': return 1;
		}
		return 0;
	}

}
