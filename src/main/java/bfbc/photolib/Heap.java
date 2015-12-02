package bfbc.photolib;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import bfbc.photolib.Heap.Image.File;

public class Heap {
	private static WeakReference<Heap> instanceRef;
	
	public static Heap getInstance() {
		Heap heap = null;
		if (instanceRef != null) {
			heap = instanceRef.get();
		}
		if (heap == null) {
			heap = new Heap();
			instanceRef = new WeakReference<Heap>(heap);
			try {
				heap.load(new java.io.File("data/heap.xml"));
			} catch (JDOMException | IOException e) {
				throw new RuntimeException(e);	// TODO Special exception
			}
		}
		return heap;
	}
	
	public class Image {
		public class File {
			private String name;
			private String type;
			
			public int indexInOwner() {
				return Image.this.files.indexOf(this);
			}
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public String getType() {
				return type;
			}
			public void setType(String type) {
				this.type = type;
			}
			
			
		}

		private final List<File> files = new ArrayList<>();

		private String title;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public List<File> getFiles() {
			return files;
		}
		
		
	}

	private final List<Image> images = new ArrayList<>();
	
	public List<Image> getImages() {
		return images;
	}
	
	public void load(java.io.File xmlSource) throws JDOMException, IOException {
		images.clear();
		
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
	}
}
