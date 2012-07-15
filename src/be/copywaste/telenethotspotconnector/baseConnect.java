package be.copywaste.telenethotspotconnector;

public abstract class baseConnect {
	public static String userid;
	public static String userpw;
	
	public baseConnect() {}
	public baseConnect(final String userid1, final String userpw1) {
		userid = userid1;
		userpw = userpw1;
	}
	
	public boolean doLogin() {
		return false;
	}

	public void doLogout() {}
	
	
}
