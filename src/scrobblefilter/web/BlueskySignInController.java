package scrobblefilter.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nimbusds.jose.jwk.ECKey;

import scrobblefilter.model.User;
import scrobblefilter.net.bluesky.AuthServerMetadata;
import scrobblefilter.net.bluesky.BlueskyOAuthClient;
import scrobblefilter.net.bluesky.BlueskyResolver;
import scrobblefilter.net.bluesky.DpopKeys;
import scrobblefilter.net.bluesky.DpopProofFactory;
import scrobblefilter.net.bluesky.FormPoster;
import scrobblefilter.net.bluesky.Pkce;
import scrobblefilter.net.bluesky.ResolvedAccount;
import scrobblefilter.net.bluesky.TokenSet;
import scrobblefilter.util.CredentialCryptoProvider;

/**
 * Connects a user's Bluesky account via AT Protocol OAuth.
 *
 * <p>{@code /hello/bluesky/signin}: resolve the handle, mint a per-account DPoP
 * key + PKCE + state, push the authorization request, and redirect the browser
 * to the authorization server. {@code /hello/bluesky/callback}: exchange the code
 * for tokens and persist the encrypted DPoP key + refresh token (and DID/handle)
 * on the {@link User}. Transient OAuth state lives in the HTTP session between the
 * two legs, mirroring {@link TwitterSignInController}.
 */
@Controller
public class BlueskySignInController {

	private static final Logger log = Logger.getLogger(BlueskySignInController.class.getName());
	private static final SecureRandom RANDOM = new SecureRandom();

	@Autowired private BlueskyResolver resolver;
	@Autowired private FormPoster formPoster;
	@Autowired private DpopProofFactory dpopProofFactory;
	@Autowired private CredentialCryptoProvider crypto;

	@RequestMapping(value = "bluesky/signin", method = GET)
	public void signin(HttpServletRequest req, HttpServletResponse res) throws IOException {
		User user = (User) req.getSession().getAttribute("user");
		if (user == null) {
			res.sendRedirect("/hello/welcome");
			return;
		}
		String handleParam = req.getParameter("handle");
		if (handleParam == null || handleParam.trim().isEmpty()) {
			req.getSession().setAttribute("error", "Enter your Bluesky handle to connect.");
			res.sendRedirect("/hello/world");
			return;
		}
		String handle = handleParam.trim();
		try {
			ResolvedAccount account = resolver.resolve(handle);
			ECKey dpopKey = DpopKeys.generate();
			Pkce pkce = Pkce.generate();
			String state = randomToken();

			BlueskyOAuthClient client = newClient(req);
			String requestUri = client.pushAuthorizationRequest(
					account.getAuthServer(), dpopKey, pkce, state, handle);

			HttpSession session = req.getSession();
			session.setAttribute("bsky_dpop_jwk", DpopKeys.toJson(dpopKey));
			session.setAttribute("bsky_pkce_verifier", pkce.verifier());
			session.setAttribute("bsky_state", state);
			session.setAttribute("bsky_handle", handle);
			session.setAttribute("bsky_did", account.getIdentity().getDid());
			session.setAttribute("bsky_issuer", account.getAuthServer().getIssuer());
			session.setAttribute("bsky_token_endpoint", account.getAuthServer().getTokenEndpoint());

			String redirect = account.getAuthServer().getAuthorizationEndpoint()
					+ "?client_id=" + enc(BlueskyUrls.clientId(req))
					+ "&request_uri=" + enc(requestUri);
			res.sendRedirect(redirect);
		} catch (Exception e) {
			log.warning("bluesky signin failed for " + handle + ": " + e.getMessage());
			req.getSession().setAttribute("error", "Could not start Bluesky sign-in: " + e.getMessage());
			res.sendRedirect("/hello/world");
		}
	}

	@RequestMapping(value = "bluesky/callback", method = GET)
	public void callback(HttpServletRequest req, HttpServletResponse res) throws IOException {
		HttpSession session = req.getSession();
		User user = (User) session.getAttribute("user");
		if (user == null) {
			res.sendRedirect("/hello/welcome");
			return;
		}

		String code = req.getParameter("code");
		String state = req.getParameter("state");
		String expectedState = (String) session.getAttribute("bsky_state");
		if (code == null || state == null || expectedState == null || !expectedState.equals(state)) {
			session.setAttribute("error", "Bluesky sign-in failed (invalid state).");
			res.sendRedirect("/hello/world");
			return;
		}

		try {
			ECKey dpopKey = DpopKeys.fromJson((String) session.getAttribute("bsky_dpop_jwk"));
			String verifier = (String) session.getAttribute("bsky_pkce_verifier");
			String handle = (String) session.getAttribute("bsky_handle");
			AuthServerMetadata as = new AuthServerMetadata(
					(String) session.getAttribute("bsky_issuer"), null, null,
					(String) session.getAttribute("bsky_token_endpoint"));

			TokenSet tokens = newClient(req).exchangeCode(as, dpopKey, code, verifier);

			User fresh = RegistrationController.findUser(user.getLastfmName());
			if (fresh == null) fresh = user;
			String did = tokens.getDid() != null ? tokens.getDid() : (String) session.getAttribute("bsky_did");
			fresh.setBlueskyDid(did);
			fresh.setBlueskyHandle(handle);
			fresh.setBlueskyDpopKeyEnc(crypto.get().encrypt(DpopKeys.toJson(dpopKey)));
			if (tokens.getRefreshToken() != null) {
				fresh.setBlueskyRefreshTokenEnc(crypto.get().encrypt(tokens.getRefreshToken()));
			}
			fresh.save();
			session.setAttribute("user", fresh);

			clear(session, "bsky_dpop_jwk", "bsky_pkce_verifier", "bsky_state",
					"bsky_issuer", "bsky_token_endpoint", "bsky_did");
			res.sendRedirect("/hello/world");
		} catch (Exception e) {
			log.warning("bluesky callback failed: " + e.getMessage());
			session.setAttribute("error", "Bluesky sign-in failed: " + e.getMessage());
			res.sendRedirect("/hello/world");
		}
	}

	private BlueskyOAuthClient newClient(HttpServletRequest req) {
		return new BlueskyOAuthClient(formPoster, dpopProofFactory,
				BlueskyUrls.clientId(req), BlueskyUrls.redirectUri(req));
	}

	private static String randomToken() {
		byte[] b = new byte[24];
		RANDOM.nextBytes(b);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
	}

	private static String enc(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}

	private static void clear(HttpSession session, String... keys) {
		for (String k : keys) session.removeAttribute(k);
	}
}
