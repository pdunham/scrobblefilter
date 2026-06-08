package scrobblefilter.web;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BlueskyUrlsTest {

	@Test
	public void originOfDropsPath() {
		assertEquals("https://host.example",
				BlueskyUrls.originOf("https://host.example/hello/client-metadata.json"));
		assertEquals("https://scrobblefilter-x-uc.a.run.app",
				BlueskyUrls.originOf("https://scrobblefilter-x-uc.a.run.app/hello/client-metadata.json"));
		assertEquals("http://localhost:8080",
				BlueskyUrls.originOf("http://localhost:8080/hello/bluesky/callback"));
	}

	@Test
	public void originOfHandlesBareOrigin() {
		assertEquals("https://host.example", BlueskyUrls.originOf("https://host.example"));
	}
}
