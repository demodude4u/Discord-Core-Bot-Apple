package com.demod.dcba;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

@FunctionalInterface
public interface ReactionWatcher {
	public interface SimpleWatcher extends ReactionWatcher {
		@Override
		default void seenReaction(MessageReactionAddEvent event) {
			String response;
			response = seenSimpleMessage(event);
			if (response != null) {
				DiscordUtils.replyTo(event.getChannel(), response);
			}
		}

		String seenSimpleMessage(MessageReactionAddEvent event);
	}

	default void seenAllReactionRemoved(MessageReactionRemoveAllEvent event) {
	}

	void seenReaction(MessageReactionAddEvent event);

	default void seenReactionRemoved(MessageReactionRemoveEvent event) {
	}
}
