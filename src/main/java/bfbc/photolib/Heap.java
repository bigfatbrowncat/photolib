package bfbc.photolib;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.gson.annotations.Expose;

import bfbc.photolib.Heap.Image.File;

public class Heap {
	private final Set<WeakReference<HeapChangeListener>> webSockets = new HashSet<>();
	
	protected boolean isConnected(HeapChangeListener webSocket) {
		for (WeakReference<HeapChangeListener> ref : webSockets) {
			HeapChangeListener s = ref.get();
			if (s == webSocket) {
				return true;
			}
		}
		return false;
	}

	private void reportChange(String path, String newValue) {
		for (WeakReference<HeapChangeListener> ref : webSockets) {
			HeapChangeListener s = ref.get();
			if (s != null) {
				s.reportChange(path, newValue); //.broadcastUpdate(request);
			}
		}
	}
	
	private static Heap instance = new Heap(new java.io.File("data/heap.xml"));
	
	public static Heap getInstanceFor(HeapChangeListener webSocket) {
		if (webSocket != null && !instance.isConnected(webSocket)) {
			instance.webSockets.add(new WeakReference<HeapChangeListener>(webSocket));
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
				return Image.this.path() + "/files[" + Image.this.files.indexOf(this) + "]";
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
		}
		
		protected String path() {
			return Heap.this.path() + "/images[" + Heap.this.images.indexOf(this) + "]";
		}
		protected void reportChange(String item, String value) {
			Heap.this.reportChange(path() + "/" + item, value);
		}

		@Expose
		private final List<File> files = new ArrayList<>();

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
		
		
	}

	@Expose
	private final List<Image> images = new ArrayList<>();
	
	protected String path() {
		return "/heap";
	}
	
	public List<Image> getImages() {
		return images;
	}
	
	public Heap(java.io.File xmlSource) {
		images.clear();
		
		try {
			SAXBuilder saxBuilder = new SAXBuilder();
			Document doc = saxBuilder.build(xmlSource);
			Element root = doc.getRootElement();
			if (root.getName().equals("heap")) {
				List<Element> imageElements = root.getChildren("image");
				for (Element imgEl : imageElements) {
					Image img = new Image();
					img.setTitle(imgEl.getAttributeValue("title"));
					List<Element> fileElements = imgEl.getChildren("file");
					for (Element fileEl : fileElements) {
						File file = img.new File();
						file.setName(fileEl.getAttributeValue("name"));
						file.setType(fileEl.getAttributeValue("type"));
						img.getFiles().add(file);
					}
					this.getImages().add(img);
				}
			} else {
				throw new RuntimeException("The root element should be <heap>");
			}
		} catch (JDOMException | IOException e) {
			throw new RuntimeException("Problem loading or parsing the " + xmlSource.getAbsolutePath() + " file", e);
		}
	}
}
