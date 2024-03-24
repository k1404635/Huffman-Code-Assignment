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

import java.io.IOException;

public class HuffmanCodeTree { 
    
    private int size;
    private TreeNode root;
    private String[] paths;
    private int numLeaf;
    
    /**
     * contructor passes in a PriorityQueue and using that, makes a HuffmanCodeTree and makes the
     * map of paths for the leaf values.
     * @param queue, a PriorityQueue of TreeNode elements
     */
    public HuffmanCodeTree(PriorityQueue<TreeNode> queue) { 
        paths = new String[IHuffConstants.ALPH_SIZE + 1]; //add one to count for PEOF value
        numLeaf = 0;
        
        //make tree
        size = 0;
        while(queue.size() > 1) {
            
            //nodes that are not leaf nodes have a value of -1 (value doesn't matter for these)
            queue.add(new TreeNode(queue.poll(), -1, queue.poll()));
            
            //three nodes made each time, but the parent node is added back to queue, so size
            //increases by 2
            size += 2; 
        }
        
        //when the loop finishes, there is only one TreeNode in queue, which will be the root of
        //this HuffmanCodeTree. This node isn't counted in the size increments in the loop, so +1
        size += 1; 
        root = queue.peek(); //set root to the only element in queue left, which is at the front
        
        //make map of paths
        makeMap(queue);
    }
    
    /**
     * constructor with BitInputStrem parameter. Reads in header data and recreates tree and paths.
     * @param in, a BitInputStream
     * @throws IOException
     */
    public HuffmanCodeTree(BitInputStream in) throws IOException {
        paths = new String[IHuffConstants.ALPH_SIZE + 1]; //add one to count for PEOF value
        
        //reads in size of tree representation in bits, but value not needed since recursion is used
        int treeSizeRep = in.readBits(IHuffConstants.BITS_PER_INT);
        
        //checks to make sure readBits returned a valid value for the tree size representation
        if(treeSizeRep == -1) {
            throw new IOException("Something is incorrect about tree header format.");
        }
        size = 0;
        root = readTree(in); //makes tree with header data
        makeMapHelp(root, ""); //makes map of paths
    }
    
    /**
     * Helper method that reads in the bits for the header data to recreate the HuffmanCodeTree
     * using recursion.
     * @param in, a BitInputStream
     * @return a TreeNode
     * @throws IOException
     */
    private TreeNode readTree(BitInputStream in) throws IOException {
        int cur = in.readBits(1);
        if(cur == 0) { //internal node
            TreeNode newNode = new TreeNode(-1, 1); //temp frequency is 1 since exists in file
            
            //format lists nodes in pre-order traversal, so next node data is left node of newNode
            newNode.setLeft(readTree(in));
            newNode.setRight(readTree(in));
            size++;
            return newNode;
        }
        if(cur == 1) { //leaf node
            
            //reads in value of leaf node
            int val = in.readBits(IHuffConstants.BITS_PER_WORD + 1); //add 1 due to PEOF
            if(val == -1) { //checks to ensure readBits returns a valid value
                throw new IOException("Something is incorrect about tree header data.");
            }
            TreeNode newNode = new TreeNode(val, 1); //temp frequency is 1 since exists in file
            size++;
            return newNode;
        }
        
        //readBits gave an invalid value (like a -1)
        throw new IOException("Something is incorrect about format of input file");
    }
    
    /**
     * calls helper method to get the paths for each leaf node and store to instance variable paths
     * @param q, a PriorityQueue with TreeNode elements
     */
    private void makeMap(PriorityQueue<TreeNode> q) {
        makeMapHelp(root, "");
    }
    
    /**
     * uses recursion to get the paths for each leaf node.
     * @param n, a TreeNode
     * @param path, a String of the path made so far
     */
    private void makeMapHelp(TreeNode n, String path) {
        
        //only do something if node is in tree (if n is null, node is not in tree)
        if(n != null) {
            if(n.getLeft() == null && n.getRight() == null) { //leaf node
                paths[n.getValue()] = path; //path is complete, set path
                numLeaf++;
            }
            else {
                makeMapHelp(n.getLeft(), path + "0"); //going left in tree adds "0" to path
                makeMapHelp(n.getRight(), path + "1"); //going right in tree adds "1" to path
            }
        }
    }
    
    /**
     * gets the String array of paths
     * pre: none
     * post: none
     * @return a String array containing the paths for each possible value
     */
    public String[] getPaths() {
        return paths;
    }
    
    /**
     * gets the size of this HuffmanCodeTree
     * pre: none
     * post: none
     * @return the number of nodes in this HuffmanCodeTree
     */
    public int size() {
        return size;
    }
    
    /**
     * gets the number of leaves in this HuffmanCodeTree
     * pre: none
     * post: none
     * @return the number of leaves
     */
    public int getNumLeaf() {
        return numLeaf;
    }
    
    /**
     * calls helper method to write the tree header data
     * pre: none
     * out: none
     * @param out, a BitOutputStream
     */
    public void writeTree(BitOutputStream out) {
        writeTreeHelp(out, root);
    }
    
    /**
     * helper method to writeTree that writes the tree header data, going through the tree in a
     * pre-order traversal.
     * @param out, a BitOutputStream
     * @param n, a TreeNode
     */
    private void writeTreeHelp(BitOutputStream out, TreeNode n) {
        if(n != null) { //only do something if n is not null, meaning n is in tree
            if(n.getLeft() == null && n.getRight() == null) { //leaf node
                
                //leaf node, so write a 1 followed by the value of the leaf node
                out.writeBits(1, 1); 
                out.writeBits(IHuffConstants.BITS_PER_WORD + 1, n.getValue()); 
            }
            else { //internal node
                out.writeBits(1, 0); //internal nodes are represented with a 0
            }
            
            //goes in pre-order traversal, so write before moving to next node
            writeTreeHelp(out, n.getLeft());
            writeTreeHelp(out, n.getRight());
        }
    }
    
    /**
     * writes the PEOF value with the path it has in this HuffmanCodeTree
     * pre: none
     * post: none
     * @param out, a BitOutputStream
     */
    public void writePEOF(BitOutputStream out) { 
        for(int x = 0; x < paths[IHuffConstants.PSEUDO_EOF].length(); x++) {
            
            //char at x in path would be either 0 or 1, so subtracting the value of '0'
            //will give the integer value of 0 or 1
            out.writeBits(1, paths[IHuffConstants.PSEUDO_EOF].charAt(x) - '0');
        }
    }
    
    /**
     * gets the root TreeNode of this HuffmanCodeTree
     * pre: none
     * post: none
     * @return a TreeNode
     */
    public TreeNode getRoot() {
        return root;
    }
}
