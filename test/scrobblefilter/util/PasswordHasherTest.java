package scrobblefilter.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class PasswordHasherTest {

	private final PasswordHasher hasher = new PasswordHasher("test-pepper");

	@Test
	public void hashThenVerifyRoundTrips() {
		String stored = hasher.hash("correct horse battery staple");
		assertTrue(hasher.verify("correct horse battery staple", stored));
	}

	@Test
	public void wrongPasswordIsRejected() {
		String stored = hasher.hash("s3cret");
		assertFalse(hasher.verify("s3cr3t", stored));
		assertFalse(hasher.verify("", stored));
	}

	@Test
	public void samePasswordHashesDifferently() {
		// Random per-user salt -> distinct stored values, both verify.
		String a = hasher.hash("hunter2");
		String b = hasher.hash("hunter2");
		assertNotEquals(a, b);
		assertTrue(hasher.verify("hunter2", a));
		assertTrue(hasher.verify("hunter2", b));
	}

	@Test
	public void pepperMatters() {
		// A hash made under one pepper must not verify under another.
		String stored = new PasswordHasher("pepper-A").hash("pw");
		assertFalse(new PasswordHasher("pepper-B").verify("pw", stored));
		assertTrue(new PasswordHasher("pepper-A").verify("pw", stored));
	}

	@Test
	public void storedFormatIsPbkdf2WithFourParts() {
		String stored = hasher.hash("pw");
		String[] parts = stored.split("\\$");
		org.junit.Assert.assertEquals(4, parts.length);
		org.junit.Assert.assertEquals("pbkdf2", parts[0]);
		org.junit.Assert.assertEquals("210000", parts[1]);
	}

	@Test
	public void malformedStoredValueIsRejected() {
		assertFalse(hasher.verify("pw", "not-a-hash"));
		assertFalse(hasher.verify("pw", "pbkdf2$abc$xx$yy"));
		assertFalse(hasher.verify("pw", null));
	}

	@Test
	public void emptyPasswordCannotBeHashed() {
		try {
			hasher.hash("");
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) { /* ok */ }
	}
}
