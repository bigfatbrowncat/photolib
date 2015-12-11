package bfbc.photolib;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

public class ChangeRequest {
	@Expose
	private CommandPath command;
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
	
	public CommandPath getCommand() {
		return command;
	}
	
	public int getArgumentsCount() {
		return arguments.length;
	}
	
	public String getArgument(int i) {
		return arguments[i];
	}
}
