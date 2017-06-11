package com.demod.dcba;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.core.Permission;

public class InfoDefinition {

	private Optional<String> botName = Optional.empty();
	private final LinkedHashMap<String, List<String>> credits = new LinkedHashMap<>();
	private Optional<String> supportMessage = Optional.empty();
	private Optional<String> version = Optional.empty();
	private final List<String> technologies = new ArrayList<>();
	private boolean allowInvite = false;
	private Permission[] invitePermissions;

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

	public Permission[] getInvitePermissions() {
		return invitePermissions;
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

	public boolean isAllowInvite() {
		return allowInvite;
	}

	public void setAllowInvite(boolean allowInvite) {
		this.allowInvite = allowInvite;
	}

	public void setBotName(Optional<String> botName) {
		this.botName = botName;
	}

	public void setInvitePermissions(Permission[] invitePermissions) {
		this.invitePermissions = invitePermissions;
	}

	public void setSupport(Optional<String> supportMessage) {
		this.supportMessage = supportMessage;
	}

	public void setVersion(Optional<String> version) {
		this.version = version;
	}

}
