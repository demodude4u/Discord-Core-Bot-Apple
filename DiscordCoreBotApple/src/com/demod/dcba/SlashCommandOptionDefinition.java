package com.demod.dcba;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SlashCommandOptionDefinition {
	private final OptionType type;
	private final String name;
	private final String description;
	private final boolean required;
	private final boolean autoComplete;

	public SlashCommandOptionDefinition(OptionType type, String name, String description, boolean required,
			boolean autoComplete) {
		this.type = type;
		this.name = name;
		this.description = description;
		this.required = required;
		this.autoComplete = autoComplete;
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

	public boolean isAutoComplete() {
		return autoComplete;
	}

	public boolean isRequired() {
		return required;
	}
}
