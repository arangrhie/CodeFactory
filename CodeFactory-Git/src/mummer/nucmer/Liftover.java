package mummer.nucmer;

import java.util.HashSet;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Liftover extends R2wrapper {

	private static boolean isRefRegion = true;
	private static String region = "";
	public static void main(String[] args) {
		if (args.length == 4) {
			if (args[2].toLowerCase().startsWith("r")) {
				isRefRegion = true;
			} else if (args[2].toLowerCase().startsWith("q"))  {
				isRefRegion = false;
			}
			region = args[3];
			new Liftover().go(args[0], args[1]);
		} else {
			new Liftover().printHelp();
		}
	}

	@Override
	public void hooker(FileReader frCoords, FileReader frDelta) {
		String line;
		String[] tokens;
		String annotation;
		if (region.endsWith(".bed")) {
			FileReader regionBed = new FileReader(region);
			while (regionBed.hasMoreLines()) {
				line = regionBed.readLine();
				tokens = line.split(RegExp.TAB);
				annotation = "";
				region=tokens[0] + ":" + tokens[1] + "-" + tokens[2];
				if (tokens.length > 3) {
					for (int i = 3; i < tokens.length; i++) {
						annotation += "\t" + tokens[i];
					}
				}
				liftOver(frCoords, frDelta, region, annotation);
				frCoords.reset();
				frDelta.reset();
			}
		} else {
			liftOver(frCoords, frDelta, region, "");
		}
		
	}
	
	public void liftOver(FileReader frCoords, FileReader frDelta, String region, String annotation) {

		String line;
		String[] tokens;
		
		String contig = region.substring(0, region.lastIndexOf(":"));
		double start = Double.parseDouble(region.substring(region.lastIndexOf(":") + 1).split("-")[0]);
		double end = Double.parseDouble(region.substring(region.lastIndexOf(":") + 1).split("-")[1]);
		double originalStart = start;
		double originalEnd = end;
		
		System.err.println("Lift over " + contig + " " + String.format("%.0f", originalStart) + " - " + String.format("%.0f", originalEnd));
		
		double rStart;	// Target coordinate
		double rEnd;
		double qStart;	// Query coordinate
		double qEnd;
		double coordsStart;
		double coordsEnd;
		
		HashSet<String> coords = new HashSet<String>();
		
		System.err.println("Start reading " + frCoords.getFileName());
		while (frCoords.hasMoreLines()) {
			line = frCoords.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			
			// skip outside of target zone
			if (isRefRegion && !tokens[COORDS.R_NAME].equals(contig)
					&& !isRefRegion && !tokens[COORDS.Q_NAME].equals(contig)) {
				continue;
			}
			
			rStart	= Double.parseDouble(tokens[COORDS.R_START]);
			rEnd	= Double.parseDouble(tokens[COORDS.R_END]);
			qStart	= Double.parseDouble(tokens[COORDS.Q_START]);
			qEnd	= Double.parseDouble(tokens[COORDS.Q_END]);
			
			// we are in target zone
			if (isRefRegion) {
				coordsStart = rStart;
				coordsEnd = rEnd;
			} else {
				if (qEnd < qStart) {
					coordsStart = qEnd;
					coordsEnd = qStart;
				} else {
					coordsStart = qStart;
					coordsEnd = qEnd;
				}
			}
			if (coordsStart <= originalEnd && coordsEnd >= originalStart) {
				// Add to the coords list
				coords.add(tokens[COORDS.R_START] + " " + tokens[COORDS.R_END] + " " + tokens[COORDS.Q_START] + " " + tokens[COORDS.Q_END]);
			}
		}
		System.err.println("Matching coordinates in " + frCoords.getFileName() + " : " + coords.size());
		
		
		
		if (coords.size() == 0) {
			System.err.println("No matching coordinates.");
			System.out.println(contig + "\t" + String.format("%.0f", originalStart) + "\t" + String.format("%.0f", originalEnd) + annotation);
			return;
		}
		
		frDelta.readLine();	// ref qry fa path
		line = frDelta.readLine();	// NUCMER/PROMER
		if (!line.equals("NUCMER")) {
			System.err.println("[ERROR]" + frDelta.getFileName() + " is not a valid NUCMER .delta file. Exit.");
			System.exit(-1);
		}
		
		System.err.println("Start reading " + frDelta.getFileName());
		int match;
		short IDtype = Delta.TYPE_UNSET;
		String liftedContig = "";
		double liftStart = -1;
		double liftEnd = -1;
		boolean foundLiftStart = false;
		boolean isQReverse = false;
		boolean isInChunk = false;
		double tmp;
		String coordinates = "";
		double initialRStart = -1;
		double initialQStart = -1;
		
		boolean isTruncated = false;
		int distance = 0;
		
		ALIGNMENT_CHUNK_LOOP : while (frDelta.hasMoreLines()) {
			line = frDelta.readLine();
			if (line.startsWith(">")) {
				tokens = line.split(RegExp.WHITESPACE);
				if ((isRefRegion && tokens[0].equals(">" + contig)) || (!isRefRegion && tokens[1].equals(contig))) {
					if (isRefRegion) {
						liftedContig = tokens[Delta.Q_NAME];
					} else {
						liftedContig = tokens[Delta.R_NAME].substring(1);
					}
					isInChunk = true;
					continue ALIGNMENT_CHUNK_LOOP;
				} else {
					isInChunk = false;
					continue ALIGNMENT_CHUNK_LOOP;
				}
			}

			if (isInChunk) {
				tokens = line.split(RegExp.WHITESPACE);
				if (tokens.length > 1) {
					// alignment chunk header
					rStart	= Double.parseDouble(tokens[Delta.R_START]);
					rEnd	= Double.parseDouble(tokens[Delta.R_END]);
					qStart	= Double.parseDouble(tokens[Delta.Q_START]);
					qEnd	= Double.parseDouble(tokens[Delta.Q_END]);
					isQReverse = false;
					if (coords.contains(tokens[Delta.R_START] + " " + tokens[Delta.R_END] + " " + tokens[Delta.Q_START] + " " + tokens[Delta.Q_END])) {
						//System.err.println(line);
						// we are in target zone

						if (qEnd < qStart) {
							isQReverse = true;
							tmp = qStart;
							qStart = qEnd;
							qEnd = tmp;
						}
						initialRStart = rStart;
						initialQStart = qStart;
						start = originalStart;
						end = originalEnd;
						distance = 0;
								
						System.out.print(contig + "\t" + String.format("%.0f", originalStart) + "\t" + String.format("%.0f", originalEnd) + annotation);
						
						ALIGNMENT_LOOP : while (frDelta.hasMoreLines()) {
							// Track the rCoords and qCoords, find that matches the region start-end
							//System.err.println("rStart: " + rStart + "\tqStart: " + qStart + "\tstart: " + start);
							if (!foundLiftStart) {
								if (isRefRegion && rStart >= originalStart) {
									if (initialQStart > qStart - (rStart - originalStart)) {
										liftStart = initialQStart;
										start = initialRStart;
										isTruncated = true;
									} else {
										liftStart = qStart - (rStart - start);
									}
									coordinates = "\t" + liftedContig + "\t" + String.format("%.0f", liftStart);
									foundLiftStart = true;
								} else if (!isRefRegion && qStart >= originalStart) {
									if (initialRStart > rStart - (qStart - originalStart)) {
										liftStart = initialRStart;
										start = initialQStart;
										isTruncated = true;
									} else {
										liftStart = rStart - (qStart - start);
									}
									coordinates = "\t" + liftedContig + "\t" + String.format("%.0f", liftStart);
									foundLiftStart = true;
								}
							}

							if (foundLiftStart) {
								if (isRefRegion && rStart >= originalEnd) {
									liftEnd = qStart - (rStart - originalEnd);
									if (!isTruncated) {
										System.out.println(coordinates + "\t" + String.format("%.0f", liftEnd) + "\t" + (isQReverse ? "-" : "+") + "\t" + --distance + "\tcomplete");
									} else {
										System.out.println(coordinates + "\t" + String.format("%.0f", liftEnd) + "\t" + (isQReverse ? "-" : "+") + "\t" + --distance
												+ "\ttruncated\t" + contig + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end));
									}
									foundLiftStart = false;
									continue ALIGNMENT_CHUNK_LOOP;
								} else if (!isRefRegion && qStart >= originalEnd) {
									liftEnd = rStart - (qStart - originalEnd);
									if (!isTruncated) {
										System.out.println(coordinates + "\t" + String.format("%.0f", liftEnd) + "\t" + (isQReverse ? "-" : "+") + "\t" + --distance + "\tcomplete");
									} else {
										System.out.println(coordinates + "\t" + String.format("%.0f", liftEnd) + "\t" + (isQReverse ? "-" : "+") + "\t" + --distance
												+ "\ttruncated\t" + contig + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end));
									}
									foundLiftStart = false;
									continue ALIGNMENT_CHUNK_LOOP;
								}
							}

							line = frDelta.readLine();
							//System.err.println("[DEBUG] :: " + line);
							if (line.equals("0")) {
								break ALIGNMENT_LOOP;
							}
							match = Integer.parseInt(line);
							IDtype = Delta.getIDType(match);
							if (match < 0) {	// I
								match *= -1;
							}
							match--;

							rStart += match;
							qStart += match;

							if (IDtype == Delta.TYPE_D) {
								rStart++;
							} else if (IDtype == Delta.TYPE_I) {
								qStart++;
							}
							distance++;

						}

						if (!foundLiftStart) {
							if (isRefRegion) {
								if (originalStart > initialRStart) {
									liftStart = qStart - (originalStart - initialRStart);
								} else {
									liftStart = qStart;
									start = rStart;
									isTruncated = true;
								}
								//System.out.print(contig + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end));
								coordinates = "\t" + liftedContig + "\t" + String.format("%.0f", liftStart);
								foundLiftStart = true;
							} else if (!isRefRegion) {
								if (originalStart > initialQStart) {
									liftStart = rStart + (originalStart - qStart);
								} else {
									liftStart = rStart;
									start = qStart;
									isTruncated = true;
								}
								//System.out.print(contig + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end));
								coordinates = "\t" + liftedContig + "\t" + String.format("%.0f", liftStart);
								foundLiftStart = true;
							}
						}

						rStart = rEnd;
						qStart = qEnd;

						if (foundLiftStart) {
							if (isRefRegion && rStart >= originalEnd) {
								liftEnd = qStart + (rStart - originalEnd);
								if (!isTruncated) {
									System.out.println(coordinates + "\t" + String.format("%.0f", liftEnd) + "\t" + (isQReverse ? "-" : "+") + "\t" + distance + "\tcomplete");
								} else {
									System.out.println(coordinates + "\t" + String.format("%.0f", liftEnd) + "\t" + (isQReverse ? "-" : "+") + "\t" + distance
											+ "\ttruncated\t" + contig + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end));
								}
							} else if (!isRefRegion && qStart >= originalEnd) {
								liftEnd = rStart - (qStart - originalEnd);
								if (!isTruncated) {
									System.out.println(coordinates + "\t" + String.format("%.0f", liftEnd) + "\t" + (isQReverse ? "-" : "+") + "\t" + distance + "\tcomplete");
								} else {
									System.out.println(coordinates + "\t" + String.format("%.0f", liftEnd) + "\t" + (isQReverse ? "-" : "+") + "\t" + distance
											+ "\ttruncated\t" + contig + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end));
								}
							} else {
								// has a split alignment?
								if (isRefRegion) {
									liftEnd = qStart;
									end = rStart; 
								} else {
									liftEnd = rStart;
									end = qStart;
								}
								System.out.println(coordinates + "\t" + String.format("%.0f", liftEnd) + "\t" + (isQReverse ? "-" : "+") + "\t" + distance
										+ "\ttruncated\t" + contig + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end));
							}
							foundLiftStart = false;
						}
					}
				}
				isTruncated = false;
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar nucmerLiftover.jar <in.coords> <in.delta> <r or q> <region>");
		System.out.println("\t<in.coords>: nucmer .1coords file");
		System.out.println("\t<in.delta>: nucmer .1delta file");
		System.out.println("\t<r or q>: r if given <region> is reference coordinate, else q if query coordinate");
		System.out.println("\t<region>: contig:start-end style region to lift over. Could be a bed file (ending with .bed extension).");
		System.out.println("\t<stdout>: query\tlifted_coords\tstrand\tedit_distance\tcomplete/truncated\texact-matching query coords if truncated paired-bed style.");
		System.out.println("Arang Rhie, 2019-01-07. arrhie@gmail.com");
	}

}
