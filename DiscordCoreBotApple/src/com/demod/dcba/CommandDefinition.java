package com.demod.dcba;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class CommandDefinition {
	public enum CommandRestriction {
		ADMIN_ONLY, PRIVATE_CHANNEL_ONLY, GUILD_CHANNEL_ONLY
	}

	private final String path;
	private final String description;
	private final CommandHandler handler;
	private final List<CommandOptionDefinition> options = new ArrayList<>();
	private final List<String> legacies = new ArrayList<>();
	private final EnumSet<CommandRestriction> restrictions = EnumSet.noneOf(CommandRestriction.class);

	public CommandDefinition(String path, String description, CommandHandler handler,
			CommandOptionDefinition... options) {
		this.path = path;
		this.description = description;
		this.handler = handler;
		Collections.addAll(this.options, options);
	}

	public void addLegacy(String name) {
		legacies.add(name);
	}

	public void addOption(CommandOptionDefinition option) {
		options.add(option);
	}

	public void clearRestriction(CommandRestriction restriction) {
		restrictions.remove(restriction);
	}

	public String getDescription() {
		return description;
	}

	public CommandHandler getHandler() {
		return handler;
	}

	public List<String> getLegacies() {
		return legacies;
	}

	public List<CommandOptionDefinition> getOptions() {
		return options;
	}

	public String getPath() {
		return path;
	}

	public EnumSet<CommandRestriction> getRestrictions() {
		return restrictions;
	}

	public boolean hasRestriction(CommandRestriction restriction) {
		return restrictions.contains(restriction);
	}

	public void setRestriction(CommandRestriction restriction) {
		restrictions.add(restriction);
	}
}
