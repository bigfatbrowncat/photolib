package bfbc.photolib;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebSocket
public class StatusWebSocket implements HeapChangeListener {

	enum RequestType {
		LIST("list"), CHANGE("change");
		
		public final String type;
		RequestType(String type) {
			this.type = type;
		}
	}
	
	private List<String> getFileNames() {
		Heap heap = Heap.getInstanceFor(this);
		List<Heap.Image> imgs = heap.getImages();
		ArrayList<String> fileNames = new ArrayList<>();
		for (Heap.Image img : imgs) {
			fileNames.add(img.getFiles().get(0).getName());
		}
		return fileNames;
	}
	
	//private static List<String> strings = new ArrayList<String>();
	private Gson gson;
	
	@Override
	public void reportChange(String path, String newValue) {
		try {
			broadcastUpdate(URLEncoder.encode(path, "UTF-8") + "=" + URLEncoder.encode(newValue, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Impossible situation", e);
		}
	}
	
	private void sendUpdate(Session s, String request) throws IOException {
		s.getRemote().sendString("update:" + request);
		System.out.println("request: " + request);
	}

	private void sendFiles(Session s, List<String> fileNames) {
		try {
			String json = gson.toJson(Heap.getInstanceFor(null));
			s.getRemote().sendString("init:" + json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void broadcastUpdate(String request) {
		synchronized (Heap.getInstanceFor(this)) {
    		List<Session> ss = new ArrayList<Session>(sessions);
    		for (Session s : ss) {
    			try {
    				sendUpdate(s, request);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
		}
	}
	
	public void broadcastFiles() {		
		List<String> fileNames = getFileNames();
		
		synchronized (Heap.getInstanceFor(this)) {
    		List<Session> ss = new ArrayList<Session>(sessions);
    		for (Session s : ss) {
    			sendFiles(s, fileNames);
    		}

		}
	}
	
    // Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    
    public StatusWebSocket() {
    	GsonBuilder builder = new GsonBuilder();
    	builder.excludeFieldsWithoutExposeAnnotation();
    	gson = builder.create();
	}
    
    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
        sendFiles(session, getFileNames());
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
    	
    }

}
