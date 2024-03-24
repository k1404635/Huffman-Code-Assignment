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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private HuffmanCodeTree tree;
    private int header;
    private int[] freqs;
    private boolean preProcessed;
    private int origBits;
    private int compressedBits;

    /**
     * default constructor
     * pre: none
     * post:none
     */
    public SimpleHuffProcessor() {
        freqs = new int[ALPH_SIZE];
        preProcessed = false;
    }
    
    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it in one as needed.
     * pre: none
     * post: none
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        
        boolean viewer = myViewer != null;
        if(viewer) {
            myViewer.showMessage("Starting preprocessCompress method");
        }
        BitInputStream bitIn = new BitInputStream(new BufferedInputStream(in));
        preProcessed = true;
        freqs = new int[ALPH_SIZE];
        header = headerFormat;
        origBits = 0;
        compressedBits = 0;
        
        int bits = bitIn.readBits(BITS_PER_WORD);
        checkReadBits(bits); //checks if bits is a valid value
        
        //if compressing a compressed file, stop at PEOF value, uncompressed file shouldn't have
        //a PEOF if reading an uncompressed file
        while(bits != -1 && bits != PSEUDO_EOF) {
            freqs[bits]++;
            origBits += BITS_PER_WORD; 
            bits = bitIn.readBits(BITS_PER_WORD);
        }
        
        //count characters & make queue
        PriorityQueue<TreeNode> queue = makeQueue();
        
        tree = new HuffmanCodeTree(queue); //make tree (map of paths made in HuffmanCodeTree class)
        calcBitsCompressed();
        bitIn.close();
        
        if(viewer) {
            myViewer.showMessage("Finished preprocessCompress method");
        }
        return origBits - compressedBits;
    }
    
    /**
     * checks if the parameter passed in is a valid value for a return value of readBits
     * @param bits, the number that was returned from a readBits method call
     * @throws IOException
     */
    private void checkReadBits(int bits) throws IOException {
        if(bits == -1) {
            throw new IOException("Something is wrong with the format of the input file.");
        }
    }
    
    /**
     * calculates the number of bits compressed based off of the original file's info.
     * Sets instance variable compressedBits to that calculated result.
     */
    private void calcBitsCompressed() {
        
        //add one BITS_PER_INT for the magic number, and another for the header format int
        compressedBits = BITS_PER_INT + BITS_PER_INT;
        if(header == STORE_COUNTS) {
            compressedBits += BITS_PER_INT*ALPH_SIZE;
        }
        else if(header == STORE_TREE) {
            
            //add another BITS_PER_INT for the size of tree representation int
            //add one to BITS_PER_WORD due to addition of PEOF value
            compressedBits += BITS_PER_INT + (tree.getNumLeaf()*(BITS_PER_WORD + 1) + tree.size());
        }
        String[] map = tree.getPaths();
        for(int x = 0; x < ALPH_SIZE; x++) {
            
            //frequency of each value times path length gives num bits for compressed data
            if(freqs[x] != 0) {
                compressedBits += freqs[x]*map[x].length();
            }
        }
        compressedBits += map[PSEUDO_EOF].length(); //add bit count for PEOF value
    }

    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * post: none
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        
        //checks precondition
        if(!preProcessed) {
            throw new IOException("File not pre-processed before compressing.");
        }
        boolean viewer = myViewer != null;
        if(viewer) {
            myViewer.showMessage("Starting to compress");
        }
        
        if(viewer && !force && origBits < compressedBits) {
            myViewer.showError("Output file is larger than input file, and cannot force.");
            return -1;
        }
        
        BitOutputStream bitOut = new BitOutputStream(new BufferedOutputStream(out));
        BitInputStream bitIn = new BitInputStream(new BufferedInputStream(in));
        
        bitOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        writeHeader(bitOut); //writes header based on header format type
        
        int bit = bitIn.readBits(BITS_PER_WORD);
        checkReadBits(bit); //checks if bit is a valid value
        
        while(bit != -1 && bit != PSEUDO_EOF) { //if hits PEOF value, stop
            for(int x = 0; x < tree.getPaths()[bit].length(); x++) {
                
                //char at x in the path would be either 0 or 1, so subtracting the value of '0'
                //will give the integer value of 0 or 1
                bitOut.writeBits(1, tree.getPaths()[bit].charAt(x) - '0');
            }
            bit = bitIn.readBits(BITS_PER_WORD);
        }
        tree.writePEOF(bitOut); //write the PEOF value based on tree's path for that value
        closeStreams(bitIn, bitOut);
        if(viewer) {
            myViewer.showMessage("Finished compressing");
        }
        return compressedBits;
    }
    
    /**
     * writes header in correct header format
     * @param bitOut, a BitOutputStream
     */
    private void writeHeader(BitOutputStream bitOut) {
        if(header == STORE_COUNTS) {
            bitOut.writeBits(BITS_PER_INT, STORE_COUNTS); //write header format number
            for(int x = 0; x < ALPH_SIZE; x++) {
                bitOut.writeBits(BITS_PER_INT, freqs[x]);
            }
        }
        else if(header == STORE_TREE) {
            bitOut.writeBits(BITS_PER_INT, STORE_TREE); //write header format number
            
            //size of tree representation calculation from second howTo guide
            int sizeTreeRep = (tree.getNumLeaf()*(BITS_PER_WORD + 1)) + tree.size();
            bitOut.writeBits(BITS_PER_INT, sizeTreeRep);
            tree.writeTree(bitOut); //goes to tree to write the tree header format data
        }
    }
    
    /**
     * closes both input and output streams
     * @param bitIn, a BitInputStream
     * @param bitOut, a BitOutputStream
     */
    private void closeStreams(BitInputStream bitIn, BitOutputStream bitOut) {
        bitOut.close();
        bitIn.close();
    }
    
    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * pre: none
     * post: none
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        boolean viewer = myViewer != null;
        if(viewer) {
            myViewer.showMessage("Starting to uncompress");
        }
        BitInputStream bitIn = new BitInputStream(new BufferedInputStream(in)); 
        BitOutputStream bitOut = new BitOutputStream(new BufferedOutputStream(out));
        
        //checks if file is uncompressable (is a hf file - starts with huff magic number)
        if(myViewer != null && bitIn.readBits(BITS_PER_INT) != MAGIC_NUMBER) {
            myViewer.showError("Error reading compressed file: did not start with huff "
                    + "magic number.");
            return -1;
        }
        
        int headerFormat = bitIn.readBits(BITS_PER_INT);
        checkReadBits(headerFormat); //checks if headerFormat is a valid value
        
        if(headerFormat == STORE_COUNTS) {
            for(int x = 0; x < ALPH_SIZE; x++) { //makes frequency array
                freqs[x] = bitIn.readBits(BITS_PER_INT);
            }
            tree = new HuffmanCodeTree(makeQueue()); //makes queue and then tree with the queue
        }
        else if(headerFormat == STORE_TREE) {
            tree = new HuffmanCodeTree(bitIn);
        }
        
        return writeUncompressData(bitIn, bitOut, viewer);
    }
    
    /**
     * helper method to uncompress. Writes the actual data part of the file using the paths of the
     * tree and the data read in with bitIn.
     * @param bitIn, a BitInputStream
     * @param bitOut, a BitOutputStream
     * @return the number of bits written to uncompressed file/stream
     * @throws IOException
     */
    private int writeUncompressData(BitInputStream bitIn, BitOutputStream bitOut, boolean viewer) 
            throws IOException {
        
        int count = 0;
        int bit = bitIn.readBits(1);
        checkReadBits(bit); //checks if bit is a valid value
        
        TreeNode temp = tree.getRoot(); //start at root
        while(bit != -1 && temp.getValue() != PSEUDO_EOF) { //if hits PEOF, stop
            
            //internal node - keep going through tree
            if(temp.getRight() != null || temp.getLeft() != null) {
                if(bit == 0) { //"0" indicates go left
                    temp = temp.getLeft();
                }
                else if(bit == 1) { //"1" indicates go right
                    temp = temp.getRight();
                }
            }
            
            //leaf node, write value and restart at root
            if(temp.getRight() == null && temp.getLeft() == null && temp.getValue() != PSEUDO_EOF) { 
                bitOut.writeBits(BITS_PER_WORD, temp.getValue());
                count += BITS_PER_WORD;
                temp = tree.getRoot();
            }
            bit = bitIn.readBits(1);
        }
        
        closeStreams(bitIn, bitOut);
        if(viewer) {
            myViewer.showMessage("Finished uncompressing");
        }
        return count;
    }
    
    /**
     * makes the queue from the array of frequencies
     * @return a PriorityQueue with TreeNode elements
     */
    private PriorityQueue<TreeNode> makeQueue(){
        PriorityQueue<TreeNode> result = new PriorityQueue<TreeNode>();
        for(int x = 0; x < freqs.length; x++) {
            if(freqs[x] != 0) { //add to queue only if exists in file
                TreeNode n = new TreeNode((char) x, freqs[x]);
                result.add(n);
            }
        }
        result.add(new TreeNode(PSEUDO_EOF, 1)); //adding in PEOF value
        return result;
    }
    
    /**
     * sets viewer to parameter passed in
     * pre: none
     * post: none
     * @param viewer, an IHuffViewer
     */
    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }
}