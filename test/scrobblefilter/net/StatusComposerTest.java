package scrobblefilter.net;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import scrobblefilter.model.ScrobbledArtist;
import scrobblefilter.net.impl.FakeScrobbleListFetcher;

/**
 * Locks the platform-agnostic status text (formerly ScrobbleTweeter.constructTweet)
 * after extracting it into StatusComposer. Uses the canned FakeScrobbleListFetcher
 * top-artists list (Phish, Grateful Dead, Miles Davis & Gil Evans, MC5, ...).
 */
public class StatusComposerTest {

	private final StatusComposer composer = new StatusComposer(new FakeScrobbleListFetcher());

	@Test
	public void buildsTopThreeStatus() {
		List<ScrobbledArtist> artists = composer.extractFilteredList("anyuser", Collections.emptyList());
		assertEquals(
				"I've been listening to Phish, Grateful Dead, and Miles Davis & Gil Evans.",
				composer.constructStatus(artists));
	}

	@Test
	public void filteredArtistsAreRemovedBeforeTopThree() {
		// Filtering out the top two should promote the next artists into the slots.
		List<ScrobbledArtist> artists =
				composer.extractFilteredList("anyuser", Arrays.asList("Phish", "Grateful Dead"));
		assertEquals(
				"I've been listening to Miles Davis & Gil Evans, MC5, and Trey Anastasio.",
				composer.constructStatus(artists));
	}
}
