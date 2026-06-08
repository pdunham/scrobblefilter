package scrobblefilter.net.bluesky;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.nimbusds.jose.jwk.ECKey;

import scrobblefilter.model.User;
import scrobblefilter.net.SocialPoster;
import scrobblefilter.net.SocialPostException;
import scrobblefilter.util.CredentialCrypto;
import scrobblefilter.util.CredentialCryptoProvider;

/**
 * {@link SocialPoster} for Bluesky (AT Protocol). At post time it decrypts the
 * stored DPoP key + refresh token, re-resolves the account (handle → PDS +
 * authorization server), refreshes for a DPoP-bound access token, and creates an
 * {@code app.bsky.feed.post} record via {@code com.atproto.repo.createRecord}.
 *
 * <p>Refresh tokens rotate, so a new one is re-encrypted and persisted. The
 * {@code client_id} for refresh comes from {@code BLUESKY_CLIENT_ID} (the hosted
 * client-metadata URL); it must match what the connect flow used.
 */
public class BlueskyPoster implements SocialPoster {

	private static final String COLLECTION = "app.bsky.feed.post";

	private final BlueskyResolver resolver;
	private final FormPoster formPoster;
	private final JsonPoster jsonPoster;
	private final DpopProofFactory proofFactory;
	private final CredentialCryptoProvider cryptoProvider;
	private final ObjectMapper mapper = new ObjectMapper();

	public BlueskyPoster(BlueskyResolver resolver, FormPoster formPoster, JsonPoster jsonPoster,
			DpopProofFactory proofFactory, CredentialCryptoProvider cryptoProvider) {
		this.resolver = resolver;
		this.formPoster = formPoster;
		this.jsonPoster = jsonPoster;
		this.proofFactory = proofFactory;
		this.cryptoProvider = cryptoProvider;
	}

	@Override
	public String platform() {
		return "bluesky";
	}

	@Override
	public boolean isConnected(User user) {
		return notEmpty(user.getBlueskyHandle()) && notEmpty(user.getBlueskyDid())
				&& notEmpty(user.getBlueskyRefreshTokenEnc()) && notEmpty(user.getBlueskyDpopKeyEnc());
	}

	@Override
	public boolean isEnabledFor(User user) {
		return isConnected(user) && user.isBlueskyCron();
	}

	@Override
	public void post(User user, String statusText) throws SocialPostException {
		try {
			CredentialCrypto crypto = cryptoProvider.get();
			ECKey dpopKey = DpopKeys.fromJson(crypto.decrypt(user.getBlueskyDpopKeyEnc()));
			String refreshToken = crypto.decrypt(user.getBlueskyRefreshTokenEnc());

			ResolvedAccount account = resolver.resolve(user.getBlueskyHandle());

			BlueskyOAuthClient oauth = new BlueskyOAuthClient(formPoster, proofFactory, clientId(), null);
			TokenSet tokens = oauth.refresh(account.getAuthServer(), dpopKey, refreshToken);

			// Refresh tokens are single-use; persist the rotated one or the next run fails.
			if (tokens.getRefreshToken() != null && !tokens.getRefreshToken().equals(refreshToken)) {
				user.setBlueskyRefreshTokenEnc(crypto.encrypt(tokens.getRefreshToken()));
				persist(user);
			}

			createRecord(account.getIdentity().getPdsUrl(), dpopKey, tokens.getAccessToken(),
					user.getBlueskyDid(), statusText);
		} catch (SocialPostException e) {
			throw e;
		} catch (Exception e) {
			throw new SocialPostException("bluesky post failed: " + e.getMessage(), e);
		}
	}

	private void createRecord(String pdsUrl, ECKey dpopKey, String accessToken, String did, String text)
			throws Exception {
		String url = stripTrailingSlash(pdsUrl) + "/xrpc/com.atproto.repo.createRecord";
		String body = recordJson(did, text);

		HttpExchange resp = postRecord(url, dpopKey, accessToken, body, null);
		String nonce = resp.header("dpop-nonce");
		if (!resp.isSuccess() && resp.status() >= 400 && resp.status() < 500 && nonce != null) {
			resp = postRecord(url, dpopKey, accessToken, body, nonce);
		}
		if (!resp.isSuccess()) {
			throw new SocialPostException("createRecord failed (" + resp.status() + "): " + resp.body());
		}
	}

	private HttpExchange postRecord(String url, ECKey dpopKey, String accessToken, String body, String nonce)
			throws IOException {
		// Resource request: DPoP proof binds the access token via ath; the access
		// token itself rides in the Authorization: DPoP <token> header.
		String proof = proofFactory.createProof(dpopKey, "POST", url, nonce, accessToken);
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("Authorization", "DPoP " + accessToken);
		headers.put("DPoP", proof);
		return jsonPoster.post(url, headers, body);
	}

	private String recordJson(String did, String text) throws IOException {
		Map<String, Object> record = new LinkedHashMap<>();
		record.put("$type", COLLECTION);
		record.put("text", text);
		record.put("createdAt", Instant.now().toString());
		Map<String, Object> req = new LinkedHashMap<>();
		req.put("repo", did);
		req.put("collection", COLLECTION);
		req.put("record", record);
		return mapper.writeValueAsString(req);
	}

	/** Test seam: how a rotated refresh token is persisted. */
	protected void persist(User user) {
		user.save();
	}

	private static String clientId() {
		String v = System.getenv("BLUESKY_CLIENT_ID");
		return v != null ? v : "";
	}

	private static boolean notEmpty(String s) {
		return s != null && !s.isEmpty();
	}

	private static String stripTrailingSlash(String s) {
		return (s != null && s.endsWith("/")) ? s.substring(0, s.length() - 1) : s;
	}
}
