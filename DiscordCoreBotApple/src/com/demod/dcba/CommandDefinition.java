package com.demod.dcba;

import java.util.Optional;

public class CommandDefinition {
	private final String name;
	private final CommandHandler handler;
	private Optional<String> help = Optional.empty();

	public CommandDefinition(String name, CommandHandler handler) {
		this.name = name;
		this.handler = handler;
	}

	public CommandDefinition(String name, String help, CommandHandler handler) {
		this.name = name;
		this.handler = handler;
		this.help = Optional.of(help);
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

	public void setHelp(Optional<String> help) {
		this.help = help;
	}
}
