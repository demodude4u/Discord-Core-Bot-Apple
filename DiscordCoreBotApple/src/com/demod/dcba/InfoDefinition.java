package com.demod.dcba;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class InfoDefinition {

	private Optional<String> botName = Optional.empty();
	private final LinkedHashMap<String, List<String>> credits = new LinkedHashMap<>();
	private Optional<String> supportMessage = Optional.empty();
	private Optional<String> version = Optional.empty();
	private final List<String> technologies = new ArrayList<>();

	public void addCredits(String group, String[] names) {
		List<String> groupNames = credits.computeIfAbsent(group, k -> new ArrayList<>());
		Collections.addAll(groupNames, names);
	}

	public void addTechnology(String name, Optional<String> version, String description) {
		technologies.add("**" + name + "** " + version.map(v -> "`" + v + "`").orElse("") + " - " + description);
	}

	public Optional<String> getBotName() {
		return botName;
	}

	public LinkedHashMap<String, List<String>> getCredits() {
		return credits;
	}

	public Optional<String> getSupportMessage() {
		return supportMessage;
	}

	public List<String> getTechnologies() {
		return technologies;
	}

	public Optional<String> getVersion() {
		return version;
	}

	public void setBotName(Optional<String> botName) {
		this.botName = botName;
	}

	public void setSupport(Optional<String> supportMessage) {
		this.supportMessage = supportMessage;
	}

	public void setVersion(Optional<String> version) {
		this.version = version;
	}

}
