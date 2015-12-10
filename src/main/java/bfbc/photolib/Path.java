package bfbc.photolib;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class Path implements Cloneable {
	@Expose
	private ArrayList<String> items = new ArrayList<>();
	
	@SuppressWarnings("unchecked")
	@Override
	protected Path clone() {
		Path p = new Path();
		p.items = (ArrayList<String>) items.clone();
		return p;
	}
	
	public Path append(String item) { 
		Path newPath = clone();
		newPath.items.add(item); 
		return newPath;
	}
	
	public String popFirst() {
		String res = items.get(0);
		items.remove(0);
		return res;
	}
	
	public Path() { 
		
	}
	
	public String[] toArray() {
		return items.toArray(new String[] {});
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String slash = "";
		for (int i = 0; i < items.size(); i++) {
			sb.append(slash);
			sb.append(items.get(i));
			slash = "/";
		}
		return sb.toString();
	}
}