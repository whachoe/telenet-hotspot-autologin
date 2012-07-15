package be.copywaste.telenethotspotconnector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import be.copywaste.telenethotspotconnector.telenetHotspotConnectorApplication.ApiException;

public class WISPrLogger implements WebLogger {
	private static final String DEFAULT_LOGOFF_URL = "http://192.168.182.1:3990/logoff";

	public LoggerResult login(String user, String password) {
		LoggerResult res = new LoggerResult(WISPrConstants.WISPR_RESPONSE_CODE_INTERNAL_ERROR, null);
		try {
			String blockedUrlText = telenetHotspotConnectorApplication.getSSLPage(telenetHotspotConnectorApplication.testurl);
			if (!blockedUrlText.equalsIgnoreCase(CONNECTED)) {
				String WISPrXML = WISPrUtil.getWISPrXML(blockedUrlText);
				if (WISPrXML != null) {
					telenetHotspotConnectorApplication.logger("XML Found:" + WISPrXML);
					WISPrInfoHandler wisprInfo = new WISPrInfoHandler();
					android.util.Xml.parse(WISPrXML, wisprInfo);

					if (wisprInfo.getMessageType().equals(WISPrConstants.WISPR_MESSAGE_TYPE_INITIAL)
							&& wisprInfo.getResponseCode().equals(WISPrConstants.WISPR_RESPONSE_CODE_NO_ERROR)) {
						res = tryToLogin(user, password, wisprInfo);
					}
				} else {
					telenetHotspotConnectorApplication.logger("XML NOT FOUND : " + WISPrXML);
					res = new LoggerResult(WISPrConstants.WISPR_NOT_PRESENT, null);
				}
			} else {
				res = new LoggerResult(WISPrConstants.ALREADY_CONNECTED, DEFAULT_LOGOFF_URL);
			}
		} catch (Exception e) {
			telenetHotspotConnectorApplication.logger(e);
			res = new LoggerResult(WISPrConstants.WISPR_RESPONSE_CODE_INTERNAL_ERROR, null);
		}
		telenetHotspotConnectorApplication.logger("WISPR Login Result: " + res);

		return res;
	}

	private LoggerResult tryToLogin(String user, String password, WISPrInfoHandler wisprInfo) throws IOException,
			ParserConfigurationException, FactoryConfigurationError, ApiException {
		
		String res = WISPrConstants.WISPR_RESPONSE_CODE_INTERNAL_ERROR;
		String logOffUrl = null;
		String targetURL = wisprInfo.getLoginURL();
		
		telenetHotspotConnectorApplication.logger("Trying to Log " + targetURL);
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
		nameValuePairs.add(new BasicNameValuePair("UserName", user));  
		nameValuePairs.add(new BasicNameValuePair("Password", password));
		nameValuePairs.add(new BasicNameValuePair("KeepMeSignedIn", "on"));
		nameValuePairs.add(new BasicNameValuePair("Terms", "on"));
		
		String htmlResponse = telenetHotspotConnectorApplication.login(targetURL, nameValuePairs);
		telenetHotspotConnectorApplication.logger("WISPR Reponse:" + htmlResponse);
		
		if (htmlResponse != null) {
			String response = WISPrUtil.getWISPrXML(htmlResponse);
			if (response != null) {
				telenetHotspotConnectorApplication.logger("WISPr response:" + response);
				WISPrResponseHandler wrh = new WISPrResponseHandler();
				try {
					android.util.Xml.parse(response, wrh);
					res = wrh.getResponseCode();
					logOffUrl = wrh.getLogoffURL();
				} catch (SAXException saxe) {
					res = WISPrConstants.WISPR_NOT_PRESENT;
				}
			} else {
				res = WISPrConstants.WISPR_NOT_PRESENT;
			}
		}

		return new LoggerResult(res, logOffUrl);
	}
}