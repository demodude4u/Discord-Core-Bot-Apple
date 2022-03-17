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
				event.reply(embed.build());
			}
		}

		void handleSimpleResponse(CommandEvent event, EmbedBuilder embed) throws Exception;
	}

	@FunctionalInterface
	public interface SimpleEmbedSlashResponse extends SimpleEmbedResponse {
		@Override
		default void handleSimpleResponse(CommandEvent event, EmbedBuilder embed) throws Exception {
			handleSimpleSlashResponse((SlashInteractionCommandEvent) event, embed);
		}

		void handleSimpleSlashResponse(SlashInteractionCommandEvent event, EmbedBuilder embed) throws Exception;
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

	@FunctionalInterface
	public interface SimpleSlashResponse extends SimpleResponse {
		@Override
		default String handleSimpleResponse(CommandEvent event) throws Exception {
			return handleSimpleSlashResponse((SlashInteractionCommandEvent) event);
		}

		String handleSimpleSlashResponse(SlashInteractionCommandEvent event) throws Exception;
	}

	@FunctionalInterface
	public interface SlashCommandHandler extends CommandHandler {
		@Override
		default void handleCommand(CommandEvent event) throws Exception {
			handleSlashCommand((SlashInteractionCommandEvent) event);
		}

		void handleSlashCommand(SlashInteractionCommandEvent event) throws Exception;
	}

	void handleCommand(CommandEvent event) throws Exception;
}
