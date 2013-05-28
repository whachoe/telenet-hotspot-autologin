package be.copywaste.telenethotspotconnector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.util.Log;
import android.util.SparseArray;

public class telenetHotspotConnectorApplication extends Application {
    public static final boolean DEV = true;
    public static final String LOGGER_TAG = "hotspotConnector";
    // public static final String hotspotssid = "404 Network not available"; // testing
    public static final String hotspotssid = "TELENETHOTSPOT";
    public static final String homespotssid = "TELENETHOMESPOT";
    public static final String testurl = "http://copywaste.org/status.html";
    
    private static final String USER_AGENT = "Copywaste Telenet Hotspot Autologin";
    private static final int HTTP_STATUS_OK = 200;
	private static byte[] sBuffer = new byte[512];
	
	private static baseConnect connector;
	
	@Override
    public void onCreate()
    {
        super.onCreate();
        logger("Application onCreate");
    }
    
	public static void logger(String message) {
        if (DEV) {
            Log.w(LOGGER_TAG, message);
        }
    }

    public static void logger(Throwable e) {
        if (DEV) {
            if (e != null) {
                String msg = e.getMessage();
                if (msg == null ) {
                    msg = "Exception.Message was Null";
                }
                Log.e(LOGGER_TAG, msg);

                for (StackTraceElement ste : e.getStackTrace()) {
                    Log.e(LOGGER_TAG, ste.getClassName() + " - " + ste.getMethodName() + " [ " + ste.getFileName() + " : " + ste.getLineNumber() + " ]");
                }
            } else {
                Log.e(LOGGER_TAG, "Exception was Null");
            }
        }
    }    

    // This is called by the hotspot-login
    public static String login(String userid, String password, String loginurl) throws ApiException
    {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);  
		nameValuePairs.add(new BasicNameValuePair("userid", userid));  
		nameValuePairs.add(new BasicNameValuePair("password", password));
		nameValuePairs.add(new BasicNameValuePair("c", "std"));
		nameValuePairs.add(new BasicNameValuePair("lang", "nl"));
		nameValuePairs.add(new BasicNameValuePair("checkterms", "1"));
		nameValuePairs.add(new BasicNameValuePair("terms", "on"));

