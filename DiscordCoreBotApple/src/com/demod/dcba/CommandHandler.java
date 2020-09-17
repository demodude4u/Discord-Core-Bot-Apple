package com.demod.dcba;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@FunctionalInterface
public interface CommandHandler {

	public interface NoArgHandler extends CommandHandler {
		void handleCommand(MessageReceivedEvent event) throws Exception;

		@Override
		default void handleCommand(MessageReceivedEvent event, String[] args) throws Exception {
			handleCommand(event);
		}
	}

	public interface SimpleArgResponse extends CommandHandler {
		@Override
		default void handleCommand(MessageReceivedEvent event, String[] args) throws Exception {
			String response;
			response = handleSimpleResponse(event, args);
			if (response != null) {
				DiscordUtils.replyTo(event.getChannel(), response);
			}
		}

		String handleSimpleResponse(MessageReceivedEvent event, String[] args) throws Exception;
	}

	@FunctionalInterface
	public interface SimpleResponse extends SimpleArgResponse {
		String handleSimpleResponse(MessageReceivedEvent event) throws Exception;

		@Override
		default String handleSimpleResponse(MessageReceivedEvent event, String[] args) throws Exception {
			return handleSimpleResponse(event);
		}
	}

	void handleCommand(MessageReceivedEvent event, String[] args) throws Exception;
}
