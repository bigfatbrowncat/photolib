package bfbc.photolib;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

import bfbc.photolib.Heap.Image.File;

public class Heap {
	
	private static final java.io.File HEAP_FILE = new java.io.File("data/heap.xml");
	private static Heap instance = new Heap(HEAP_FILE);

	private final Set<WeakReference<HeapChangeListener>> listeners = new HashSet<>();
	
	public Set<WeakReference<HeapChangeListener>> getListeners() {
		return listeners;
	}
	
	protected boolean isConnected(HeapChangeListener listener) {
		for (WeakReference<HeapChangeListener> ref : listeners) {
			HeapChangeListener s = ref.get();
			if (s == listener) {
				return true;
			}
		}
		return false;
	}

	protected void reportChange(Path command, Object... arguments) {
		ClientUpdateRequest cr = new ClientUpdateRequest(command.toArray(), arguments);
		for (WeakReference<HeapChangeListener> ref : listeners) {
			HeapChangeListener s = ref.get();
			if (s != null) {
				s.reportChange(cr);
			}
		}
		save(HEAP_FILE);
	}
	
	
	public static Heap getInstanceFor(HeapChangeListener webSocket) {
		if (webSocket != null && !instance.isConnected(webSocket)) {
			instance.listeners.add(new WeakReference<HeapChangeListener>(webSocket));
		}
		return instance;
	}
	
	public class Image implements ItemWithId {
		public class File implements ItemWithId {
			
			@Expose
			private int id;
			@Expose
			private String name;
			@Expose
			private String type;
			
			protected void reportChange(String item, String value) {
				Heap.this.reportChange(path().append(item), value);
			}
			protected Path path() {
				return Image.this.files.path().append("item").append(String.valueOf(id));
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
			public File(int id, String name, String type) {
				this.id = id;
				this.name = name;
				this.type = type;
			}
			public File(String name, String type) {
				this(Image.this.getFiles().getFreeId(), name, type);
			}
			
			@Override
			public int getId() {
				return id;
			}
		}
		
		protected Path path() {
			return Heap.this.images.path().append("item").append(String.valueOf(id));
		}
		
		protected void reportChange(String item, String value) {
			Heap.this.reportChange(path().append(item), value);
		}

		@Expose
		private final ReportingArrayList<File> files = new ReportingArrayList<File>() {
			Path path() {
				return Image.this.path().append("files");
			}

			@Override
			void applyChange(ChangeRequest cr) {
				// TODO Auto-generated method stub
				
			}
		};
		
		@Expose
		private int id;
		@Expose
		private String title;

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
		
		public Image(int id, String title) {
			this.id = id;
			this.title = title;
		}
		public Image(String title) {
			this(Heap.this.getImages().getFreeId(), title);
		}

		@Override
		public int getId() {
			return id;
		}
		
		public void applyChange(ChangeRequest cr) throws InvalidChangeRequestException {
			String item = cr.command.popFirst();
			if (item.equals("title")) {
				String newTitle = cr.arguments[0];
				setTitle(newTitle);
			} else {
				throw new InvalidChangeRequestException(cr);
			}
		}
	}

	@Expose
	private final ReportingArrayList<Image> images = new ReportingArrayList<Image>() {
		Path path() {
			return Heap.this.path().append("images");
		}
		
		void applyChange(ChangeRequest cr) {
			String item = cr.command.popFirst();
			if (item.equals("remove")) {
				int id = Integer.parseInt(cr.arguments[0]);
				Image byId = getImages().findById(id);
				for (File f : byId.getFiles()) {
					String fileName = f.getName();
					java.io.File file = new java.io.File("data/" + fileName);
					file.delete();
				}
				
				getImages().remove(byId);
			} else {
				throw new RuntimeException("Invalid change request: " + cr.command);
			}
		}
	};
	
	protected Path path() {
		return new Path();
	}
	
	public ReportingArrayList<Image> getImages() {
		return images;
	}
	
	private Heap(java.io.File xmlSource) {
		SAXBuilder saxBuilder = new SAXBuilder();
		
		try {
			Document doc = saxBuilder.build(xmlSource);
			Element root = doc.getRootElement();
			if (root.getName().equals("heap")) {
				List<Element> imageElements = root.getChildren("image");
				for (Element imgEl : imageElements) {
					Image img = new Image(Integer.parseInt(imgEl.getAttributeValue("id")), imgEl.getAttributeValue("title"));
					this.getImages().add(img);
					List<Element> fileElements = imgEl.getChildren("file");
					for (Element fileEl : fileElements) {
						File file = img.new File(Integer.parseInt(fileEl.getAttributeValue("id")), fileEl.getAttributeValue("name"), fileEl.getAttributeValue("type"));
						img.getFiles().add(file);
					}
				}
			} else {
				throw new RuntimeException("The root element should be <heap>");
			}
		} catch (JDOMException | IOException e) {
			throw new RuntimeException("Problem loading or parsing the " + xmlSource.getAbsolutePath() + " file", e);
		}
	}
	
