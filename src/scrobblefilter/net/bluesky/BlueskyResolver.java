package scrobblefilter.net.bluesky;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Resolves a Bluesky handle to where its OAuth actually happens:
 *
 * <pre>
 *   handle --resolveHandle--> DID --DID doc--> PDS --oauth-protected-resource-->
 *   authorization server --oauth-authorization-server--> {PAR, authorize, token}
 * </pre>
 *
 * The two entry points (handle resolver, PLC directory) default to the public
 * Bluesky services and are overridable via {@code BLUESKY_HANDLE_RESOLVER_URL} /
 * {@code BLUESKY_PLC_DIRECTORY_URL} (mirroring {@code LASTFM_BASE_URL}) so tests
 * can point at a mock. The PDS and authorization-server URLs are data-driven —
 * whatever resolution returns — so a mock that controls those responses needs no
 * further configuration.
 */
public class BlueskyResolver {

	private static final String DEFAULT_HANDLE_RESOLVER = "https://bsky.social";
	private static final String DEFAULT_PLC_DIRECTORY = "https://plc.directory";
	private static final String PDS_SERVICE_TYPE = "AtprotoPersonalDataServer";

	private final HttpGetter http;
	private final String handleResolverBase;
	private final String plcDirectoryBase;
	private final ObjectMapper mapper = new ObjectMapper();

	public BlueskyResolver() {
		this(new JdkHttpGetter(),
				envOr("BLUESKY_HANDLE_RESOLVER_URL", DEFAULT_HANDLE_RESOLVER),
				envOr("BLUESKY_PLC_DIRECTORY_URL", DEFAULT_PLC_DIRECTORY));
	}

	/** Test seam: inject the HTTP getter and entry-point base URLs. */
	public BlueskyResolver(HttpGetter http, String handleResolverBase, String plcDirectoryBase) {
		this.http = http;
		this.handleResolverBase = stripTrailingSlash(handleResolverBase);
		this.plcDirectoryBase = stripTrailingSlash(plcDirectoryBase);
	}

	/** Resolve a handle through the full chain to identity + authorization-server metadata. */
	public ResolvedAccount resolve(String handle) throws IOException {
		String did = resolveHandleToDid(handle);
		String pds = resolveDidToPds(did);
		AuthServerMetadata authServer = resolveAuthServer(pds);
		return new ResolvedAccount(new BlueskyIdentity(did, handle, pds), authServer);
	}

	public String resolveHandleToDid(String handle) throws IOException {
		String url = handleResolverBase + "/xrpc/com.atproto.identity.resolveHandle?handle="
				+ URLEncoder.encode(handle, StandardCharsets.UTF_8);
		String did = text(parse(http.get(url)), "did");
		if (did == null) {
			throw new IOException("resolveHandle returned no did for handle " + handle);
		}
		return did;
	}

	public String resolveDidToPds(String did) throws IOException {
		String didDocJson;
		if (did.startsWith("did:plc:")) {
			didDocJson = http.get(plcDirectoryBase + "/" + did);
		} else if (did.startsWith("did:web:")) {
			// did:web:host(:path...) — colons after the host map to path segments.
			String host = did.substring("did:web:".length()).replace(":", "/");
			didDocJson = http.get("https://" + host + "/.well-known/did.json");
		} else {
			throw new IOException("unsupported DID method: " + did);
		}

		JsonNode services = parse(didDocJson).get("service");
		if (services != null && services.isArray()) {
			Iterator<JsonNode> it = services.getElements();
			while (it.hasNext()) {
				JsonNode svc = it.next();
				if (PDS_SERVICE_TYPE.equals(text(svc, "type"))) {
					String endpoint = text(svc, "serviceEndpoint");
					if (endpoint != null) {
						return stripTrailingSlash(endpoint);
					}
				}
			}
		}
		throw new IOException("no " + PDS_SERVICE_TYPE + " service endpoint in DID document for " + did);
	}

	public AuthServerMetadata resolveAuthServer(String pdsUrl) throws IOException {
		JsonNode prm = parse(http.get(stripTrailingSlash(pdsUrl) + "/.well-known/oauth-protected-resource"));
		JsonNode servers = prm.get("authorization_servers");
		if (servers == null || !servers.isArray() || servers.size() == 0) {
			throw new IOException("PDS " + pdsUrl + " advertises no authorization_servers");
		}
		String issuer = stripTrailingSlash(servers.get(0).asText());

		JsonNode meta = parse(http.get(issuer + "/.well-known/oauth-authorization-server"));
		String par = text(meta, "pushed_authorization_request_endpoint");
		String authorize = text(meta, "authorization_endpoint");
		String token = text(meta, "token_endpoint");
		if (par == null || authorize == null || token == null) {
			throw new IOException("authorization server " + issuer
					+ " metadata missing PAR/authorize/token endpoint");
		}
		String declaredIssuer = text(meta, "issuer");
		return new AuthServerMetadata(declaredIssuer != null ? declaredIssuer : issuer, par, authorize, token);
	}

	private JsonNode parse(String json) throws IOException {
		return mapper.readValue(json, JsonNode.class);
	}

	private static String text(JsonNode node, String field) {
		if (node == null) return null;
		JsonNode v = node.get(field);
		if (v == null || v.isNull()) return null;
		String s = v.asText();
		return (s == null || s.isEmpty()) ? null : s;
	}

	private static String stripTrailingSlash(String s) {
		return (s != null && s.endsWith("/")) ? s.substring(0, s.length() - 1) : s;
	}

	private static String envOr(String key, String def) {
		String v = System.getenv(key);
		return (v == null || v.isEmpty()) ? def : v;
	}
}
