package be.copywaste.telenethotspotconnector.wispr;

public interface WebLogger {
	public static final String CONNECTED = "on";

	public LoggerResult login(String user, String password);
}
