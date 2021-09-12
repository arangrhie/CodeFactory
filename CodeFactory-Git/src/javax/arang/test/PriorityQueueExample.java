package javax.arang.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PriorityQueueExample {

	public static void main(String[] args) {
	      PriorityQueue < Integer >  prq = new PriorityQueue < Integer > (); 
	       
	      // insert values in the queue
	      for ( Integer i = 3; i  <  10; i++ ){  
	         prq.add (i) ; 
	      }
	      System.out.println ( "Initial priority queue values are: "+ prq);
	      
	      // get the head from the queue
	      Integer head = prq.poll();
	      System.out.println ( "Head of the queue is: "+ head);
	      System.out.println ( "Priority queue values after poll: "+ prq);

	      System.out.println();
	      System.out.println("Longest one first");
	      Comparator<String> comparator = new StringLengthComparator();
	      PriorityQueue<String> queue = new PriorityQueue<String>(10, comparator);
	      queue.add("short");
	      queue.add("very long indeed");
	      queue.add("medium");
	      while (queue.size() != 0)
	      {
	    	  System.out.println(queue.remove());
	      }
	      
	      System.out.println();
	      System.out.println("Shortest one first");
	      System.out.println("Queue2");
	      PriorityQueue<String> queue2 = new PriorityQueue<String>(10);
	      queue2.add("short");
	      queue2.add("very long indeed");
	      queue2.add("medium");
	      
	      System.out.println();
	      System.out.println("Checking for PriorityQeue order after toArray()");
	      ArrayList<String> q2Arr = new ArrayList<String>();
	      while (!queue2.isEmpty()) {
	    	  q2Arr.add(queue2.remove());
	      }
	      for (String a : q2Arr) {
	    	  System.out.println(a);
	      }
	      
	}

}

/***
 * Returns the longest first
 * @author Arang
 *
 */
class StringLengthComparator implements Comparator<String>
{
    @Override
    public int compare(String x, String y)
    {
        // Assume neither string is null. Real code should
        // probably be more robust
        // You could also just return x.length() - y.length(),
        // which would be more efficient.
        if (x.length() < y.length())
        {
            return 1;	// y will be sitting prior to x.
        }
        if (x.length() > y.length())
        {
            return -1;
        }
        return 0;
    }
}
