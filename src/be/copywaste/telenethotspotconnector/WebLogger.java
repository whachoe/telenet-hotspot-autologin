package be.copywaste.telenethotspotconnector;

public interface WebLogger {
	public static final String CONNECTED = "on";

	public LoggerResult login(String user, String password);
}
