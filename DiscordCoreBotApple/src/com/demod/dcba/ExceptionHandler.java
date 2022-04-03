package com.demod.dcba;

public interface ExceptionHandler {
	void handleMessageCommandException(MessageCommandDefinition command, MessageCommandEvent event, Exception e);

	void handleSlashCommandException(SlashCommandDefinition command, EventReply event, Exception e);
}
