package com.demod.dcba;

import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.MessageBuilder.SplitPolicy;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public abstract class CommandEvent {

	public abstract User getAuthor();

	public abstract ChannelType getChannelType();

	public abstract String getCommandString();

	public abstract Guild getGuild();

	public abstract JDA getJDA();

	public abstract Member getMember();

	public abstract MessageChannel getMessageChannel();

	public abstract String getParam(String name);

	public abstract boolean isFromType(ChannelType private1);

	public abstract Optional<String> optParam(String name);

	public void reply(List<String> responseSegments) {
		MessageBuilder builder = new MessageBuilder();
		for (String segment : responseSegments) {
			if (builder.length() + segment.length() <= 2000) {
				builder.append(segment);
			} else {
				if (!builder.isEmpty()) {
					reply(builder.build());
					builder = new MessageBuilder();
				}
				if (segment.length() > 2000) {
					reply(segment);
				} else {
					builder.append(segment);
				}
			}
		}
		if (!builder.isEmpty()) {
			reply(builder.build());
		}
	}

	public abstract Message reply(Message message);

	public void reply(String response) {
//		int maxSizeWithSafety = DiscordConsts.MAX_MESSAGE_SIZE - 1;
//		if (response.length() <= maxSizeWithSafety) {
//			reply(new MessageBuilder(DiscordConsts.SAFETY_CHAR + response).build());
//		} else {
//			for (int i = 0; i < response.length(); i += maxSizeWithSafety) {
//				reply(new MessageBuilder().append(DiscordConsts.SAFETY_CHAR
//						+ response.substring(i, Math.min(i + maxSizeWithSafety, response.length()))).build());
//			}
//		}

		for (Message message : new MessageBuilder(response).buildAll(SplitPolicy.NEWLINE)) {
			reply(message);
		}
	}

	public abstract Message replyEmbed(MessageEmbed build);

	public abstract Message replyFile(byte[] data, String filename);

}
