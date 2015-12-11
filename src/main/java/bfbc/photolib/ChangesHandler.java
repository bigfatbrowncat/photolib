package bfbc.photolib;

public interface ChangesHandler {
	void reportChange(CommandPath command, Object... arguments);
}
