package com.demod.dcba;

import java.util.Optional;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public abstract class CommandEvent {

	public abstract User getAuthor();

	public abstract MessageChannel getChannel();

	public abstract ChannelType getChannelType();

	public abstract Guild getGuild();

	public abstract JDA getJDA();

	public abstract Member getMember();

	public abstract String getParam(String name);

	public abstract boolean isFromType(ChannelType private1);

	public abstract Optional<String> optParam(String name);

	public abstract void reply(MessageEmbed build);

	public abstract void reply(String response);

}
