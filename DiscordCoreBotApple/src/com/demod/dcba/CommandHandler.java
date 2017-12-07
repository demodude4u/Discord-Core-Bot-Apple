package com.demod.dcba;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

@FunctionalInterface
public interface CommandHandler {

	public interface NoArgHandler extends CommandHandler {
		void handleCommand(MessageReceivedEvent event);

		@Override
		default void handleCommand(MessageReceivedEvent event, String[] args) {
			handleCommand(event);
		}
	}

	public interface SimpleArgResponse extends CommandHandler {
		@Override
		default void handleCommand(MessageReceivedEvent event, String[] args) {
			String response = handleSimpleResponse(event, args);
			if (response != null) {
				DiscordUtils.replyTo(event.getChannel(), response);
			}
		}

		String handleSimpleResponse(MessageReceivedEvent event, String[] args);
	}

	@FunctionalInterface
	public interface SimpleResponse extends CommandHandler.NoArgHandler {
		@Override
		default void handleCommand(MessageReceivedEvent event) {
			String response = handleSimpleResponse(event);
			if (response != null) {
				DiscordUtils.replyTo(event.getChannel(), response);
			}
		}

		String handleSimpleResponse(MessageReceivedEvent event);
	}

	void handleCommand(MessageReceivedEvent event, String[] args);
}
