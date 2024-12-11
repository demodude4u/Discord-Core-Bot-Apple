package com.demod.dcba;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.SplitUtil;
import net.dv8tion.jda.api.utils.SplitUtil.Strategy;

public interface EventReply {

	User getReplyPrivateUser();

	CommandReporting getReporting();

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

	default List<Message> reply(String response) {
		List<Message> ret = new ArrayList<>();
		for (String split : SplitUtil.split(response, MessageEmbed.DESCRIPTION_MAX_LENGTH, true, Strategy.NEWLINE,
				Strategy.ANYWHERE)) {
			ret.add(replyEmbed(new EmbedBuilder().appendDescription(split).build()));
		}
		return ret;
	}

	Message replyEmbed(MessageEmbed embed, MessageEmbed... embeds);

	default Message replyFile(byte[] data, String filename) {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
			return replyFile(bais, filename);
		} catch (IOException e) {
			throw new InternalError(e);// Should not happen
		}
	}

	Message replyFile(InputStream data, String filename);

	default Message replyFile(String content, String filename) {
		return replyFile(content.getBytes(StandardCharsets.UTF_8), filename);
	}

	default public void replyIfNoException(String response) {
		if (getReporting().getExceptionsWithBlame().isEmpty()) {
			reply(response);
		}
	}

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

	default void replyPrivate(String response) {
		for (String split : SplitUtil.split(response, MessageEmbed.DESCRIPTION_MAX_LENGTH, true, Strategy.NEWLINE,
				Strategy.ANYWHERE)) {
			replyPrivateEmbed(new EmbedBuilder().appendDescription(split).build());
		}
	}

	default Message replyPrivateEmbed(MessageEmbed embed, MessageEmbed... embeds) {
		return getReplyPrivateUser().openPrivateChannel().complete().sendMessageEmbeds(embed, embeds).complete();
	}

	default Message replyPrivateFile(byte[] data, String filename) {
		return getReplyPrivateUser().openPrivateChannel().complete().sendFiles(FileUpload.fromData(data, filename))
				.complete();
	}

}