package com.demod.dcba;

import java.util.Optional;

public class CommandDefinition {
	private final String name;
	private final CommandHandler handler;
	private Optional<String[]> aliases = Optional.empty();
	private Optional<String> help = Optional.empty();
	private boolean adminOnly = false;

	public CommandDefinition(String name, boolean adminOnly, String help, CommandHandler handler) {
		this.name = name;
		this.handler = handler;
		this.help = Optional.of(help);
		this.adminOnly = adminOnly;
	}

	public CommandDefinition(String name, CommandHandler handler) {
		this.name = name;
		this.handler = handler;
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

	public boolean isAdminOnly() {
		return adminOnly;
	}

	public void setAdminOnly(boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

	public void setAliases(Optional<String[]> aliases) {
		this.aliases = aliases;
	}

	public void setHelp(Optional<String> help) {
		this.help = help;
	}
}
