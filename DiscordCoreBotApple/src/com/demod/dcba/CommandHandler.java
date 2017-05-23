package com.demod.dcba;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

@FunctionalInterface
public interface CommandHandler {

	@FunctionalInterface
	public interface SimpleResponse extends CommandHandler {
		@Override
		default void handleCommand(MessageReceivedEvent event) {
			String response = handleSimpleResponse(event);
			if (response != null) {
				DiscordUtils.replyTo(event, response);
			}
		}

		String handleSimpleResponse(MessageReceivedEvent event);
	}

	void handleCommand(MessageReceivedEvent event);
}
