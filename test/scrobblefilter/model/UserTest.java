package scrobblefilter.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;

import org.junit.Test;

public class UserTest {

	private static final Key KEY = Key.newBuilder("test-project", "User", "somelastfmname").build();

	private static Entity.Builder userEntity() {
		return Entity.newBuilder(KEY);
	}

	@Test
	public void emptyTokenAndSecretCoerceToNull() {
		// toEntity() persists missing OAuth tokens as "" (never null), so the
		// load path must treat empty the same as absent — otherwise the cron
		// guard (token == null) lets a token-less user through to a doomed
		// Twitter call.
		Entity e = userEntity()
			.set("token", "")
			.set("tokenSecret", "")
			.build();

		User u = User.fromEntity(e);

		assertNull("empty token should load as null", u.getToken());
		assertNull("empty tokenSecret should load as null", u.getTokenSecret());
	}

	@Test
	public void absentTokenAndSecretAreNull() {
		User u = User.fromEntity(userEntity().build());

		assertNull(u.getToken());
		assertNull(u.getTokenSecret());
	}

	@Test
	public void presentTokenAndSecretArePreserved() {
		Entity e = userEntity()
			.set("token", "abc123")
			.set("tokenSecret", "secret456")
			.build();

		User u = User.fromEntity(e);

		assertEquals("abc123", u.getToken());
		assertEquals("secret456", u.getTokenSecret());
	}

	@Test
	public void emptyBlueskyCredentialsCoerceToNull() {
		Entity e = userEntity()
			.set("blueskyDid", "")
			.set("blueskyHandle", "")
			.set("blueskyRefreshTokenEnc", "")
			.set("blueskyDpopKeyEnc", "")
			.build();

		User u = User.fromEntity(e);

		assertNull(u.getBlueskyDid());
		assertNull(u.getBlueskyHandle());
		assertNull(u.getBlueskyRefreshTokenEnc());
		assertNull(u.getBlueskyDpopKeyEnc());
		assertFalse(u.isBlueskyCron());
	}

	@Test
	public void absentBlueskyFieldsAreNullAndCronFalse() {
		User u = User.fromEntity(userEntity().build());

		assertNull(u.getBlueskyDid());
		assertNull(u.getBlueskyHandle());
		assertNull(u.getBlueskyRefreshTokenEnc());
		assertNull(u.getBlueskyDpopKeyEnc());
		assertFalse(u.isBlueskyCron());
	}

	@Test
	public void presentBlueskyFieldsArePreserved() {
		Entity e = userEntity()
			.set("blueskyDid", "did:plc:abc123")
			.set("blueskyHandle", "alice.bsky.social")
			.set("blueskyRefreshTokenEnc", "enc-refresh-blob")
			.set("blueskyDpopKeyEnc", "enc-dpop-blob")
			.set("blueskyCron", true)
			.build();

		User u = User.fromEntity(e);

		assertEquals("did:plc:abc123", u.getBlueskyDid());
		assertEquals("alice.bsky.social", u.getBlueskyHandle());
		assertEquals("enc-refresh-blob", u.getBlueskyRefreshTokenEnc());
		assertEquals("enc-dpop-blob", u.getBlueskyDpopKeyEnc());
		assertTrue(u.isBlueskyCron());
	}

}
