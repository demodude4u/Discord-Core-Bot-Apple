package com.demod.dcba;

import java.util.Optional;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class SlashInteractionCommandEvent extends CommandEvent {

	private final SlashCommandEvent event;
	private final InteractionHook hook;
	private final Interaction interaction;

	private boolean replied = false;

	public SlashInteractionCommandEvent(SlashCommandEvent event, InteractionHook hook) {
		this.event = event;
		this.hook = hook;
		this.interaction = hook.getInteraction();
	}

	@Override
	public User getAuthor() {
		return interaction.getUser();
	}

	@Override
	public MessageChannel getChannel() {
		return interaction.getMessageChannel();
	}

	@Override
	public ChannelType getChannelType() {
		return interaction.getChannelType();
	}

	@Override
	public Guild getGuild() {
		return interaction.getGuild();
	}

	@Override
	public JDA getJDA() {
		return interaction.getJDA();
	}

	@Override
	public Member getMember() {
		return interaction.getMember();
	}

	@Override
	public String getParam(String name) {
		return event.getOption(name).getAsString();
	}

	public OptionMapping getParamMapping(String name) {
		return event.getOption(name);
	}

	public boolean hasReplied() {
		return replied;
	}

	@Override
	public boolean isFromType(ChannelType type) {
		return interaction.getChannel().getType() == type;
	}

	@Override
	public Optional<String> optParam(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsString);
	}

	public Optional<OptionMapping> optParamMapping(String name) {
		return Optional.ofNullable(event.getOption(name));
	}

	@Override
	public void reply(MessageEmbed build) {
		hook.sendMessageEmbeds(build).setEphemeral(false).complete();
		replied = true;
	}

	@Override
	public void reply(String response) {
		// TODO determine max message size, use DiscordUtils to split multiple replies
		hook.sendMessage(response).setEphemeral(false).complete();
		replied = true;
	}

}
