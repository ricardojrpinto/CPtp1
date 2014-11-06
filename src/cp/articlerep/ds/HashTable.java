package cp.articlerep.ds;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Ricardo Dias
 */
public class HashTable<K extends Comparable<K>, V> implements Map<K, V> {

	private static class Node {
		public Object key;
		public Object value;
		public Node next;

		public Node(Object key, Object value, Node next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}
	
	private static class HeadSentinel extends Node{
		
		private ReadWriteLock rwl;
		private Lock r,w;
		
		public HeadSentinel(){
			super(null,null,null); 
			rwl = new ReentrantReadWriteLock();
			r = rwl.readLock();
			w = rwl.writeLock();
			}
		
		public void wrLockList(){
			w.lock();
		}
		
		public void wrUnlockList(){
			w.unlock();
		}
		
		public void rdLockList(){
			r.lock();
		}
		
		public void rdUnlockList(){
			r.unlock();
		}
	}

	private Node[] table;

	public HashTable() {
		this(1000);
	}

	public HashTable(int size) {
		this.table = new Node[size];
		for(int i = 0; i < size;i++)
		{
			table[i] = new HeadSentinel();
			}
	}

	private int calcTablePos(K key) {
		return Math.abs(key.hashCode()) % this.table.length;
	}
	
	public void writeLock(K key){
		int pos = this.calcTablePos(key);
		((HeadSentinel) this.table[pos]).wrLockList();
	}
	
	public void writeUnlock(K key){
		int pos = this.calcTablePos(key);
		((HeadSentinel) this.table[pos]).wrUnlockList();
	}
	
	public void readLock(K key){
		int pos = this.calcTablePos(key);
		((HeadSentinel) this.table[pos]).rdLockList();
	}
	
	public void readUnlock(K key){
		int pos = this.calcTablePos(key);
		((HeadSentinel) this.table[pos]).rdUnlockList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V put(K key, V value){
		
		int pos = this.calcTablePos(key);//calculate position for insertion at the table
		
		Node n = this.table[pos].next;

		while (n != null && !n.key.equals(key)) {
			n = n.next; //colision detected
		}

		if (n != null) { // node value replacement
			V oldValue = (V) n.value;
			n.value = value;
			return oldValue;
		}

		Node nn = new Node(key, value, this.table[pos].next); // create node
		this.table[pos].next = nn;
		
		return null;
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V remove(K key){
		
		int pos = this.calcTablePos(key);
		
		Node p = this.table[pos].next;
		if (p == null) {
			return null;
		}

		if (p.key.equals(key)) {
			this.table[pos].next = p.next;
			
			return (V) p.value;
		}

		Node n = p.next;
		while (n != null && !n.key.equals(key)) {
			p = n;
			n = n.next;
		}

		if (n == null) {
			return null;
		}

		p.next = n.next;
		
		return (V) n.value;
		
	}


	@SuppressWarnings("unchecked")
	@Override
	public V get(K key) { //read thread's will see the last update of the hashmap ... implicit read Lock
		int pos = this.calcTablePos(key);
		
		
		Node n = this.table[pos].next;
		while (n != null && !n.key.equals(key)) {
			n = n.next;
		}
				
		return (V) (n != null ? n.value : null);
	}

	@Override
	public boolean contains(K key) {
		return get(key) != null;
	}
	


	/**
	 * No need to protect this method from concurrent interactions
	 */
	@Override
	public Iterator<V> values() {
		return new Iterator<V>() {

			private int pos = -1;
			private Node nextBucket = advanceToNextBucket();

			private Node advanceToNextBucket() {
				pos++;
				while (pos < HashTable.this.table.length
						&& HashTable.this.table[pos].next == null) {
					pos++;
				}
				if (pos < HashTable.this.table.length)
					return HashTable.this.table[pos].next;

				return null;
			}

			@Override
			public boolean hasNext() {
				return nextBucket != null;
			}

			@SuppressWarnings("unchecked")
			@Override
			public V next() {
				V result = (V) nextBucket.value;

				nextBucket = nextBucket.next != null ? nextBucket.next
						: advanceToNextBucket();

				return result;
			}

		};
	}

	@Override
	public Iterator<K> keys() {
		return null;
	}
	
	/*No concurrency -- Hashmap Invariants*/
	public boolean validate(){
		
		for(int i = 0; i < table.length; i++){
			if(!(table[i] instanceof HeadSentinel))
				return false;
		}
		
		return true;
		
	}

}