	void save(java.io.File xmlTarget) {
		try {
			Document doc = new Document();
			Element root = new Element("heap");
			doc.setRootElement(root);
			
			for (Image img : getImages()) {
				Element imgTag = new Element("image");
				imgTag.setAttribute("title", img.getTitle());
				imgTag.setAttribute("id", String.valueOf(img.getId()));
				for (File file : img.getFiles()) {
					Element fileTag = new Element("file");
					fileTag.setAttribute("name", file.getName());
					fileTag.setAttribute("type", file.getType());
					fileTag.setAttribute("id", String.valueOf(file.getId()));
					imgTag.addContent(fileTag);
				}
				root.addContent(imgTag);
			}
			
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(HEAP_FILE), StandardCharsets.UTF_8)
			));
		} catch (IOException e) {
			throw new ServerException("Problem saving " + xmlTarget.getAbsolutePath() + " file", e);
		}
	}
	
	public interface ItemWithId {
		int getId();
	}
	
	public abstract class ReportingArrayList<T extends ItemWithId> extends ArrayList<T> {
		abstract Path path();
		abstract void applyChange(ChangeRequest cr) throws InvalidChangeRequestException;

		@Override
		public boolean add(T obj) {
			boolean res = super.add(obj);
			Heap.this.reportChange(path().append("add"), obj);
			return res;
		}
		
		@Override
		public void add(int index, T element) {
			super.add(index, element);
			Heap.this.reportChange(path().append("add"), index, element);
		}
		
		@Override
		public boolean addAll(Collection<? extends T> c) {
			boolean res = super.addAll(c);
			Heap.this.reportChange(path().append("addAll"), c);
			return res;
		}
		
		@Override
		public void clear() {
			super.clear();
			Heap.this.reportChange(path().append("clear"));
		}
		
		@Override
		public boolean remove(Object o) {
			if (o instanceof ItemWithId && this.contains(o)) {
				int index = ((ItemWithId)o).getId();
				if (index != -1) {
					boolean res = super.remove(o);
					Heap.this.reportChange(path().append("remove"), index);
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
			Heap.this.reportChange(path().append("addAll"), index, c);
			return res;
		}
		
		@Override
		public T remove(int index) {
			T res = super.remove(index);
			Heap.this.reportChange(path().append("remove"), index);
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
			Heap.this.reportChange(path().append("removeAll"), indices);
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
		
		T findById(int id) {
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
	
	public String toJson() {
		return GlobalServices.getGson().toJson(this);
	}
	
	public class ClientUpdateRequest {
		@Expose
		private String[] command;
		@Expose
		private Object[] arguments;
		
		public String[] getCommand() {
			return command;
		}
		public Object[] getArguments() {
			return arguments.clone();
		}
		public ClientUpdateRequest(String[] command, Object[] arguments) {
			super();
			this.command = command;
			this.arguments = arguments;
		}
		
		public String toJson() {
			return GlobalServices.getGson().toJson(this);
		}
	}
	
	public static class ChangeRequest {
		@Expose
		private Path command;
		@Expose
		private String[] arguments;

		public static ChangeRequest fromJson(String json) throws CantParseRequestException {
			try {
				return GlobalServices.getGson().fromJson(json, ChangeRequest.class);
			} catch (JsonSyntaxException e) {
				throw new CantParseRequestException(json);
			}
		}
		
		@Override
		public String toString() {
			String res = "command: " + command.toString() + ", arguments: ";
			String comma = "";
			for (String s : arguments) {
				res += comma + "\"" + s + "\"";
				comma = ", ";
			}
			return res;
		}
	}

	public void applyChange(ChangeRequest cr) throws InvalidChangeRequestException {
		String item = cr.command.popFirst();
		if (item.equals("image")) {
			int id = Integer.parseInt(cr.command.popFirst());
			Image byId = getImages().findById(id);
			byId.applyChange(cr);
		} else if (item.equals("images")) {
			images.applyChange(cr);
		}
	}
	
	
}
