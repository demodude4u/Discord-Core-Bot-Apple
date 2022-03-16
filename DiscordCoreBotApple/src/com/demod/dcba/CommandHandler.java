package com.demod.dcba;

@FunctionalInterface
public interface CommandHandler {

	@FunctionalInterface
	public interface SimpleResponse extends CommandHandler {
		@Override
		default void handleCommand(CommandEvent event) throws Exception {
			String response;
			response = handleSimpleResponse(event);
			if (response != null) {
				event.reply(response);
			}
		}

		String handleSimpleResponse(CommandEvent event) throws Exception;
	}

	void handleCommand(CommandEvent event) throws Exception;
}
