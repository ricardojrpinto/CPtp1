package cp.articlerep.ds;


public interface List<E> {

	void add(E a);

	Iterator<E> iterator();

	E remove(int pos);

}
