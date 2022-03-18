package com.demod.dcba;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.MessageBuilder.SplitPolicy;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

@FunctionalInterface
public interface TextWatcher {
	public interface SimpleWatcher extends TextWatcher {
		@Override
		default void seenMessage(MessageReceivedEvent event) {
			String response;
			response = seenSimpleMessage(event);
			if (response != null) {
				new MessageBuilder(response).buildAll(SplitPolicy.NEWLINE).forEach(m -> {
					event.getChannel().sendMessage(m).complete();
				});
			}
		}

		String seenSimpleMessage(MessageReceivedEvent event);
	}

	default void deletedMessage(MessageDeleteEvent event) {
	}

	default void editedMessage(MessageUpdateEvent event) {
	}

	void seenMessage(MessageReceivedEvent event);
}
