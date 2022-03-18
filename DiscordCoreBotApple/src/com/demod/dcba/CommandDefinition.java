package com.demod.dcba;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class CommandDefinition {
	public enum CommandRestriction {
		ADMIN_ONLY, PRIVATE_CHANNEL_ONLY, GUILD_CHANNEL_ONLY
	}

	private final String name;
	private final CommandHandler handler;
	private final List<CommandOptionDefinition> options = new ArrayList<>();
	private Optional<String[]> aliases = Optional.empty();
	private Optional<String> help = Optional.empty();
	private final EnumSet<CommandRestriction> restrictions = EnumSet.noneOf(CommandRestriction.class);

	public CommandDefinition(String name, boolean adminOnly, String help, CommandHandler handler,
			CommandOptionDefinition... options) {
		this.name = name;
		this.handler = handler;
		Collections.addAll(this.options, options);
		this.help = Optional.of(help);

		if (adminOnly) {
			setRestriction(CommandRestriction.ADMIN_ONLY);
		}
	}

	public CommandDefinition(String name, CommandHandler handler, CommandOptionDefinition... options) {
		this.name = name;
		this.handler = handler;
		Collections.addAll(this.options, options);
	}

	public void addOption(CommandOptionDefinition option) {
		options.add(option);
	}

	public void clearRestriction(CommandRestriction restriction) {
		restrictions.remove(restriction);
	}

	public Optional<String[]> getAliases() {
		return aliases;
	}

	public String getAliasesString(String commandPrefix) {
		String ret = "";
		if (aliases.isPresent()) {
			for (String alias : aliases.get()) {
				ret += ", ``" + commandPrefix + alias + "``";
			}
		}

		return ret;
	}

	public CommandHandler getHandler() {
		return handler;
	}

	public Optional<String> getHelp() {
		return help;
	}

	public String getName() {
		return name;
	}

	public List<CommandOptionDefinition> getOptions() {
		return options;
	}

	public EnumSet<CommandRestriction> getRestrictions() {
		return restrictions;
	}

	public boolean hasRestriction(CommandRestriction restriction) {
		return restrictions.contains(restriction);
	}

	public void setAliases(Optional<String[]> aliases) {
		this.aliases = aliases;
	}

	public void setHelp(Optional<String> help) {
		this.help = help;
	}

	public void setRestriction(CommandRestriction restriction) {
		restrictions.add(restriction);
	}
}
