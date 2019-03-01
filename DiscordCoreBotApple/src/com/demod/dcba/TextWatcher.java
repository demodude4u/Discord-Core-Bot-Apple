package com.demod.dcba;

import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;

@FunctionalInterface
public interface TextWatcher {
	public interface SimpleWatcher extends TextWatcher {
		@Override
		default void seenMessage(MessageReceivedEvent event) {
			String response;
			response = seenSimpleMessage(event);
			if (response != null) {
				DiscordUtils.replyTo(event.getChannel(), response);
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
