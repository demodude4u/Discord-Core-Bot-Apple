package com.demod.dcba;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.utils.SplitUtil;
import net.dv8tion.jda.api.utils.SplitUtil.Strategy;

@FunctionalInterface
public interface ReactionWatcher {
	public interface SimpleWatcher extends ReactionWatcher {
		@Override
		default void seenReaction(MessageReactionAddEvent event) {
			String response;
			response = seenSimpleMessage(event);
			if (response != null) {
				SplitUtil.split(response, 2000, true, Strategy.NEWLINE, Strategy.ANYWHERE).forEach(m -> {
					event.getChannel().sendMessage(m).complete();
				});
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
