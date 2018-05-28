package javax.arang.kmer;

import java.util.ArrayList;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class HistToPloidyDepth extends Rwrapper {

	private int effectiveMinDepth = 0;
	private ArrayList<Integer> ploidyDepth = new ArrayList<Integer>();
	private ArrayList<Integer> ploidyBoundary = new ArrayList<Integer>();
	
	/***
	 * idx=0 is ploidy 1
	 * @return
	 */
	public ArrayList<Integer> getPloidyDepth() {
		return ploidyDepth;
	}
	
	public ArrayList<Integer> getPloidyBoundary() {
		return ploidyBoundary;
	}
	
	@Override
	public void hooker(FileReader fr) {
		
		ArrayList<Integer> ploidyDepth = getPloidyDepthTable(fr);
		
		if (!printD) {
			System.out.println("ploidy\tdepth\tboundary");
			System.out.println(0 + "\t" + 0 + "\t" + effectiveMinDepth);
			for (int ploidy = 1; ploidy <= ploidyDepth.size(); ploidy++) {
				System.out.println(ploidy + "\t" + ploidyDepth.get(ploidy - 1) + "\t" + ploidyBoundary.get(ploidy - 1));
			}
		}
	}
	
	public void printErrPloidyBounds() {
		System.err.println("ploidy\tdepth\tboundary");
		System.err.println(0 + "\t" + 0 + "\t" + effectiveMinDepth);
		for (int ploidy = 1; ploidy <= ploidyDepth.size(); ploidy++) {
			System.err.println(ploidy + "\t" + ploidyDepth.get(ploidy - 1) + "\t" + ploidyBoundary.get(ploidy - 1));
		}
	}
	public int getEffectiveMinDepth() {
		return effectiveMinDepth;
	}
	
	public ArrayList<Integer> getPloidyDepthTable(FileReader fr) {
		
		ArrayList<Double> ploidyCount = new ArrayList<Double>();
		
		String line;
		String[] tokens;
		int depth;
		double count;
		double prevCount = Double.MAX_VALUE;
		
		double d;
		double prevD = -1;
		double prevDPrime = Double.MAX_VALUE;
		double dPrime = 0;
		
		int lineCount = 0;
		boolean keepTracking = false;
		boolean hasHaploidDepthAdded = false;
		
		double minCount = 0;
		
		int countMin = 0;
		
		int estimatedHaploidIdx = -1;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			depth = Integer.parseInt(tokens[0]);
			count = Double.parseDouble(tokens[1]);
			if (lineCount == 0) {
				prevD = count - prevCount;
				prevCount = count;
				lineCount++;
				continue;
			}

			d = count - prevCount;	// slope delta d
			if (printD) {
				System.out.println(depth + "\t" + d);
			}
			dPrime = d - prevD;
			if (d * prevD < 0) {
				if (d > prevD) {
					countMin++;
					if (countMin == 1) {
						minCount = prevCount;
						effectiveMinDepth = depth - 1;
						keepTracking = true;
						System.err.println("Start sampling here: " + effectiveMinDepth + ", " + prevCount);
					}
					else if (countMin > 1) {
						keepTracking = false;
						if (hasHaploidDepthAdded) {
							System.err.println("Haploid peak divergent enough. Kick out: " + ploidyDepth.get(estimatedHaploidIdx) + ", " + ploidyCount.get(estimatedHaploidIdx));
							ploidyDepth.remove(estimatedHaploidIdx);
							ploidyCount.remove(estimatedHaploidIdx);
							hasHaploidDepthAdded = false;
						}
						System.err.println("Next min here: " + (depth - 1) + ", " + prevCount);
						ploidyBoundary.add(depth - 1);
					}
				} else if (d < prevD) {
					// kick out the previous local maxima
					System.err.println("Next max here: " + (depth - 1) + ", " + prevCount);
					if (ploidyCount.size() > 0 && ploidyDepth.get(ploidyDepth.size() - 1) + 5 > depth) {
						if (ploidyCount.get(ploidyCount.size() - 1) < prevCount) {
							System.err.println("Kick out local maxima: " + ploidyDepth.get(ploidyDepth.size() - 1) + ", " + ploidyCount.get(ploidyCount.size() - 1));
							ploidyDepth.remove(ploidyDepth.size() - 1);
							ploidyCount.remove(ploidyCount.size() - 1);
							ploidyBoundary.remove(ploidyBoundary.size() - 1);
							ploidyDepth.add(depth - 1);
							ploidyCount.add(prevCount);
						} // else don't add this depth - 1
					} else {
						ploidyDepth.add(depth - 1);
						ploidyCount.add(prevCount);
					}
				}
			} else if (keepTracking && !hasHaploidDepthAdded) {
				if (d > 0 && d < prevD && dPrime * prevDPrime > 0 && prevDPrime < dPrime) {
					System.err.println("Estimated haploid peak here: " + depth + ", " + count);
					estimatedHaploidIdx = ploidyDepth.size();
					ploidyDepth.add(depth);
					ploidyCount.add(count);
					hasHaploidDepthAdded = true;
				}

			}

			if (count < minCount) {
				System.err.println("Stop sampling here: " + depth + ", " + count);
				if (hasHaploidDepthAdded && ploidyDepth.size() == 2) {
					ploidyBoundary.add((ploidyDepth.get(0) + ploidyDepth.get(1))/ 2);
				} else {
					ploidyBoundary.add(depth);
				}
				break;
			}
			prevCount = count;
			prevD = d;
			prevDPrime = dPrime;
		}
		return ploidyDepth;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar kmerHistToPloidyDepth.jar <in.hist> [-d]");
		System.out.println("\t<in.hist>: 1st column being kmer occurrence (depth), 2nd column being counts");
		System.out.println("\t<sysout>: Estimated occurrence (depth) for each ploidy");
		System.out.println("\t\tploidy\tdepth\tboundary");
		System.out.println("\t[-d]: print the differenciated <in.hist>");
		System.out.println("Arang Rhie, 2018-03-30. arrhie@gmail.com");
	}

	private static boolean printD = false;
	public static void main(String[] args) {
		if (args.length == 2) {
			printD = true;
			new HistToPloidyDepth().go(args[0]);
		} else if (args.length == 1) {
			new HistToPloidyDepth().go(args[0]);
		} else {
			new HistToPloidyDepth().printHelp();
		}
	}

}
