package com.demod.dcba;

import net.dv8tion.jda.api.EmbedBuilder;

@FunctionalInterface
public interface CommandHandler {

	@FunctionalInterface
	public interface SimpleEmbedResponse extends CommandHandler {
		@Override
		default void handleCommand(CommandEvent event) throws Exception {
			EmbedBuilder embed = new EmbedBuilder();
			handleSimpleResponse(event, embed);
			if (!embed.isEmpty()) {
				event.replyEmbed(embed.build());
			}
		}

		void handleSimpleResponse(CommandEvent event, EmbedBuilder embed) throws Exception;
	}

	@FunctionalInterface
	public interface SimpleResponse extends CommandHandler {
		@Override
		default void handleCommand(CommandEvent event) throws Exception {
			String response = handleSimpleResponse(event);
			if (response != null) {
				event.reply(response);
			}
		}

		String handleSimpleResponse(CommandEvent event) throws Exception;
	}

	void handleCommand(CommandEvent event) throws Exception;
}
