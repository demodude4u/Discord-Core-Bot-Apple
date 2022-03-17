package com.demod.dcba;

import java.util.Map;
import java.util.Optional;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MessageCommandEvent extends CommandEvent {

	private final MessageReceivedEvent event;
	private final Map<String, String> options;

	public MessageCommandEvent(MessageReceivedEvent event, Map<String, String> options) {
		this.event = event;
		this.options = options;
	}

	@Override
	public User getAuthor() {
		return event.getAuthor();
	}

	@Override
	public MessageChannel getChannel() {
		return event.getChannel();
	}

	@Override
	public ChannelType getChannelType() {
		return event.getChannelType();
	}

	@Override
	public Guild getGuild() {
		return event.getGuild();
	}

	@Override
	public JDA getJDA() {
		return event.getJDA();
	}

	@Override
	public Member getMember() {
		return event.getMember();
	}

	@Override
	public String getParam(String name) {
		return options.get(name);
	}

	@Override
	public boolean isFromType(ChannelType private1) {
		return event.isFromType(private1);
	}

	@Override
	public Optional<String> optParam(String name) {
		return Optional.ofNullable(options.get(name));
	}

	@Override
	public void reply(MessageEmbed build) {
		event.getChannel().sendMessageEmbeds(build).complete();
	}

	@Override
	public void reply(String response) {
		DiscordUtils.replyTo(event.getChannel(), response);
	}

}
