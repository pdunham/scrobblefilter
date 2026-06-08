package scrobblefilter.net.bluesky;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Production {@link JsonPoster} over the JDK {@link HttpClient}. */
public class JdkJsonPoster implements JsonPoster {

	private final HttpClient client = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	@Override
	public HttpExchange post(String url, Map<String, String> headers, String jsonBody) throws IOException {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
		if (headers != null) {
			headers.forEach(builder::header);
		}
		try {
			HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
			Map<String, String> flat = new HashMap<>();
			response.headers().map().forEach((k, v) -> {
				if (!v.isEmpty()) flat.put(k.toLowerCase(Locale.ROOT), v.get(0));
			});
			return new HttpExchange(response.statusCode(), response.body(), flat);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("interrupted during POST " + url, e);
		}
	}
}
