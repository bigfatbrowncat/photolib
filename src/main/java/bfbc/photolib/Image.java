package bfbc.photolib;

import com.google.gson.annotations.Expose;

public class Image implements Entity {
	
	@Expose
	private int id;
	@Expose
	private String title;

	private final ChangesHandler changesHandler;
	private final EntityContainer parent;
	
	protected void reportChange(String item, String value) {
		changesHandler.reportChange(path().append(item), value);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		reportChange("title", title);
	}

	public ReportingArrayList<File> getFiles() {
		return files;
	}
	
	public Image(ChangesHandler changesHandler, EntityContainer parent, int id, String title) {
		this.changesHandler = changesHandler;
		this.parent = parent;
		this.id = id;
		this.title = title;
		
		files = new ReportingArrayList<File>(changesHandler) {
			public CommandPath path() {
				return Image.this.path().append("files");
			}
		
			@Override
			void applyChange(ChangeRequest cr) {
				// No change requests are supported for files yet
			}
		};
	}
	public Image(ChangesHandler changesHandler, EntityContainer parent, String title) {
		this(changesHandler, parent, parent.getFreeId(), title);
	}

	@Override
	public int getId() {
		return id;
	}
	
	public void applyChange(ChangeRequest cr) throws InvalidChangeRequestException {
		String item = cr.getCommand().popFirst();
		if (item.equals("title")) {
			String newTitle = cr.getArgument(0);
			setTitle(newTitle);
		} else {
			throw new InvalidChangeRequestException(cr);
		}
	}
	
	
	public CommandPath path() {
		return parent.path().append("item").append(String.valueOf(id));
	}
	
	@Expose
	private final ReportingArrayList<File> files;
}
