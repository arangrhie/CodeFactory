package javax.arang.chain;

public class Chain {
	/***
	 * chain
	 *  score
	 *  tName 
	 *  tSize 
	 *  tStrand 
	 *  tStart 
	 *  tEnd 
	 *  qName 
	 *  qSize 
	 *  qStrand 
	 *  qStart 
	 *  qEnd 
	 *  id
	 */
	public static final int CHAIN = 0;
	public static final int SCORE=1;
	public static final int T_NAME=2;
	public static final int T_SIZE=3;
	public static final int T_STRAND=4;
	public static final int T_START=5;
	public static final int T_END=6;
	public static final int Q_NAME=7;
	public static final int Q_SIZE=8;
	public static final int Q_STRAND=9;
	public static final int Q_START=10;
	public static final int Q_END=11;
	public static final int Q_ID=12;
	
	/***
	 * size dt dq
	 */
	public static final short BLOCK_SIZE = 0;
	public static final short BLOCK_DT = 1;
	public static final short BLOCK_DQ = 2;
}
