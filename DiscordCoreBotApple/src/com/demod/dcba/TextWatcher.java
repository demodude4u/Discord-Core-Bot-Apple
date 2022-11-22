package com.demod.dcba;

import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.utils.SplitUtil;
import net.dv8tion.jda.api.utils.SplitUtil.Strategy;

@FunctionalInterface
public interface TextWatcher {
	public interface SimpleWatcher extends TextWatcher {
		@Override
		default void seenMessage(MessageReceivedEvent event) {
			String response;
			response = seenSimpleMessage(event);
			if (response != null) {
				SplitUtil.split(response, 2000, true, Strategy.NEWLINE, Strategy.ANYWHERE).forEach(m -> {
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
