package bfbc.photolib;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.google.gson.annotations.Expose;

public class Heap implements ChangesHandler {
	
	private static final java.io.File HEAP_FILE = new java.io.File("data/heap.xml");
	private static Heap instance = new Heap();

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

	public void reportChange(CommandPath command, Object... arguments) {
		ClientUpdateRequest cr = new ClientUpdateRequest(command.toArray(), arguments);
		for (WeakReference<HeapChangeListener> ref : listeners) {
			HeapChangeListener s = ref.get();
			if (s != null) {
				s.reportChange(cr);
			}
		}
		save(HEAP_FILE);
	}
	
	private static boolean loaded = false;
	public static Heap getInstanceFor(HeapChangeListener webSocket) {

		if (!loaded) {
			loaded = true;
			instance.load(HEAP_FILE);
		}
		
		if (webSocket != null && !instance.isConnected(webSocket)) {
			instance.listeners.add(new WeakReference<HeapChangeListener>(webSocket));
		}
		return instance;
	}
	

	@Expose
	private final ReportingArrayList<Image> images = new ReportingArrayList<Image>(this) {
		public CommandPath path() {
			return Heap.this.path().append("images");
		}
		
		void applyChange(ChangeRequest cr) {
			String item = cr.getCommand().popFirst();
			if (item.equals("add")) {
				add(new Image(Heap.this, this, "New image"));
			} else if (item.equals("remove")) {
				int id = Integer.parseInt(cr.getArgument(0));
				Image byId = getImages().findById(id);
				for (File f : byId.getFiles()) {
					String fileName = f.getName();
					java.io.File file = new java.io.File("data/" + fileName);
					file.delete();
				}
				
				remove(byId);
			} else {
				throw new RuntimeException("Invalid change request: " + cr.getCommand());
			}
		}
	};
	
	protected CommandPath path() {
		return new CommandPath();
	}
	
	public ReportingArrayList<Image> getImages() {
		return images;
	}
	
	private Heap() {

	}
	
	void load(java.io.File xmlSource) {
		SAXBuilder saxBuilder = new SAXBuilder();
		
		try {
			Document doc = saxBuilder.build(xmlSource);
			Element root = doc.getRootElement();
			if (root.getName().equals("heap")) {
				List<Element> imageElements = root.getChildren("image");
				for (Element imgEl : imageElements) {
					Image img = new Image(this, getImages(), Integer.parseInt(imgEl.getAttributeValue("id")), imgEl.getAttributeValue("title"));
					this.getImages().add(img);
					List<Element> fileElements = imgEl.getChildren("file");
					for (Element fileEl : fileElements) {
						File file = new File(this, img.getFiles(), Integer.parseInt(fileEl.getAttributeValue("id")), fileEl.getAttributeValue("name"), fileEl.getAttributeValue("type"));
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
	
	public void applyChange(ChangeRequest cr) throws InvalidChangeRequestException {
		String item = cr.getCommand().popFirst();
		if (item.equals("image")) {
			int id = Integer.parseInt(cr.getCommand().popFirst());
			Image byId = getImages().findById(id);
			byId.applyChange(cr);
		} else if (item.equals("images")) {
			images.applyChange(cr);
		}
	}
	
	
}
