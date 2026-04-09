package scrobblefilter;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import scrobblefilter.net.ScrobbleListFetcher;
import scrobblefilter.net.impl.FakeScrobbleListFetcher;
import scrobblefilter.net.impl.NetworkedScrobbleListFetcher;

public class ScrobbleListFetcherTest {

	@Before
	public void setUp() throws IOException {
		try (InputStream in = new FileInputStream("war/WEB-INF/scrobblefilter.properties")) {
			AppConfig.load(in);
		}
	}

	@Test
	public void testFakeScrobbleFetcher() throws JsonParseException, JsonMappingException, IOException {
		assertArtistListStructure(new FakeScrobbleListFetcher());
	}

	@Test
	public void testNetworkScrobbleFetcher() throws JsonParseException, JsonMappingException, IOException {
		assertArtistListStructure(new NetworkedScrobbleListFetcher());
	}

	private void assertArtistListStructure(ScrobbleListFetcher fetcher)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readValue(fetcher.fetchList("pdunham"), JsonNode.class);
		JsonNode artistNode = rootNode.get("topartists").get("artist");
		Iterator<JsonNode> artists = artistNode.getElements();
		while (artists.hasNext()) {
			JsonNode artist = artists.next();
			assertNotNull("Artist missing 'name' field", artist.get("name"));
			assertNotNull("Artist missing 'playcount' field", artist.get("playcount"));
			System.out.println(artist.get("name") + ": " + artist.get("playcount").asInt() + " plays");
		}
	}

}
