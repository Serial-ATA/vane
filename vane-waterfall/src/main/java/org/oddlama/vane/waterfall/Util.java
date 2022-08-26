package org.oddlama.vane.waterfall;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Util {


	private static Map<Character, Long> time_multiplier;

	static {
		Map<Character, Long> mult = new HashMap<>();
		mult.put('s', 1000L); //seconds
		mult.put('m', 60000L); //minutes
		mult.put('h', 3600000L); //hours
		mult.put('d', 86400000L); //days
		mult.put('w', 604800000L); //weeks
		mult.put('y', 31536000000L); //years
		time_multiplier = mult;
	}

	public static long parse_time(String input) throws NumberFormatException {
		long ret = 0;

		for (String time : input.split("(?<=[^0-9])(?=[0-9])")) {
			String content[] = time.split("(?=[^0-9])");

			if (content.length != 2) {
				throw new NumberFormatException("missing multiplier");
			}

			Long mult = time_multiplier.get(content[1].replace("and", "").replaceAll("[,+\\.\\s]+", "").charAt(0));
			if (mult == null) {
				throw new NumberFormatException("\"" + content[1] + "\" is not a valid multiplier");
			}

			ret += Long.parseLong(content[0]) * mult;
		}

		return ret;
	}

	public static String format_time(long millis) {
		String ret = "";

		long days = millis / 86400000L;
		long hours = (millis / 3600000L) % 24;
		long minutes = (millis / 60000L) % 60;
		long seconds = (millis / 1000L) % 60;

		if (days > 0) {
			ret += Long.toString(days) + "d";
		}

		if (hours > 0) {
			ret += Long.toString(hours) + "h";
		}

		if (minutes > 0) {
			ret += Long.toString(minutes) + "m";
		}

		if (seconds > 0 || ret.length() == 0) {
			ret += Long.toString(seconds) + "s";
		}

		return ret;
	}

	private static String read_all(Reader rd) throws IOException {
		final var sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject read_json_from_url(String url) throws IOException, JSONException {
		try (
				final var rd = new BufferedReader(new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8))
		) {
			return new JSONObject(read_all(rd));
		}
	}

	public static UUID resolve_uuid(String name) throws IOException {
		final var url = "https://api.mojang.com/users/profiles/minecraft/" + name;

		final var json = read_json_from_url(url);
		final var id_str = json.getString("id");
		final var uuid_str = id_str.replaceFirst(
				"(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
				"$1-$2-$3-$4-$5"
		);
		return UUID.fromString(uuid_str);
	}

	public static UUID add_uuid(UUID uuid, long i) {
		var msb = uuid.getMostSignificantBits();
		var lsb = uuid.getLeastSignificantBits();

		lsb += i;
		if (lsb < uuid.getLeastSignificantBits()) {
			++msb;
		}

		return new UUID(msb, lsb);
	}
}
