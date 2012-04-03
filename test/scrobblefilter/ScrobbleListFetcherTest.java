package scrobblefilter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import scrobblefilter.net.ScrobbleListFetcher;
import scrobblefilter.net.impl.FakeScrobbleListFetcher;
import scrobblefilter.net.impl.NetworkedScrobbleListFetcher;

public class ScrobbleListFetcherTest {

//	@Test
	public void test() throws JsonParseException, JsonMappingException, IOException {
		
		ScrobbleListFetcher fetcher = new FakeScrobbleListFetcher();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readValue(fetcher.fetchList("pdunham"), JsonNode.class);
		JsonNode topartistNode = rootNode.get("topartists");
		JsonNode artistNode = topartistNode.get("artist");
		Iterator<JsonNode> artists = artistNode.getElements();
		while (artists.hasNext()) {
			
			JsonNode artist = artists.next();
			Iterator<String> fieldNames = artist.getFieldNames();
			assertEquals("name",fieldNames.next());
			System.out.println( artist.get("name") + ": "+artist.get("playcount").asInt() + " plays");
			
		}
	}
	
	@Test
	public void testNetworkScrobbleFetcher() throws JsonParseException, JsonMappingException, IOException {
		ScrobbleListFetcher fetcher = new NetworkedScrobbleListFetcher();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readValue(fetcher.fetchList("pdunham"), JsonNode.class)	;
		JsonNode topartistNode = rootNode.get("topartists");
		JsonNode artistNode = topartistNode.get("artist");
		Iterator<JsonNode> artists = artistNode.getElements();
		while (artists.hasNext()) {
			
			JsonNode artist = artists.next();
			Iterator<String> fieldNames = artist.getFieldNames();
			assertEquals("name",fieldNames.next());
			System.out.println( artist.get("name") + ": "+artist.get("playcount").asInt() + " plays");
			
		}
		
	}

}
