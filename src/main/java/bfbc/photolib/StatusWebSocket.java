package bfbc.photolib;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import bfbc.photolib.Heap.ClientUpdateRequest;

@WebSocket
public class StatusWebSocket implements HeapChangeListener {

	enum RequestType {
		LIST("list"), CHANGE("change");
		
		public final String type;
		RequestType(String type) {
			this.type = type;
		}
	}
	
	@Override
	public void reportChange(ClientUpdateRequest cr) {
		String req = cr.toJson();
		System.out.println("Sending update request: " + req);
		broadcastUpdate(req);
	}
	
	private void sendUpdate(Session s, String request) throws IOException {
		s.getRemote().sendString("update:" + request);
	}

	private void sendInit(Session s) {
		try {
			String json = Heap.getInstanceFor(null).toJson();
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
	
    // Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    
    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
        sendInit(session);
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
    	Heap heap = Heap.getInstanceFor(this);
    	try {
			heap.applyChange(ChangeRequest.fromJson(message));
		} catch (InvalidChangeRequestException | CantParseRequestException e) {
			session.getRemote().sendString("error:" + GlobalServices.getGson().toJson(e));
		}
    }

}
