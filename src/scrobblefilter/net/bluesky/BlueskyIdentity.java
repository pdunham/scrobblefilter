package scrobblefilter.net.bluesky;

/** A resolved Bluesky account: its DID, the handle it was resolved from, and its PDS host. */
public class BlueskyIdentity {

	private final String did;
	private final String handle;
	private final String pdsUrl;

	public BlueskyIdentity(String did, String handle, String pdsUrl) {
		this.did = did;
		this.handle = handle;
		this.pdsUrl = pdsUrl;
	}

	public String getDid() {
		return did;
	}

	public String getHandle() {
		return handle;
	}

	public String getPdsUrl() {
		return pdsUrl;
	}
}
