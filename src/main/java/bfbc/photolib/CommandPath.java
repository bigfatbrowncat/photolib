package bfbc.photolib;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class CommandPath implements Cloneable {
	@Expose
	private ArrayList<String> items = new ArrayList<>();
	
	@SuppressWarnings("unchecked")
	@Override
	protected CommandPath clone() {
		CommandPath p = new CommandPath();
		p.items = (ArrayList<String>) items.clone();
		return p;
	}
	
	public CommandPath append(String item) { 
		CommandPath newPath = clone();
		newPath.items.add(item); 
		return newPath;
	}
	
	public String popFirst() {
		String res = items.get(0);
		items.remove(0);
		return res;
	}
	
	public CommandPath() { 
		
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