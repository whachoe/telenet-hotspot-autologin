package be.copywaste.telenethotspotconnector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.copywaste.telenethotspotconnector.telenetHotspotConnectorApplication.ApiException;

public class hotspotConnect extends baseConnect {
	// URLS
	public static final String hotspotfronturl = "https://portal.telenethotspot.be/portal";
	
	// These get rewritten by doLogin since it's different for each hotspot
	public static String baseurl = "";
	private static String loginurl = "https://portal.telenethotspot.be/logon/welcome.jsp";
	private static String logouturl = "https://portal.telenethotspot.be/logout";
	
	// Old urls:
	// private static String loginurl = "https://portal.telenethotspot.be/at/id/0940/1002/welcome.jsp?c=std";
	// private static String logouturl = "https://portal.telenethotspot.be/at/id/0940/1002/welcome.jsp?logout=yes";
	
	public hotspotConnect() {
		super();
	}
	public hotspotConnect(String userid1, String userpw1) {
		super(userid1, userpw1);
	}
	
	public boolean doLogin()
    {
    	boolean login_status = false;
    	
		// Go to welcome page first
		String loginreturn = null;
		try {
			loginreturn = telenetHotspotConnectorApplication.getSSLPage(hotspotfronturl);
		} catch (Exception e) {
			telenetHotspotConnectorApplication.logger(e);
		}
		
		// Log in	
		if (loginreturn != null) {
			try {
				login_status = telenetHotspotConnectorApplication.login(userid, userpw, loginurl).length() > 0;
			} catch (Exception e) {
				telenetHotspotConnectorApplication.logger(e);
			}
			
			if (!login_status) {
				try {
					Pattern p = Pattern.compile(".*<meta http-equiv=\"refresh\" content=\"0;URL=(.*)\" />.*"); // https://portal.telenethotspot.be/at/id/0940/1002/
					Matcher m = p.matcher(loginreturn);
					 while (m.find()) {
						 baseurl = m.group(1)+"welcome.jsp";
					     loginurl = baseurl +"?c=std";
					     logouturl = baseurl +"?logout=yes";
					     
					     telenetHotspotConnectorApplication.logger("regex match: "+loginurl);
					 }
	
					return telenetHotspotConnectorApplication.login(userid, userpw, loginurl).length() > 0;
				} catch (Exception e) {
					telenetHotspotConnectorApplication.logger(e);
				}
			} else {
				return login_status;
			}
		}
		
		return false;
    }
    
    public void doLogout() {
		try {
			telenetHotspotConnectorApplication.getSSLPage(logouturl);
		} catch (ApiException e) {
			telenetHotspotConnectorApplication.logger(e);		
		}
	}   
}
