package com.demod.dcba;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@FunctionalInterface
public interface ExceptionHandler {
	void handleException(CommandDefinition command, MessageReceivedEvent event, Exception e);
}
