package com.demod.dcba;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

@FunctionalInterface
public interface MessageContextHandler {
	void onMessageContextInteraction(MessageContextInteractionEvent event, CommandReporting reporting) throws Exception;
}
