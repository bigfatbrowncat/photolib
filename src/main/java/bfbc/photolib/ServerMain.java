package bfbc.photolib;


import static spark.Spark.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.JDOMException;

import bfbc.photolib.upload.FileUploaderWebSocket;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

public class ServerMain {
	private static final String STATIC_ROOT = "/" + ServerMain.class.getPackage().getName().replace('.', '/') + "/root";
	
    public static void main(String[] args) {
    	port(8081);
    	staticFileLocation(STATIC_ROOT);

    	webSocket("/upload/image", FileUploaderWebSocket.class);
    	webSocket("/status", StatusWebSocket.class);
    	 	
		get("/image/:name", (request, response) -> {
			
			String filePath = "data" + File.separator + request.params(":name");
			
			response.type("image/jpeg");
			byte[] bytes = Files.readAllBytes(Paths.get(filePath));
			HttpServletResponse raw = response.raw();

			raw.getOutputStream().write(bytes);
			raw.getOutputStream().flush();
			raw.getOutputStream().close();

			return raw;
		});

		get("/test", (request, response) -> {
			
			Heap.getInstanceFor(null).getImages().get(2).getFiles().get(0).setName("image1.jpg");
			return "success";
		});

		exception(IOException.class, (exception, request, response) -> {
			exception.printStackTrace();
			response.status(404);
			response.body("Input/Output exception occured: " + ((IOException)exception).getMessage());
		});
		
		init();
    }
}