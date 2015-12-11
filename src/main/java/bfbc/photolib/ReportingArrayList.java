package bfbc.photolib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public abstract class ReportingArrayList<T extends Entity> extends ArrayList<T> implements EntityContainer {
	public abstract CommandPath path();
	abstract void applyChange(ChangeRequest cr) throws InvalidChangeRequestException;

	private ChangesHandler changesHandler;
	
	public ReportingArrayList(ChangesHandler changesHandler) {
		super();
		this.changesHandler = changesHandler;
	}
	
	public ReportingArrayList(ChangesHandler changesHandler, Collection<? extends T> c) {
		super(c);
		this.changesHandler = changesHandler;
	}
	
	public ReportingArrayList(ChangesHandler changesHandler, int initialCapacity) {
		super(initialCapacity);
		this.changesHandler = changesHandler;
	}
	
	@Override
	public boolean add(T obj) {
		boolean res = super.add(obj);
		changesHandler.reportChange(path().append("add"), obj);
		return res;
	}
	
	@Override
	public void add(int index, T element) {
		super.add(index, element);
		changesHandler.reportChange(path().append("add"), index, element);
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean res = super.addAll(c);
		changesHandler.reportChange(path().append("addAll"), c);
		return res;
	}
	
	@Override
	public void clear() {
		super.clear();
		changesHandler.reportChange(path().append("clear"));
	}
	
	@Override
	public boolean remove(Object o) {
		if (o instanceof Entity && this.contains(o)) {
			int index = ((Entity)o).getId();
			if (index != -1) {
				boolean res = super.remove(o);
				changesHandler.reportChange(path().append("remove"), index);
				return res;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		boolean res = super.addAll(index, c);
		changesHandler.reportChange(path().append("addAll"), index, c);
		return res;
	}
	
	@Override
	public T remove(int index) {
		T res = super.remove(index);
		changesHandler.reportChange(path().append("remove"), index);
		return res;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		List<Integer> indices = new ArrayList<>();
		for (Object o : c) {
			int index = indexOf(o);
			if (index != -1) {
				indices.add(index);
			}
		}
		changesHandler.reportChange(path().append("removeAll"), indices);
		return super.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("Unimplemented");
	}

	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		throw new RuntimeException("Unimplemented");
	}
	
	@Override
	public void sort(Comparator<? super T> c) {
		throw new RuntimeException("Unimplemented");
	}
	
	public T findById(int id) {
		for (T item : this) {
			if (item.getId() == id) return item; 
		}
		return null;
	}
	
	public int getFreeId() {
		int res = 0;
		for (T item : this) {
			if (item.getId() == res) res++; 
		}
		return res;
	}
	
}