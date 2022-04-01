package com.demod.dcba;

import java.util.EnumSet;

public class MessageCommandDefinition {
	private final String name;
	private final MessageCommandHandler handler;
	private final EnumSet<CommandRestriction> restrictions = EnumSet.noneOf(CommandRestriction.class);

	public MessageCommandDefinition(String name, MessageCommandHandler handler) {
		this.name = name;
		this.handler = handler;
	}

	public void clearRestriction(CommandRestriction restriction) {
		restrictions.remove(restriction);
	}

	public MessageCommandHandler getHandler() {
		return handler;
	}

	public String getName() {
		return name;
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
