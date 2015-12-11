package bfbc.photolib;

import com.google.gson.annotations.Expose;

public class File implements Entity {
	
	@Expose
	private int id;
	@Expose
	private String name;
	@Expose
	private String type;
	
	private ChangesHandler changesHandler;
	private EntityContainer parent;
	
	protected void reportChange(String item, String value) {
		changesHandler.reportChange(path().append(item), value);
	}
	public CommandPath path() {
		return parent.path().append("item").append(String.valueOf(id));
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
		reportChange("name", name);
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
		reportChange("type", type);
	}
	public File(ChangesHandler changesHandler, EntityContainer parent, int id, String name, String type) {
		this.changesHandler = changesHandler;
		this.parent = parent;
		this.id = id;
		this.name = name;
		this.type = type;
	}
	public File(ChangesHandler changesHandler, EntityContainer parent, String name, String type) {
		this(changesHandler, parent, parent.getFreeId(), name, type);
	}
	
	@Override
	public int getId() {
		return id;
	}
}
