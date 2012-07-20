package be.copywaste.telenethotspotconnector;

import be.copywaste.telenethotspotconnector.wispr.LoggerResult;
import be.copywaste.telenethotspotconnector.wispr.WISPrLogger;

public class homespotConnect extends baseConnect {
	private static final String logouturl = "https://portal.telenethomespot.be/portal/wispr/logout.do";
	
	public homespotConnect() {
		super();
	}
	public homespotConnect(String userid1, String userpw1) {
		super(userid1, userpw1);
	}
	
	public boolean doLogin() {
		boolean login_status = false;
		
		WISPrLogger logger = new WISPrLogger();
		LoggerResult result = logger.login(userid, userpw);
		telenetHotspotConnectorApplication.logger(result.toString());
		login_status = result.getResult().equalsIgnoreCase("50");
		
		return login_status;
	}
	
	public void doLogout() {
		try {
			telenetHotspotConnectorApplication.getSSLPage(logouturl);
		} catch (Exception e) {
			telenetHotspotConnectorApplication.logger(e);
		}
	}   
}
