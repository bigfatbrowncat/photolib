package bfbc.photolib;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
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
import com.google.gson.annotations.Expose;

import bfbc.photolib.Heap.Image.File;
import bfbc.photolib.HeapChangeListener;

public class Heap {
	
	private static final java.io.File HEAP_FILE = new java.io.File("data/heap.xml");
	private static Heap instance = new Heap(HEAP_FILE);

	private Gson gson;
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

	protected void reportChange(String path, String newValue) {
		for (WeakReference<HeapChangeListener> ref : listeners) {
			HeapChangeListener s = ref.get();
			if (s != null) {
				s.reportChange(path, newValue); //.broadcastUpdate(request);
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
	
	public class Image {
		public class File {
			
			@Expose
			private String name;
			@Expose
			private String type;
			
			protected void reportChange(String item, String value) {
				Heap.this.reportChange(path() + "/" + item, value);
			}
			protected String path() {
				int index = Image.this.files.indexOf(this);
				if (index == -1) {
					throw new RuntimeException("File object not found inside Image object");
				}
				return Image.this.path() + "/files[" + index + "]";
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
			
			public File(String name, String type) {
				this.name = name;
				this.type = type;
			}
		}
		
		protected String path() {
			int index = Heap.this.images.indexOf(this);
			if (index == -1) {
				throw new RuntimeException("Image object not found inside Heap object");
			}
			return Heap.this.path() + "/images[" + index + "]";
		}
		
		protected void reportChange(String item, String value) {
			Heap.this.reportChange(path() + "/" + item, value);
		}

		@Expose
		private final List<File> files = new ReportingArrayList<File>() {
			String path() {
				return Image.this.path() + "/files";
			}
		};
		
		@Expose
		private String title;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
			reportChange("title", title);
		}

		public List<File> getFiles() {
			return files;
		}
		
		public Image(String title) {
			this.title = title;
		}

	}

	@Expose
	private final List<Image> images = new ReportingArrayList<Image>() {
		String path() {
			return Heap.this.path() + "/images";
		}
	};
	
	protected String path() {
		return "/heap";
	}
	
	public List<Image> getImages() {
		return images;
	}
	
	private Heap(java.io.File xmlSource) {
    	GsonBuilder builder = new GsonBuilder();
    	builder.excludeFieldsWithoutExposeAnnotation();
    	gson = builder.create();
		SAXBuilder saxBuilder = new SAXBuilder();
		
		try {
			Document doc = saxBuilder.build(xmlSource);
			Element root = doc.getRootElement();
			if (root.getName().equals("heap")) {
				List<Element> imageElements = root.getChildren("image");
				for (Element imgEl : imageElements) {
					Image img = new Image(imgEl.getAttributeValue("title"));
					this.getImages().add(img);
					List<Element> fileElements = imgEl.getChildren("file");
					for (Element fileEl : fileElements) {
						File file = img.new File(fileEl.getAttributeValue("name"), fileEl.getAttributeValue("type"));
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
				for (File file : img.getFiles()) {
					Element fileTag = new Element("file");
					fileTag.setAttribute("name", file.getName());
					fileTag.setAttribute("type", file.getType());
					imgTag.addContent(fileTag);
				}
				root.addContent(imgTag);
			}
			
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(HEAP_FILE), "UTF-8"
			)));
		} catch (IOException e) {
			throw new RuntimeException("Problem saving " + xmlTarget.getAbsolutePath() + " file", e);
		}
	}
	
	public abstract class ReportingArrayList<T> extends ArrayList<T> {
		abstract String path();
		
		@Override
		public boolean add(T obj) {
			boolean res = super.add(obj);
			Heap.this.reportChange(path() + "/add", gson.toJson(obj));
			return res;
		}
		
		@Override
		public void add(int index, T element) {
			super.add(index, element);
			Heap.this.reportChange(path() + "/add(" + index + ")", gson.toJson(element));
		}
		
		@Override
		public boolean addAll(Collection<? extends T> c) {
			boolean res = super.addAll(c);
			Heap.this.reportChange(path() + "/addAll", gson.toJson(c));
			return res;
		}
		
		@Override
		public void clear() {
			super.clear();
			Heap.this.reportChange(path() + "/clear", "");
		}
		
		@Override
		public boolean remove(Object o) {
			int index = this.indexOf(o);
			if (index != -1) {
				boolean res = super.remove(o);
				Heap.this.reportChange(path() + "/remove(" + index + ")", "");
				return res;
			} else {
				return false;
			}
		}
		
		@Override
		public boolean addAll(int index, Collection<? extends T> c) {
			boolean res = super.addAll(index, c);
			Heap.this.reportChange(path() + "/addAll(" + index + ")", gson.toJson(c));
			return res;
		}
		
		@Override
		public T remove(int index) {
			T res = super.remove(index);
			Heap.this.reportChange(path() + "/remove(" + index + ")", "");
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
			Heap.this.reportChange(path() + "/removeAll(" + gson.toJson(indices) + ")", "");
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
		
		
	}
	
	public String toJson() {
		return gson.toJson(this);
	}
}
