package com.demod.dcba;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class SlashCommandDefinition {
	public enum Restriction {
		ADMIN_ONLY, PRIVATE_CHANNEL_ONLY, GUILD_CHANNEL_ONLY, EPHEMERAL
	}

	private final String path;
	private final String description;
	private final SlashCommandHandler handler;
	private final List<SlashCommandOptionDefinition> options = new ArrayList<>();
	private final List<String> legacies = new ArrayList<>();
	private final EnumSet<Restriction> restrictions = EnumSet.noneOf(Restriction.class);

	public SlashCommandDefinition(String path, String description, SlashCommandHandler handler,
			SlashCommandOptionDefinition... options) {
		this.path = path;
		this.description = description;
		this.handler = handler;
		Collections.addAll(this.options, options);
	}

	public void addLegacy(String name) {
		legacies.add(name);
	}

	public void addOption(SlashCommandOptionDefinition option) {
		options.add(option);
	}

	public void clearRestriction(Restriction restriction) {
		restrictions.remove(restriction);
	}

	public String getDescription() {
		return description;
	}

	public SlashCommandHandler getHandler() {
		return handler;
	}

	public List<String> getLegacies() {
		return legacies;
	}

	public List<SlashCommandOptionDefinition> getOptions() {
		return options;
	}

	public String getPath() {
		return path;
	}

	public EnumSet<Restriction> getRestrictions() {
		return restrictions;
	}

	public boolean hasRestriction(Restriction restriction) {
		return restrictions.contains(restriction);
	}

	public void setRestriction(Restriction restriction) {
		restrictions.add(restriction);
	}
}