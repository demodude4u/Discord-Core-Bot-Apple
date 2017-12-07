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
			String response;
			try {
				response = handleSimpleResponse(event, args);
			} catch (Exception e) {
				e.printStackTrace();
				response = "Error: [" + e.getClass().getSimpleName() + "] " + e.getMessage();
			}
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

	void handleCommand(MessageReceivedEvent event, String[] args);
}
