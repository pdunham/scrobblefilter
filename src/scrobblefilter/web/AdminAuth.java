package scrobblefilter.web;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class AdminAuth {

	public static boolean valid(HttpServletRequest req, String envVar, String headerName) {
		String expected = System.getenv(envVar);
		if (expected == null || expected.isEmpty()) return false;
		String got = req.getHeader(headerName);
		if (got == null) return false;
		byte[] a = expected.getBytes(StandardCharsets.UTF_8);
		byte[] b = got.getBytes(StandardCharsets.UTF_8);
		return MessageDigest.isEqual(a, b);
	}
}
