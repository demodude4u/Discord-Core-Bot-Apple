package com.demod.dcba;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class SlashCommandDefinition {

	private final String path;
	private final String description;
	private final SlashCommandHandler handler;
	private final Optional<AutoCompleteHandler> autoCompleteHandler;
	private final List<SlashCommandOptionDefinition> options = new ArrayList<>();
	private final List<String> legacies = new ArrayList<>();
	private final EnumSet<CommandRestriction> restrictions = EnumSet.noneOf(CommandRestriction.class);

	public SlashCommandDefinition(String path, String description, SlashCommandHandler handler,
			AutoCompleteHandler autoCompleteHandler, SlashCommandOptionDefinition... options) {
		this.path = path;
		this.description = description;
		this.handler = handler;
		this.autoCompleteHandler = Optional.ofNullable(autoCompleteHandler);
		Collections.addAll(this.options, options);
	}

	public SlashCommandDefinition(String path, String description, SlashCommandHandler handler,
			SlashCommandOptionDefinition... options) {
		this(path, description, handler, null, options);
	}

	public void addLegacy(String name) {
		legacies.add(name);
	}

	public void addOption(SlashCommandOptionDefinition option) {
		options.add(option);
	}

	public void clearRestriction(CommandRestriction restriction) {
		restrictions.remove(restriction);
	}

	public Optional<AutoCompleteHandler> getAutoCompleteHandler() {
		return autoCompleteHandler;
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
