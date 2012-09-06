package scrobblefilter.net.impl;

import scrobblefilter.net.ScrobbleListFetcher;

public class FakeScrobbleListFetcher implements ScrobbleListFetcher {

	@Override
	public String fetchList(String userName) {
		return "{\"topartists\":{\"artist\":[{\"name\":\"Phish\",\"playcount\":\"50\",\"mbid\":\"e01646f2-2a04-450d-8bf2-0d993082e058\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Phish\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/30654597.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/30654597.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/30654597.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/30654597.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/30654597\\/Phish+_2.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"1\"}},{\"name\":\"Grateful Dead\",\"playcount\":\"44\",\"mbid\":\"6faa7ca7-0d99-4a5e-bfa6-1fd5037520c6\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Grateful+Dead\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/58911273.png\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/58911273.png\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/58911273.png\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/58911273.png\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/58911273\\/Grateful+Dead+DeadPNG.png\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"2\"}},{\"name\":\"Miles Davis & Gil Evans\",\"playcount\":\"39\",\"mbid\":\"\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Miles%2BDavis%2B%2526%2BGil%2BEvans\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/60645075.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/60645075.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/60645075.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/60645075.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/60645075\\/Miles+Davis++Gil+Evans+cSonyMusicEntertainement7.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"3\"}},{\"name\":\"MC5\",\"playcount\":\"32\",\"mbid\":\"08b736bb-1c82-40b4-8b0b-49e2182a067a\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/MC5\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/63029671.png\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/63029671.png\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/63029671.png\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/63029671.png\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/63029671\\/MC5+001024.png\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"4\"}},{\"name\":\"Trey Anastasio\",\"playcount\":\"28\",\"mbid\":\"d942f71b-09d3-406c-8f7d-c52eba3135c1\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Trey+Anastasio\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/514143.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/514143.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/514143.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/514143.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/514143\\/Trey+Anastasio.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"5\"}},{\"name\":\"Miles Davis\",\"playcount\":\"26\",\"mbid\":\"561d854a-6a28-4aa7-8c99-323e6ce46c2a\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Miles+Davis\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/20860587.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/20860587.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/20860587.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/20860587.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/20860587\\/Miles+Davis.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"6\"}},{\"name\":\"George Jones\",\"playcount\":\"21\",\"mbid\":\"f2903be0-79a7-4334-8cc5-e45309482a97\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/George+Jones\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/35613247.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/35613247.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/35613247.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/35613247.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/35613247\\/George+Jones+George+and+guitar.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"7\"}},{\"name\":\"Sonny Rollins\",\"playcount\":\"17\",\"mbid\":\"3b47247e-5b57-49b6-a0ed-bad80243802a\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Sonny+Rollins\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/5636129.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/5636129.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/5636129.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/5636129.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/5636129\\/Sonny+Rollins+sonnyrollins2.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"8\"}},{\"name\":\"Bob Dylan\",\"playcount\":\"12\",\"mbid\":\"72c536dc-7137-4477-a521-567eeb840fa8\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Bob+Dylan\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/6690423.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/6690423.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/6690423.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/6690423.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/6690423\\/Bob+Dylan+DylanByBarryFeinstein.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"9\"}},{\"name\":\"Cornershop\",\"playcount\":\"12\",\"mbid\":\"92046be7-0927-4835-a4ed-a90416747d53\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Cornershop\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/58937881.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/58937881.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/58937881.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/58937881.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/58937881\\/Cornershop+presspic.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"10\"}},{\"name\":\"moe.\",\"playcount\":\"11\",\"mbid\":\"5fab339d-5dd4-42b0-8d70-496a4493ed59\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/moe.\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/353625.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/353625.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/353625.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/353625.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/353625\\/moe.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"11\"}},{\"name\":\"Dr. Dog\",\"playcount\":\"10\",\"mbid\":\"e9aed5e5-ed35-4244-872e-194862290295\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Dr.+Dog\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/41716447.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/41716447.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/41716447.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/41716447.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/41716447\\/Dr+Dog+drdog_img01_hires.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"12\"}},{\"name\":\"Shellac\",\"playcount\":\"9\",\"mbid\":\"604f273a-d3c1-4b5b-ab83-99541f9368ea\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Shellac\",\"streamable\":\"0\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/49773377.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/49773377.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/49773377.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/49773377.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/49773377\\/Shellac+_footup.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"13\"}},{\"name\":\"Extra Golden\",\"playcount\":\"8\",\"mbid\":\"c39b100f-0e43-4e39-b244-a33d6dcb09a0\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Extra+Golden\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/41654.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/41654.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/41654.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/41654.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/41654\\/Extra+Golden.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"14\"}},{\"name\":\"Pharoah Sanders\",\"playcount\":\"7\",\"mbid\":\"b3a0912a-a62a-4388-9368-7cb21ed5caf9\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Pharoah+Sanders\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/62865233.png\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/62865233.png\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/62865233.png\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/62865233.png\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/62865233\\/Pharoah+Sanders+ps.png\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"15\"}},{\"name\":\"Link Wray\",\"playcount\":\"6\",\"mbid\":\"1c1c86a2-7850-47ac-8771-ae6359bae2b7\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Link+Wray\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/98372.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/98372.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/98372.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/98372.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/98372\\/Link+Wray.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"16\"}},{\"name\":\"Cal Tjader\",\"playcount\":\"6\",\"mbid\":\"1015b5e3-8e3d-4d78-806a-c530cf742e66\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Cal+Tjader\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/71765998.png\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/71765998.png\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/71765998.png\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/71765998.png\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/71765998\\/Cal+Tjader+Tjader+PNG.png\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"17\"}},{\"name\":\"George Szell: Cleveland Orchestra\",\"playcount\":\"4\",\"mbid\":\"\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/George+Szell%3A+Cleveland+Orchestra\",\"streamable\":\"0\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/10663309.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/10663309.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/10663309.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/10663309.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/10663309\\/George+Szell+Cleveland+Orchestra+portrait_szell.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"18\"}},{\"name\":\"The Clash\",\"playcount\":\"2\",\"mbid\":\"8f92558c-2baa-4758-8c38-615519e9deda\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/The+Clash\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/36023147.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/36023147.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/36023147.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/36023147.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/36023147\\/The+Clash.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"19\"}},{\"name\":\"Medeski, Martin and Wood\",\"playcount\":\"2\",\"mbid\":\"\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Medeski%2C+Martin+and+Wood\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/205949.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/205949.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/205949.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/205949.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/205949\\/Medeski+Martin+and+Wood.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"20\"}},{\"name\":\"Mike Gordon\",\"playcount\":\"2\",\"mbid\":\"c172276a-fcbf-4477-894a-f37d1582557e\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Mike+Gordon\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/8554317.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/8554317.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/8554317.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/8554317.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/8554317\\/Mike+Gordon+mg2.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"21\"}},{\"name\":\"Rancid\",\"playcount\":\"1\",\"mbid\":\"24f8d8a5-269b-475c-a1cb-792990b0b2ee\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Rancid\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/204902.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/204902.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/204902.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/204902.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/204902\\/Rancid.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"22\"}},{\"name\":\"Miles Davis Quintet\",\"playcount\":\"1\",\"mbid\":\"fe7245e7-d734-4ca1-8e26-691883f58201\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Miles+Davis+Quintet\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/41641425.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/41641425.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/41641425.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/41641425.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/41641425\\/Miles+Davis+Quintet+miles+quintet.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"23\"}},{\"name\":\"James Taylor\",\"playcount\":\"1\",\"mbid\":\"107d0c22-d051-4d98-8206-4e14de02132a\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/James+Taylor\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/58696185.png\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/58696185.png\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/58696185.png\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/58696185.png\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/58696185\\/James+Taylor.png\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"24\"}},{\"name\":\"No Doubt\",\"playcount\":\"1\",\"mbid\":\"fbd2a255-1d57-4d31-ac11-65b671c19958\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/No+Doubt\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/52235841.png\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/52235841.png\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/52235841.png\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/52235841.png\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/52235841\\/No+Doubt.png\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"25\"}},{\"name\":\"The Mighty Mighty Bosstones\",\"playcount\":\"1\",\"mbid\":\"779353f3-6401-4cda-a8a2-6fd3ec9bc11b\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/The+Mighty+Mighty+Bosstones\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/178293.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/178293.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/178293.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/178293.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/178293\\/The+Mighty+Mighty+Bosstones.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"26\"}},{\"name\":\"Silverchair\",\"playcount\":\"1\",\"mbid\":\"b0799818-22cb-4564-8e68-3c410d0722ee\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Silverchair\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/20540173.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/20540173.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/20540173.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/20540173.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/20540173\\/Silverchair.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"27\"}},{\"name\":\"The Urge\",\"playcount\":\"1\",\"mbid\":\"4c35ea75-02d5-4a7a-8b01-f17effc07580\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/The+Urge\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/417812.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/417812.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/417812.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/417812.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/417812\\/The+Urge.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"28\"}},{\"name\":\"Third Eye Blind\",\"playcount\":\"1\",\"mbid\":\"92a42e82-b36f-4308-82c1-68bad2e03c89\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Third+Eye+Blind\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/7933051.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/7933051.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/7933051.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/7933051.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/7933051\\/Third+Eye+Blind+thirdeyeblind.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"29\"}},{\"name\":\"311\",\"playcount\":\"1\",\"mbid\":\"bf600e2b-dc2d-4839-a1be-6ebef4087cd0\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/311\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/23826283.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/23826283.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/23826283.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/23826283.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/23826283\\/311.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"30\"}},{\"name\":\"Cracker\",\"playcount\":\"1\",\"mbid\":\"ca48bfb8-37c5-4a04-9837-a07975ee0cd3\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Cracker\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/2177311.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/2177311.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/2177311.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/2177311.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/2177311\\/Cracker.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"31\"}},{\"name\":\"Indigo Girls\",\"playcount\":\"1\",\"mbid\":\"00c49f40-d715-4b79-b223-432048602cce\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Indigo+Girls\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/4861777.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/4861777.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/4861777.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/4861777.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/4861777\\/Indigo+Girls+IndigoGirls1web.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"32\"}},{\"name\":\"Afghan Whigs\",\"playcount\":\"1\",\"mbid\":\"\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Afghan+Whigs\",\"streamable\":\"1\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/65473.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/65473.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/65473.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/65473.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/65473\\/Afghan+Whigs.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"33\"}},{\"name\":\"Ice Cube and Mack 10\",\"playcount\":\"1\",\"mbid\":\"\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Ice+Cube+and+Mack+10\",\"streamable\":\"0\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/29327913.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/29327913.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/29327913.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/29327913.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/29327913\\/Ice+Cube+and+Mack+10+Burning_London.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"34\"}},{\"name\":\"moby featuring heather nova\",\"playcount\":\"1\",\"mbid\":\"\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/moby+featuring+heather+nova\",\"streamable\":\"0\",\"image\":[{\"#text\":\"\",\"size\":\"small\"},{\"#text\":\"\",\"size\":\"medium\"},{\"#text\":\"\",\"size\":\"large\"},{\"#text\":\"\",\"size\":\"extralarge\"},{\"#text\":\"\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"35\"}},{\"name\":\"Gap Mangione\",\"playcount\":\"1\",\"mbid\":\"e8497fd3-0b92-463b-9ea5-2024914503d5\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Gap+Mangione\",\"streamable\":\"0\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/27202937.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/27202937.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/27202937.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/27202937.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/500\\/27202937\\/Gap+Mangione+GapMangione_SheAndI_back.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"36\"}},{\"name\":\"James Tatum\",\"playcount\":\"1\",\"mbid\":\"3d700a72-30b2-4425-bcee-12700b6acb68\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/James+Tatum\",\"streamable\":\"0\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/70147742.png\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/70147742.png\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/70147742.png\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/70147742.png\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/70147742\\/James+Tatum+tatum.png\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"37\"}},{\"name\":\"Bethlehem Progressive Ensemble\",\"playcount\":\"1\",\"mbid\":\"895da026-078c-4402-9edb-d9190abd6046\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Bethlehem+Progressive+Ensemble\",\"streamable\":\"0\",\"image\":[{\"#text\":\"\",\"size\":\"small\"},{\"#text\":\"\",\"size\":\"medium\"},{\"#text\":\"\",\"size\":\"large\"},{\"#text\":\"\",\"size\":\"extralarge\"},{\"#text\":\"\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"38\"}},{\"name\":\"Carrie Cleveland\",\"playcount\":\"1\",\"mbid\":\"e9fd9327-1ef4-44e2-9a99-cece8ea273b5\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/Carrie+Cleveland\",\"streamable\":\"0\",\"image\":[{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/34\\/2969279.jpg\",\"size\":\"small\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/64\\/2969279.jpg\",\"size\":\"medium\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/126\\/2969279.jpg\",\"size\":\"large\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/252\\/2969279.jpg\",\"size\":\"extralarge\"},{\"#text\":\"http:\\/\\/userserve-ak.last.fm\\/serve\\/_\\/2969279\\/Carrie+Cleveland.jpg\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"39\"}},{\"name\":\"MeastroClassical\",\"playcount\":\"1\",\"mbid\":\"\",\"url\":\"http:\\/\\/www.last.fm\\/music\\/MeastroClassical\",\"streamable\":\"0\",\"image\":[{\"#text\":\"\",\"size\":\"small\"},{\"#text\":\"\",\"size\":\"medium\"},{\"#text\":\"\",\"size\":\"large\"},{\"#text\":\"\",\"size\":\"extralarge\"},{\"#text\":\"\",\"size\":\"mega\"}],\"@attr\":{\"rank\":\"40\"}}],\"@attr\":{\"user\":\"pdunham\",\"type\":\"7day\",\"page\":\"1\",\"perPage\":\"50\",\"totalPages\":\"1\",\"total\":\"40\"}}}";
	}

}