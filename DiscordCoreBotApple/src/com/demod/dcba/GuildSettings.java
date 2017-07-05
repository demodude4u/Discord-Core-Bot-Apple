package com.demod.dcba;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

public final class GuildSettings {
	private static final String GUILDS_FILE = "guilds.json";

	private static JSONObject guildsJson = null;

	public static synchronized JSONObject get(String guildId) {
		if (guildsJson == null) {
			guildsJson = loadGuilds();
		}

		if (guildsJson.has(guildId)) {
			return guildsJson.getJSONObject(guildId);
		} else {
			return new JSONObject();
		}
	}

	private static JSONObject loadGuilds() {
		try (Scanner scanner = new Scanner(new FileInputStream(GUILDS_FILE), "UTF-8")) {
			scanner.useDelimiter("\\A");
			return new JSONObject(scanner.next());
		} catch (JSONException | IOException e) {
			System.out.println(GUILDS_FILE + " was not found!");
			return new JSONObject();
		}
	}

	public static synchronized void save(String guildId, JSONObject guildJson) {
		guildsJson.put(guildId, guildJson);

		try (FileWriter fw = new FileWriter(GUILDS_FILE)) {
			fw.write(guildsJson.toString(2));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private GuildSettings() {
	}
}
