package scrobblefilter.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
}
