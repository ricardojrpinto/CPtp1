package cp.articlerep.ds;

/**
 * @author Ricardo Dias
 */
public interface Map<K extends Comparable<K>, V> {
	public V put(K key, V value);
	public boolean contains(K key);
	public V remove(K key);
	public V get(K key);
	
	public Iterator<V> values();
	public Iterator<K> keys();
	
	public void writeLock(K key);
	public void writeUnlock(K key);
	
	public void readLock(K key);
	public void readUnlock(K key);
	
	
	public boolean validate();
}