    	return login(loginurl,nameValuePairs);
    }
    
    @SuppressLint("NewApi")
	public static String login(String loginurl, List<NameValuePair> params) throws ApiException 
    {
        if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
        
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		
		// Setting up basic communication 
		HttpParams httpParameters = new BasicHttpParams();
		SingleClientConnManager mgr = new SingleClientConnManager(httpParameters, schemeRegistry);
		
		// Set the timeout in milliseconds until a connection is established.
		int timeoutConnection = 30000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 30000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		// Loading client with timeout-params
		HttpClient client = new DefaultHttpClient(mgr, httpParameters);	
		HttpPost poster = new HttpPost(loginurl);
		poster.setHeader("User-Agent", USER_AGENT);
		
		// Parameters
		try {
			poster.setEntity(new UrlEncodedFormEntity(params));  
			
			HttpResponse response = client.execute(poster);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {
				telenetHotspotConnectorApplication.logger("BAD REQUEST: "+EntityUtils.toString(response.getEntity()));
				throw new ApiException("Invalid response from server: " +
						status.toString());
			}

			// Pull content stream from response
			HttpEntity entity = response.getEntity();
			String contentstring = EntityUtils.toString(entity).trim();
			logger("return from login: "+contentstring);
			return contentstring;
			
		} catch (UnsupportedEncodingException e1) {
			logger(e1);
		} catch (IOException e) {
			logger(e);
			throw new ApiException("Problem communicating with API ("+e.getMessage()+")", e);
		} catch (Exception e) {
        	logger(e);
        	return "";
        }
		
		return null;    	
    }
    
    @SuppressLint("NewApi")
	public static String getSSLPage(String url) throws ApiException
    {
        if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
    	// We're doing SSL here
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		schemeRegistry.register(new Scheme("http",  PlainSocketFactory.getSocketFactory(), 80));
		
		// Setting up basic communication 
		HttpParams httpParameters = new BasicHttpParams();
		SingleClientConnManager mgr = new SingleClientConnManager(httpParameters, schemeRegistry);
    	
        // Set the timeout in milliseconds until a connection is established.
        int timeoutConnection = 30000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        
        // Set the default socket timeout (SO_TIMEOUT) 
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 30000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        // Loading client with timeout-params
        HttpClient client = new DefaultHttpClient(mgr, httpParameters);

        HttpGet getter = new HttpGet(url);
        getter.setHeader("User-Agent", USER_AGENT);

        try {
            HttpResponse response = client.execute(getter);

            // Check if server response is valid
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != HTTP_STATUS_OK) {
            	telenetHotspotConnectorApplication.logger("BAD REQUEST: "+ response.getEntity().toString());
                throw new ApiException("Invalid response from server: " +
                        status.toString());
            }

            // Pull content stream from response
            HttpEntity entity = response.getEntity();
            String contentstring = EntityUtils.toString(entity).trim();
            logger("return from login: "+contentstring);
            return contentstring;
        } catch (IOException e) {
            throw new ApiException("Problem communicating with API ("+e.getMessage()+")", e);
        } catch (Exception e) {
        	logger(e);
        	return "";
        }
    }

    @SuppressLint("NewApi")
	protected static String getPage(String url) throws ApiException
    {
        if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		// Setting up basic communication 
		HttpParams httpParameters = new BasicHttpParams();
    	
        // Set the timeout in milliseconds until a connection is established.
        int timeoutConnection = 2000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        
        // Set the default socket timeout (SO_TIMEOUT) 
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 2000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        // Loading client with timeout-params
        HttpClient client = new DefaultHttpClient(httpParameters);

        HttpGet getter = new HttpGet(url);
        getter.setHeader("User-Agent", USER_AGENT);

        try {
            HttpResponse response = client.execute(getter);

            // Check if server response is valid
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != HTTP_STATUS_OK) {
                throw new ApiException("Invalid response from server: " +
                        status.toString());
            }

            // Pull content stream from response
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();

            ByteArrayOutputStream content = new ByteArrayOutputStream();

            // Read response into a buffered stream
            int readBytes = 0;
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }

            // Return result from buffered stream
            String returnstring = new String(content.toByteArray());
            logger("Login return page: "+returnstring);
            return returnstring;
        } catch (IOException e) {
            throw new ApiException("Problem communicating with API ("+e.getMessage()+")", e);
        } catch (Exception e) {
        	logger(e);
        	return "";
        }
    }

	/**
	 *  We are checking if we can reach the testurl: http://copywaste.org/status.html 
	 * @return boolean Returns true when our testurl is readable
	 */
    public static boolean webIsReachable()
    {
		String httpreturn = "";
		int maxretry   = 3;
		int retrycount = 0;
		
		while (!httpreturn.equals("on") && maxretry > retrycount) {
			try {
				httpreturn = telenetHotspotConnectorApplication.getSSLPage(telenetHotspotConnectorApplication.testurl).trim();
				telenetHotspotConnectorApplication.logger("testurl:"+httpreturn+".");
				retrycount++;
				
				if (httpreturn.equals("on"))
					return true;
				
			} catch (Exception e) {
				telenetHotspotConnectorApplication.logger(e);
				return false;
			}
			
			// Sleep for a while before retrying
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return httpreturn.equals("on");
    }
    
    public static boolean doLogin(String ssid, String userid, String userpw) {
    	logger("doLogin: "+userid+" "+userpw);
    	
    	if (ssid.equals(hotspotssid)) {
    		connector = (hotspotConnect) new hotspotConnect(userid, userpw);
    	}
    	
    	if (ssid.equals(homespotssid)) {
    		connector = (homespotConnect) new homespotConnect(userid, userpw);
    	}
    	
    	if (connector instanceof baseConnect)
    		return connector.doLogin();
    	
		return false;
    }
    
    public static void doLogout(String ssid) {
    	if (ssid.equals(hotspotssid)) {
    		connector = (hotspotConnect) new hotspotConnect();
    	}
    	
    	if (ssid.equals(homespotssid)) {
    		connector = (homespotConnect) new homespotConnect();
    	}
    	
    	if (connector instanceof baseConnect)
    		connector.doLogout();
    	
    	return;
    }
    
    public static SparseArray<String> getCredentialsForSSID(String ssid, SharedPreferences prefs) {
    	if (prefs == null) {
    		logger("Preferences are null!!!");
    		return null;
    	}
    	
    	if (ssid.equals(hotspotssid)) {
    		SparseArray<String> result = new SparseArray<String>(2);
    		result.append(0, prefs.getString("userid", ""));
    		result.append(1, prefs.getString("userpw", ""));
    		return result;
    	}
    	
    	if (ssid.equals(homespotssid)) {
    		SparseArray<String> result = new SparseArray<String>(2);
    		if (prefs.getString("userid_homespot", "") == "" )
    			result.append(0, prefs.getString("userid", ""));
    		else	
    			result.append(0, prefs.getString("userid_homespot", ""));
    		
    		if (prefs.getString("userpw_homespot", "") == "" )
    			result.append(1, prefs.getString("userpw", ""));
    		else
    			result.append(1, prefs.getString("userpw_homespot", ""));
    		
    		return result;
    	}
    	
    	return null;
    }
    
	/**
	 * Thrown when there were problems contacting the remote API server, either
	 * because of a network error, or the server returned a bad status code.
	 */
	@SuppressWarnings("serial")
	public static class ApiException extends Exception 
	{
		public ApiException(String detailMessage, Throwable throwable) 
		{
			super(detailMessage, throwable);
		}

		public ApiException(String detailMessage) 
		{
			super(detailMessage);
		}
	}
}
