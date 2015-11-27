package javax.arang.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class Alignment {
	
	public int[][] score;
	public int[][] marking;
	public ArrayList<Integer> depthCov = new ArrayList<Integer>();
	
	public static final int MATCH_SCORE = 5;
	public static final int MISMATCH_SCORE = -3;
	public static final int GAP_SCORE = -2;
	public static final int GAP_PANELTY = -2;
	
	public static final String SPAN = "span";
	public static final String LEFT_FLANK = "leftFlank";
	public static final String RIGHT_FLANK = "rightFlank";
	public static final String BRIDGE_FLANK = "bridgeFlank";
	public static final String OPEN = "open";
	
	public static final int CIGAR = 0;
	public static final int SEQ = 1;
	public static final int TYPE = 2;
	

	public void initAlignment() {
		this.depthCov = new ArrayList<Integer>();
		score = null;
		marking = null;
	}
	
	public ArrayList<Integer> reverseDepthCov() {
		ArrayList<Integer> newDepthCov =  new ArrayList<Integer>();
		for (int i = depthCov.size() - 1; i >= 0 ; i--) {
			newDepthCov.add(depthCov.get(i));
		}
		return newDepthCov;
	}
	
	/***
	 * Initializing score and marking table.
	 * @param readToAlign
	 * @param readToCompare
	 * @return Cell containing the maximum score
	 */
	public Cell initializeScoreTable(String readToAlign, String readToCompare) {
		
		int toAlignLen = readToAlign.length();
		int toCompareLen = readToCompare.length();
		score = new int[toAlignLen + 1][toCompareLen + 1];
		marking = new int[toAlignLen + 1][toCompareLen + 1];
		int gapPanelty = 0;
		Cell cell = null;
		//if (!reverse) {
			for (int i = 0; i < toAlignLen + 1; i++) {
				score[i][0] = gapPanelty;
				marking[i][0] = 0;
				gapPanelty += GAP_PANELTY;
			}
			for (int j = 0; j < toCompareLen + 1; j++) {
				score[0][j] = gapPanelty;
				marking[0][j] = 0;
				gapPanelty += GAP_PANELTY;
			}

			for (int j = 1; j < readToCompare.length() + 1; j++) {
				for (int i = 1; i < readToAlign.length() + 1; i++) {
					score[i][j] = getScore(readToAlign.charAt(i - 1), readToCompare.charAt(j - 1), i, j);
					marking[i][j] = 0;
				}
			}
		
			cell = new Cell(new ArrayList<Integer>(), score[readToAlign.length()][readToCompare.length()], readToAlign.length(), readToCompare.length());
//		} else {
//			for (int i = toAlignLen; i >=0; i--) {
//				score[i][0] = gapPanelty;
//				marking[i][0] = 0;
//				gapPanelty += GAP_PANELTY;
//			}
//			for (int j = toCompareLen; j >= 0; j--) {
//				score[0][j] = gapPanelty;
//				marking[0][j] = 0;
//				gapPanelty += GAP_PANELTY;
//			}
//
//			for (int j = readToCompare.length(); j >= 1; j--) {
//				for (int i = readToAlign.length(); i >= 1; i--) {
//					score[i][j] = getScore(readToAlign.charAt(i + 1), readToCompare.charAt(j + 1), i, j);
//					marking[i][j] = 0;
//				}
//			}
//		
//			cell = new Cell(new Vector<Integer>(), score[0][0], readToAlign.length(), readToCompare.length());
//		}
		return cell;
	}
	
	private int getScore(char toAlign, char toCompare, int i, int j) {
		int scoreMax = score[i-1][j-1]; 
		if (toAlign == toCompare)	scoreMax += MATCH_SCORE;
		else scoreMax += MISMATCH_SCORE;
		int indelMax = Math.max(score[i-1][j] + GAP_SCORE, score[i][j-1] + GAP_SCORE);
		scoreMax = Math.max(scoreMax, indelMax);
		return scoreMax;
	}
	
	public String globalAlign(String readToAlign, String readToCompare) {
		
		// 1. Generate the score matrix
		Cell lastCell = initializeScoreTable(readToAlign, readToCompare);
		
		// 2. Trace back from the highest score
		//   \ : 1
		//   - : 2 insertion from readToAlign
		//   | : 3 insertion from readToCompare
		//Cell lastCell = Alignment.backTrack(score[readToAlign.length()][readToCompare.length()], readToAlign.length(), readToCompare.length());
		lastCell = backTrack(lastCell);
		// System.out.println("[DEBUG] :: endPosScore: " + lastCell.score + " starting from " + readToAlign.length() + "," + readToCompare.length());
		// System.out.println("[DEBUG] :: backTrack path | " + lastCell.printPath());
		
		StringBuffer refCandidate = new StringBuffer();

		int toAlignIdx = readToAlign.length() - 1;
		int toCompIdx = readToCompare.length() - 1;
		int depthIdx = 0;
		for (int i = 0; i < lastCell.path.size() - 1 && toAlignIdx >= 0 && toCompIdx >= 0; i++) {
			if (lastCell.path.get(i) == 1) {	// \
				refCandidate.append(readToAlign.charAt(toAlignIdx));
				if (refCandidate.length() > depthCov.size()) {
					depthCov.add(2);
				} else {
					depthCov.set(depthIdx, depthCov.get(depthIdx) + 1);
				}
				toAlignIdx--;
				toCompIdx--;
			}
			else if (lastCell.path.get(i) == 2) {	// -
				refCandidate.append(readToAlign.charAt(toAlignIdx));
				if (refCandidate.length() > depthCov.size()) {
					depthCov.add(1);
				}
				toAlignIdx--;
			}
			else if (lastCell.path.get(i) == 3) {	// |
				refCandidate.append(readToCompare.charAt(toCompIdx));
				if (refCandidate.length() > depthCov.size()) {
					depthCov.add(1);
				}
				toCompIdx--;
			}
			depthIdx++;
		}
		
		for (int i = 0; i <= toAlignIdx; i++) {
			refCandidate.append(readToAlign.charAt(toAlignIdx - i));
			if (refCandidate.length() > depthCov.size()) {
				depthCov.add(1);
			}
		}
		
		for (int i = 0; i <= toCompIdx; i++) {
			refCandidate.append(readToCompare.charAt(toCompIdx));
			if (refCandidate.length() > depthCov.size()) {
				depthCov.add(1);
			}
		}
		
		refCandidate = refCandidate.reverse();
		return refCandidate.toString();
	}
	
	static Vector<Cell> cellQueue = new Vector<Cell>();
	
	public Cell backTrack(Cell cell) {
		return backTrack(cell.score, cell.toAlignIdx, cell.toCompareIdx);
	}
	
	public Cell backTrack(int cumScore, int i, int j) {
		Cell cell = new Cell();
		cell.path = new ArrayList<Integer>();
		cell.score = cumScore;
		cell.toAlignIdx = i;
		cell.toCompareIdx = j;
		if(i == 1 || j == 1) {
			return cell;
		}
		
		Cell maxCell = cell;
		int numCells = 0;
		
		cellQueue.add(cell);
		marking[i][j] = 1;
		numCells = cellQueue.size();

		while (numCells > 0) {
			while (numCells == 1) {
				cell = cellQueue.get(0);
				cellQueue.remove(0);
				if(cell.toAlignIdx == 1 || cell.toCompareIdx == 1) {
					return cell;
				}
				maxCell = null;
				addCellsToCellQueue(cell, maxCell);
				numCells = cellQueue.size();
				//System.out.println("[DEBUG] :: numCells: " + numCells);
			}
			if (cellQueue.size() == 0)	break;
			maxCell = null;

			// numCells > 1
			HashMap<String, Integer> tmpCellPositions = new HashMap<String, Integer>();
			int localMax = 0;
			for (int queueIdx = 0; queueIdx < numCells; queueIdx++) {
				Cell tmpCell = cellQueue.get(0);
				if (localMax < tmpCell.score) {
					localMax = tmpCell.score;
				}
				String tmpCellPos = tmpCell.toAlignIdx + "," + tmpCell.toCompareIdx;
				if (!tmpCellPositions.containsKey(tmpCellPos)) {
					tmpCellPositions.put(tmpCellPos, tmpCell.path.size());
				} else {	// tmpCellPositions.containsKey(tmpCellPos)
					if (tmpCellPositions.get(tmpCellPos) > tmpCell.path.size()) {
						tmpCellPositions.put(tmpCellPos, tmpCell.path.size());
					}
				}
			}
			for (int queueIdx = 0; queueIdx < numCells; queueIdx++) {
				cell = cellQueue.get(0);
				if (cell.score < localMax) {	
					// pruning
					cellQueue.remove(0);
					continue;
				}
				String tmpCellPos = cell.toAlignIdx + "," + cell.toCompareIdx;
				if (tmpCellPositions.containsKey(tmpCellPos) && tmpCellPositions.get(tmpCellPos) < cell.path.size()) {
					// pruning
					cellQueue.remove(0);
					continue;
				}
				if(cell.toAlignIdx == 1 || cell.toCompareIdx == 1) {
					return cell;
				}
				//System.out.println("[DEBUG] :: addCellsToCellQ : " + cell.printPath());
				cellQueue.remove(0);
				maxCell = addCellsToCellQueue(cell, maxCell);
				numCells = cellQueue.size();
			}
		}
		return cell;
	}
	
	private Cell addCellsToCellQueue(Cell cell, Cell maxCell) {
		Cell diagCell = cell.clone();
		Cell leftCell = cell.clone();
		Cell upperCell = cell.clone();
		int i = cell.toAlignIdx;
		int j = cell.toCompareIdx;
		int max = getMax(i, j);
		if (score[i-1][j-1] == max) {
			diagCell.path.add(1);
			diagCell.score = max;
			diagCell.toAlignIdx--;
			diagCell.toCompareIdx--;
			if (maxCell != null && maxCell.score <= diagCell.score || maxCell == null) {
				if (marking[diagCell.toAlignIdx][diagCell.toCompareIdx] == 0) {
					maxCell = diagCell;
					cellQueue.add(diagCell);
					marking[diagCell.toAlignIdx][diagCell.toCompareIdx] = 1;
				}
			}
		} 
		// |
		if (score[i][j - 1] == max) {
			upperCell.path.add(3);
			upperCell.score = max;
			upperCell.toCompareIdx--;
			if (maxCell != null && maxCell.score < upperCell.score || maxCell == null) {
				if (marking[upperCell.toAlignIdx][upperCell.toCompareIdx] == 0) {
					maxCell = upperCell;
					cellQueue.add(upperCell);
					marking[upperCell.toAlignIdx][upperCell.toCompareIdx] = 1;
				}
			}
		}
		// -
		if (score[i - 1][j] == max) {
			leftCell.path.add(2);
			leftCell.score = max;
			leftCell.toAlignIdx--;
			if (maxCell != null && maxCell.score < leftCell.score || maxCell == null) {
				if (marking[leftCell.toAlignIdx][leftCell.toCompareIdx] == 0) {
					maxCell = leftCell;
					cellQueue.add(leftCell);
					marking[leftCell.toAlignIdx][leftCell.toCompareIdx] = 1;
				}
			}
		}
		return maxCell;
	}
	
	private int getMax(int i, int j) {
		int max;
		if (score[i - 1][j - 1] >= Math.max(score[i - 1][j], score[i][j - 1])) {
			max = score[i - 1][j - 1];
		} else if (score[i-1][j] >= score[i][j-1]) {
			max = score[i-1][j];
		} else {
			max = score[i][j-1];
		}
		//System.out.println("[DEBUG :: getMax : score[" + (i - 1) + "][" + (j - 1) + "]=" + score[i - 1][j - 1] + " vs. score[" + (i - 1) + "][" + j + "]=" + score[i - 1][j] + " vs. score[" + i + "][" + (j - 1) + "]="  + score[i][j - 1] + " : " + max);
		return max;
	}
	
	
	
}
