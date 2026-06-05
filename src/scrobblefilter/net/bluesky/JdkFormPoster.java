package scrobblefilter.net.bluesky;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Production {@link FormPoster} over the JDK {@link HttpClient}. */
public class JdkFormPoster implements FormPoster {

	private final HttpClient client = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	@Override
	public HttpExchange post(String url, Map<String, String> headers, Map<String, String> formParams)
			throws IOException {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(encode(formParams)));
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

	private static String encode(Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e : params.entrySet()) {
			if (sb.length() > 0) sb.append('&');
			sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
					.append('=')
					.append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
		}
		return sb.toString();
	}
}
