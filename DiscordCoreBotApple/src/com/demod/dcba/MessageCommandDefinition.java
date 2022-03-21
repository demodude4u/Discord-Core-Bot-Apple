package com.demod.dcba;

import java.util.EnumSet;

public class MessageCommandDefinition {
	public enum Restriction {
		ADMIN_ONLY, PRIVATE_CHANNEL_ONLY, GUILD_CHANNEL_ONLY, EPHEMERAL
	}

	private final String name;
	private final MessageCommandHandler handler;
	private final EnumSet<Restriction> restrictions = EnumSet.noneOf(Restriction.class);

	public MessageCommandDefinition(String name, MessageCommandHandler handler) {
		this.name = name;
		this.handler = handler;
	}

	public void clearRestriction(Restriction restriction) {
		restrictions.remove(restriction);
	}

	public MessageCommandHandler getHandler() {
		return handler;
	}

	public String getName() {
		return name;
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
