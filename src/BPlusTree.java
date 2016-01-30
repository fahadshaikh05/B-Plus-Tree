import javax.swing.JOptionPane;

/**
 * The BPlusTree class implements B+-trees. Each BPlusTree stores its elements in the main memory (not on disks) for
 * simplicity reasons.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class BPlusTree {

	/**
	 * The maximum number of pointers that each node of this BPlusTree can have.
	 */
	protected int fanout;

	/**
	 * The root node of this BPlusTree.
	 */
	protected Node root;


	/**
	 * The Node class implements nodes that constitute a B+-tree. Each Node instance has multiple pointers to other
	 * nodes. At each node, the number of keys is smaller than the number of pointers by one.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 * 
	 */
	protected class Node {

		/**
		 * The number of keys that this Node currently maintains.
		 */
		int numberOfKeys;

		/**
		 * The keys that this Node maintains.
		 */
		Object[] keys;

		/**
		 * The pointers that this Node maintains.
		 */
		Object[] pointers;

		/**
		 * Constructs a Node.
		 */
		protected Node(int fanout) {
			numberOfKeys = 0;
			keys = new Comparable[fanout - 1];
			pointers = new Object[fanout];
		}

		/**
		 * Copy-constructs a Node.
		 * 
		 * @param node
		 *            the other node to copy from.
		 */
		protected Node(Node node) {
			this.numberOfKeys = node.numberOfKeys;
			keys = new Object[node.keys.length];
			System.arraycopy(node.keys, 0, keys, 0, node.keys.length);
			pointers = new Object[node.pointers.length];
			for (int i = 0; i < node.pointers.length; i++) {
				Object pointer = node.pointers[i];
				if (pointer instanceof Node)
					pointers[i] = new Node((Node) pointer); // copy construct the node.
				else
					pointers[i] = pointer;
			}
		}

		/**
		 * Clears this Node.
		 */
		protected void clear() {
			numberOfKeys = 0;
			for (int i = 0; i < keys.length; i++)
				keys[i] = null;
			for (int i = 0; i < pointers.length; i++)
				pointers[i] = null;
		}

		/**
		 * Determines whether or not this Node is a leaf node. It is assumed that all the pointers of every non-leaf
		 * node reference Nodes whereas the pointers of leaf nodes can reference something else.
		 * 
		 * @return true if this Node is a leaf node; false otherwise.
		 */
		protected boolean isLeafNode() {
			return !(pointers[0] instanceof Node);
		}

		/**
		 * Determines whether or not this Node has room for a new entry.
		 * 
		 * @return true if this Node has room for a new node; false otherwise.
		 */
		protected boolean hasRoom() {
			return numberOfKeys < fanout - 1;
		}

		/**
		 * Returns the first index i such that keys[i] >= the given key.
		 * 
		 * @param key
		 *            the given key.
		 * @return the first index i such that keys[i] >= the given key; -1 if there is no such i.
		 */
		protected int findIndexGE(Object key) {
			for (int i = 0; i < numberOfKeys; i++) {
				if (compare(keys[i], key) >= 0)
					return i;
			}
			return -1;
		}

		/**
		 * Returns the largest index i such that keys[i] < the given key.
		 * 
		 * @param key
		 *            the given key.
		 * @return the largest index i such that keys[i] < the given key; -1 if there is no such i.
		 */
		protected int findIndexL(Object key) {
			for (int i = numberOfKeys - 1; i >= 0; i--) {
				if (compare(keys[i], key) < 0)
					return i;
			}
			return -1;
		}


		/**
		 * @param key
		 *            the given key.
		 * @return the first index i such that keys[i] <= the given key; -1 if there is no such i.
		 */
		protected int findIndexLE(Object key) {
			for (int i = 0; i < numberOfKeys; i++) {
				if (compare(keys[i], key) <= 0)
					return i;
			}
			return -1;
		}

		/**
		 * @return the number of pointers in the Node.
		 */
		protected int countPointers() {
			int c =0;
			for (int i = 0; i < fanout; i++) {
				if (this.pointers[i]!= null)
					c++;
			}
			return c;
		}

		/**
		 * Returns the last non-null pointer (assuming that this Node is a non-leaf node).
		 * 
		 * @return the last non-null pointer.
		 */
		protected Object getLastNonNullPointer() {
			return pointers[numberOfKeys];
		}

		/**
		 * Inserts the specified key and value at the specified location.
		 * 
		 * @param key
		 *            the key to insert.
		 * @param value
		 *            the value to insert.
		 * @param pos
		 *            the insertion position
		 */
		protected void insert(Object key, Object value, int pos) {
			for (int i = numberOfKeys; i > pos; i--) {
				keys[i] = keys[i - 1];
				pointers[i] = pointers[i - 1];
			}
			keys[pos] = key;
			pointers[pos] = value;
			numberOfKeys++;
		}

		/**
		 * Inserts the specified key and value after the specified pointer.
		 * 
		 * @param key
		 *            the key to insert.
		 * @param value
		 *            the value to insert.
		 * @param pointer
		 *            the pointer after which the key and value will be inserted.
		 */
		protected void insertAfter(Object key, Object value, Object pointer) {
			int i = numberOfKeys;
			while (pointers[i] != pointer) {
				keys[i] = keys[i - 1];
				pointers[i + 1] = pointers[i];
				i--;
			}
			keys[i] = key;
			pointers[i + 1] = value;
			numberOfKeys++;
		}

		/**
		 * Inserts the specified key and value assuming that this Node has room for them and is a leaf node.
		 * 
		 * @param key
		 *            the key to insert.
		 * @param value
		 *            the value to insert.
		 */
		protected void insertInLeaf(Object key, Object value) {
			if (numberOfKeys == 0 || compare(key, keys[0]) < 0) {
				insert(key, value, 0);
			} else {
				int i = findIndexL(key);
				insert(key, value, i + 1);
			}
		}

	}

	/**
	 * Constructs a BPlusTree.
	 * 
	 * @param fanout
	 *            the maximum number of pointers that each node of this BPlusTree can have.
	 */
	public BPlusTree(int fanout) {
		this.fanout = fanout;
	}

	/**
	 * Copy-constructs a BPlusTree.
	 * 
	 * @param tree
	 *            another tree to copy from.
	 */
	public BPlusTree(BPlusTree tree) {
		this.fanout = tree.fanout;
		this.root = new Node(tree.root);
	}

	/**
	 * Finds the node in this BPlusTree that must be responsible for the specified key.
	 * 
	 * @param key
	 *            the search key.
	 * @return the node in this BPlusTree that must be responsible for the specified key.
	 */
	public Node find(Object key) {

		Node c = root;
		while (!c.isLeafNode()) {
			int i = c.findIndexGE(key); // find smallest i such that c.keys[i] >= key
			if (i < 0) { // if no i such that c.keys[i] >= key
				c = (Node) c.getLastNonNullPointer();
			} else if (compare(key, c.keys[i]) == 0) {
				c = (Node) c.pointers[i + 1];
			} else { // if c.keys[i] = key
				c = (Node) c.pointers[i];
			}
		}
		return c;
	}


	/**
	 * Finds the parent node of the specified node.
	 * 
	 * @param node
	 *            the node of which the parent needs to be found.
	 * @return the parent node of the specified node; null if the parent cannot be found.
	 */
	public Node findParent(Node node) {
		Node p = root;
		while (p != null) {
			Object key = node.keys[0];
			int i = p.findIndexGE(key); // find smallest i such that p.keys[i] >= key
			Node c;
			if (i < 0) { // if no i such that p.keys[i] >= key
				c = (Node) p.getLastNonNullPointer();
			} else if (compare(key, p.keys[i]) == 0) {
				c = (Node) p.pointers[i + 1];
			} else { // if p.keys[i] = key
				c = (Node) p.pointers[i];
			}
			if (c == node) { // if found the parent of the node.
				return p;
			}
			p = c;
		}
		return null;
	}

	/**
	 * Inserts the specified key and the value into this BPlusTree.
	 * 
	 * @param key
	 *            the key to insert.
	 * @param value
	 *            the value to insert.
	 */
	public void insert(Object key, Object value) {
		Node l;
		if (root == null) { // if the root is null
			root = new Node(fanout);
			l = root;
		} else { // if root is not null
			l = find(key);
		}
		if (l.hasRoom()) { // if node l has room for the new entry
			l.insertInLeaf(key, value);
		} else { // if split is required (l is a leaf node)
			Node t = new Node(fanout + 1); // create a temporary node
			for (int i = 0; i < l.numberOfKeys; i++) { // copy everything to the temporary node
				t.insert(l.keys[i], l.pointers[i], i);
			}
			t.insertInLeaf(key, value); // insert the key and values to the temporary node
			Node nl = new Node(fanout); // create a new leaf node
			nl.pointers[nl.pointers.length - 1] = l.pointers[l.pointers.length - 1]; // set the last pointer of n to
			// node nl
			l.clear(); // clear node l
			l.pointers[l.pointers.length - 1] = nl; // set the last pointer of l to nl
			int m = (int) Math.ceil(fanout / 2.0); // compute the split point
			for (int i = 0; i < m; i++) { // put the first half into node l
				l.insert(t.keys[i], t.pointers[i], i);
			}
			for (int i = m; i < t.numberOfKeys; i++) { // put the second half to node nl
				nl.insert(t.keys[i], t.pointers[i], i - m);
			}
			insertInParent(l, nl.keys[0], nl); // use the first key of nl as the separator.
		}
	}

	/**
	 * Inserts pointers to the specified nodes into an appropriate parent node.
	 * 
	 * @param n
	 *            a node.
	 * @param key
	 *            the key that splits the nodes
	 * @param nn
	 *            a new node.
	 */
	void insertInParent(Node n, Object key, Node nn) {
		if (n == root) { // if the root was split
			root = new Node(fanout); // create a new node
			root.insert(key, n, 0); // make the new root point to the nodes.
			root.pointers[1] = nn;
			return;
		}
		Node p = findParent(n);
		if (p.hasRoom()) {
			p.insertAfter(key, nn, n); // insert key and nn right after n
		} else { // if split is required
			Node t = new Node(fanout + 1); // create a temporary node
			for (int i = 0; i < p.numberOfKeys; i++) { // copy everything of p to the temporary node
				t.insert(p.keys[i], p.pointers[i], i);
			}
			t.pointers[p.numberOfKeys] = p.pointers[p.numberOfKeys];
			t.insertAfter(key, nn, n); // insert key and nn after n
			p.clear(); // clear p
			int m = (int) Math.ceil(fanout / 2.0); // compute the split point

			for (int i = 0; i < m - 1; i++) { // put the first half back to p
				p.insert(t.keys[i], t.pointers[i], i);
			}
			p.pointers[m - 1] = t.pointers[m - 1];

			Node np = new Node(fanout); // create a new node
			for (int i = m; i < t.numberOfKeys; i++) { // put the second half to np
				np.insert(t.keys[i], t.pointers[i], i - m);
			}
			np.pointers[t.numberOfKeys - m] = t.pointers[t.numberOfKeys];

			insertInParent(p, t.keys[m - 1], np); // use the middle key as the separator
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected int compare(Object k1, Object k2) {
		return ((Comparable) k1).compareTo(k2);
	}

	/**
	 * Deletes the specified key and the value from this BPlusTree.
	 * 
	 * @param key
	 *            the key to delete.
	 * @param value
	 *            the value to delete.
	 */
	public void delete(Object key, Object value) {
		// please implement the body of this method so that we can remove key-value pairs from the tree (refer to page
		// 498 in the text book).

		/*procedure delete(value K, pointer P)
		find the leaf node L that contains (K, P)
		delete entry(L, K, P)*/
		Node l;
		l = find(key);

		try{			
			System.out.println("leafnode:"+l);  // finds the leaf node l that contains key
			deleteEntry(l, key, value); // calls deleteEntry function to delete node

		}
		catch (Exception exception)
		{
			// Display the keys which is not found in the BPlus tree
			JOptionPane.showMessageDialog(null, "Key "+ key+ " Not found. Please Proceed", "", 
					JOptionPane.PLAIN_MESSAGE);
		}
	}

	/**
	 * Deletes the specified node, key and the value from this BPlusTree.
	 * 
	 * @param node
	 *            the node to delete.
	 * @param key
	 *            the key to delete.
	 * @param value
	 *            the value to delete.
	 */

	void deleteEntry(Node n,Object key, Object value){

		int init = n.findIndexGE(key); // Returns the first index i such that keys[i] >= the given key
		System.out.println("Init "+init);
		Node parent=findParent(n); // Will give the parent node
		int flag=0 ;

		for (Object compare: n.keys) {
			if(compare!=null && compare.equals(key))
				flag = 1;
			System.out.println(flag);
		}

		System.out.println(key+" numberOf Nodes"+n.numberOfKeys);


		// Check if the node is a leaf Node
		if (n.isLeafNode()) {
			for (int i = init; i < n.numberOfKeys; i++) {

				if (i==n.numberOfKeys-1)      // If key found
				{
					n.keys[i] = null;         // Delete the key
					//n.numberOfKeys--;
				}
				else 						  // If key not found
					n.keys[i] = n.keys[i+1];  // traverse forward 

				if (i==0 && n.numberOfKeys==1 && parent.countPointers()>2 && parent==root)
					redistribution(parent,key); // Call re-distribution
				else if(i==0 && n.numberOfKeys==1 && parent.countPointers()==2 && parent==root){
					root=(Node) parent.pointers[parent.findIndexLE(key)];
					root.pointers[fanout-1]=null;
				}
			}
		} n.numberOfKeys--;  // Decrement the number of keys
	}

	/**
	 * Redistributes the specified node and key from this BPlusTree.
	 * 
	 * @param node
	 *            the node and key.
	 *            to re-distribute
	 */

	public void redistribution(Node parent,Object key){

		for(int j=parent.findIndexLE(key)+1;j<parent.countPointers()-1;j++)
		{
			parent.keys[j-1]=parent.keys[j];
			parent.pointers[j]=parent.pointers[j+1];
		}
		parent.keys[fanout-2]=null;	
		parent.pointers[fanout-1]=null;
	}


	/**
	 * Merges the specified nodes of this BPlusTree.
	 * 
	 * @param node
	 *            the leftNode.
	 * @param node
	 *            the rightNode.            
	 *            to do merging
	 */

	public void merge(Node leftNode,Node rightNode){

		Object key = null,value = null;

		// Checking for predecessor value
		if(leftNode.numberOfKeys < rightNode.numberOfKeys){

			// swap the nodes using temporary nodes
			Node temp =null;
			temp = leftNode;
			leftNode = rightNode;
			rightNode = temp;

			if (leftNode.isLeafNode()) {
				// Copy all values of leftNode to rightNode
				rightNode = new Node(leftNode); // Calling Copy-constructs a Node.
			}
			else {
					rightNode.pointers = leftNode.pointers; 
					delete(key, value); // Call delete method
			}
		}
	}
}


