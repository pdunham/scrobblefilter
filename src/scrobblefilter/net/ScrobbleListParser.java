package scrobblefilter.net;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import scrobblefilter.model.ScrobbledArtist;

public class ScrobbleListParser {

	
	public static List<ScrobbledArtist> parseList(String rawJson) {
		
		ArrayList<ScrobbledArtist> artists = new ArrayList<ScrobbledArtist>();
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readValue(rawJson, JsonNode.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonNode topartistNode = rootNode.get("topartists");
		JsonNode artistArray = topartistNode.get("artist");
		Iterator<JsonNode> artistNodes = artistArray.getElements();
		while (artistNodes.hasNext()) {
			
			JsonNode artistNode = artistNodes.next();
			artists.add(new ScrobbledArtist(artistNode.get("name").asText(), artistNode.get("playcount").asInt()));
			
		}

		
		return artists;
		
	}

	
}
