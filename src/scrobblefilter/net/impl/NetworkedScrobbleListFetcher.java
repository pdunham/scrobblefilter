package scrobblefilter.net.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import scrobblefilter.net.ScrobbleListFetcher;

public class NetworkedScrobbleListFetcher implements ScrobbleListFetcher {

	private static final String LAST_FM_API_KEY = "c0c210a13a2d568ed460f60479b79092";
	private static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
	private static final String METHOD_PARM = "method=user.gettopartists";
	private static final String PERIOD_PARM = "period=7day";
	private static final String FORMAT_PARM = "format=json";
	
	@Override
	public String fetchList(String userName) {
		String jsonList = new String();
		try {
			URL url = new URL(constructUrl(userName));
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        String line;
	        int i = 0;
	            while ((line = reader.readLine()) != null) {
	                jsonList = jsonList + line;
	            }
	            reader.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return  jsonList;
	}

	private String constructUrl(String userName) {
		return BASE_URL+METHOD_PARM+"&"+PERIOD_PARM+"&"+FORMAT_PARM+"&api_key="+ LAST_FM_API_KEY +"&user="+userName;
	}

}
