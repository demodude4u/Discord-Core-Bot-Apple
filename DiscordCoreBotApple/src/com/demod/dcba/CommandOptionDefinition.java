package com.demod.dcba;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CommandOptionDefinition {
	private final OptionType type;
	private final String name;
	private final String description;
	private final boolean required;

	public CommandOptionDefinition(OptionType type, String name, String description, boolean required) {
		this.type = type;
		this.name = name;
		this.description = description;
		this.required = required;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public OptionType getType() {
		return type;
	}

	public boolean isRequired() {
		return required;
	}
}
