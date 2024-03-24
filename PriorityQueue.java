/*  Student information for assignment:
 *
 *  On my honor, Sooyeon Yang, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 2
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: sy22975
 *  email address: soois1114@gmail.com
 *  Grader name: Aditya
 */

import java.util.ArrayList;

public class PriorityQueue<E extends Comparable<? super E>> { 
    
    private ArrayList<E> list;
    
    /**
     * default constructor
     * pre: none
     * post: none
     */
    public PriorityQueue() {
        list = new ArrayList<E>();
    }
    
    /**
     * Adds element e to this queue, prioritizing smaller elements first (adding towards front).
     * If elements are equal, adds passed in element after all elements that are equal to it.
     * pre: e != null
     * post: none
     * @param e
     * @return
     */
    public boolean add(E e) {
        
        //checks preconditions
        if(e == null) {
            throw new IllegalArgumentException("Violation of precondition: add. Parameter"
                    + "cannot be null.");
        }
        
        //if queue is empty, just add; if last element in queue equals e, add to end
        if(list.size() == 0 || list.get(size() - 1).compareTo(e) <= 0) { 
            list.add(e);
            return true;
        }
        
        //otherwise, there must be an elem greater than e, so find position to insert to list
        int pos = 0;
        boolean found = false;
        while(!found && pos < size()) {
            if(e.compareTo(list.get(pos)) < 0) {
                found = true;
            }
            else {
                pos++;
            }
        }
        if(found) {
            list.add(pos, e);
            return true;
        }
        
        //couldn't find a position to add in e, failed to add to queue
        return false;
    }
    
    /**
     * Removes the first element in the queue (the one with the highest priority) and returns it.
     * If queue is empty, return null.
     * pre: none
     * post: first element of queue is removed
     * @return E, an element that is at the front of the queue
     */
    public E poll() {
        if(list.size() == 0) {
            return null;
        }
        
        return list.remove(0);
    }
    
    /**
     * gets the size of this PriorityQueue
     * pre: none
     * post:none
     * @return the size
     */
    public int size() {
        return list.size();
    }
    
    /**
     * returns the first element of the queue. If queue is empty, return null.
     * pre: none
     * post: first element of queue is NOT removed
     * @return
     */
    public E peek() {
        if(list.size() == 0) {
            return null;
        }
        
        return list.get(0);
    }
}
