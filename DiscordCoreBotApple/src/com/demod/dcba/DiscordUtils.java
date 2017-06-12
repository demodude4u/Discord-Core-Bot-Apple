package com.demod.dcba;

import java.util.List;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;

public final class DiscordUtils {
	public static void replyTo(MessageChannel channel, List<String> responseSegments) {
		MessageBuilder builder = new MessageBuilder();
		for (String segment : responseSegments) {
			if (builder.length() + segment.length() <= 2000) {
				builder.append(segment);
			} else {
				if (!builder.isEmpty()) {
					channel.sendMessage(builder.build()).complete();
					builder = new MessageBuilder();
				}
				if (segment.length() > 2000) {
					replyTo(channel, segment);
				} else {
					builder.append(segment);
				}
			}
		}
		if (!builder.isEmpty()) {
			channel.sendMessage(builder.build()).complete();
		}
	}

	static void replyTo(MessageChannel channel, String response) {
		int maxSizeWithSafety = DiscordConsts.MAX_MESSAGE_SIZE - 1;
		if (response.length() <= maxSizeWithSafety) {
			channel.sendMessage(DiscordConsts.SAFETY_CHAR + response).complete();
		} else {
			for (int i = 0; i < response.length(); i += maxSizeWithSafety) {
				channel.sendMessage(
						new MessageBuilder()
								.append(DiscordConsts.SAFETY_CHAR
										+ response.substring(i, Math.min(i + maxSizeWithSafety, response.length())))
								.build())
						.complete();
			}
		}
	}

	private DiscordUtils() {
	}

}
