package com.demod.dcba;

import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.MessageBuilder.SplitPolicy;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public interface EventReply {

	User getReplyPrivateUser();

	default void reply(List<String> responseSegments) {
		StringBuilder builder = new StringBuilder();
		for (String segment : responseSegments) {
			if (builder.length() + segment.length() <= 2000) {
				builder.append(segment);
			} else {
				if (builder.length() > 0) {
					reply(builder.toString());
					builder = new StringBuilder();
				}
				if (segment.length() > 2000) {
					reply(segment);
				} else {
					builder.append(segment);
				}
			}
		}
		if (builder.length() > 0) {
			reply(builder.toString());
		}
	}

	Message reply(Message message);

	default void reply(String response) {
		for (Message message : new MessageBuilder(response).buildAll(SplitPolicy.NEWLINE)) {
			// reply(message);
			replyEmbed(new EmbedBuilder().appendDescription(message.getContentRaw()).build());
		}
	}

	Message replyEmbed(MessageEmbed embed, MessageEmbed... embeds);

	Message replyFile(byte[] data, String filename);

	default void replyPrivate(List<String> responseSegments) {
		StringBuilder builder = new StringBuilder();
		for (String segment : responseSegments) {
			if (builder.length() + segment.length() <= 2000) {
				builder.append(segment);
			} else {
				if (builder.length() > 0) {
					replyPrivate(builder.toString());
					builder = new StringBuilder();
				}
				if (segment.length() > 2000) {
					replyPrivate(segment);
				} else {
					builder.append(segment);
				}
			}
		}
		if (builder.length() > 0) {
			replyPrivate(builder.toString());
		}
	}

	default Message replyPrivate(Message message) {
		return getReplyPrivateUser().openPrivateChannel().complete().sendMessage(message).complete();
	}

	default void replyPrivate(String response) {
		for (Message message : new MessageBuilder(response).buildAll(SplitPolicy.NEWLINE)) {
			// reply(message);
			replyPrivateEmbed(new EmbedBuilder().appendDescription(message.getContentRaw()).build());
		}
	}

	default Message replyPrivateEmbed(MessageEmbed embed, MessageEmbed... embeds) {
		return getReplyPrivateUser().openPrivateChannel().complete().sendMessageEmbeds(embed, embeds).complete();
	}

	default Message replyPrivateFile(byte[] data, String filename) {
		return getReplyPrivateUser().openPrivateChannel().complete().sendFile(data, filename).complete();
	}

}