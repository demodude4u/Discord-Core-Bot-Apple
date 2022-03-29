package com.demod.dcba;

import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class SlashCommandEvent implements EventReply {

	private final SlashCommandInteractionEvent event;
	private final InteractionHook hook;
	private final Interaction interaction;
	private final boolean ephemeral;

	private boolean replied = false;

	public SlashCommandEvent(SlashCommandInteractionEvent event, InteractionHook hook, boolean ephemeral) {
		this.event = event;
		this.hook = hook;
		this.ephemeral = ephemeral;
		this.interaction = hook.getInteraction();
	}

	public Attachment getAttachment(String name) {
		return event.getOption(name).getAsAttachment();
	}

	public ChannelType getChannelType() {
		return interaction.getChannelType();
	}

	public String getCommandString() {
		return event.getCommandString();
	}

	public Guild getGuild() {
		return interaction.getGuild();
	}

	public JDA getJDA() {
		return interaction.getJDA();
	}

	public Member getMember() {
		return interaction.getMember();
	}

	public MessageChannel getMessageChannel() {
		return interaction.getMessageChannel();
	}

	public boolean getParamBoolean(String name) {
		return event.getOption(name).getAsBoolean();
	}

	public double getParamDouble(String name) {
		return event.getOption(name).getAsDouble();
	}

	public GuildChannel getParamGuildChannel(String name) {
		return event.getOption(name).getAsGuildChannel();
	}

	public long getParamLong(String name) {
		return event.getOption(name).getAsLong();
	}

	public Member getParamMember(String name) {
		return event.getOption(name).getAsMember();
	}

	public IMentionable getParamMentionable(String name) {
		return event.getOption(name).getAsMentionable();
	}

	public MessageChannel getParamMessageChannel(String name) {
		return event.getOption(name).getAsMessageChannel();
	}

	public Role getParamRole(String name) {
		return event.getOption(name).getAsRole();
	}

	public List<OptionMapping> getParams() {
		return event.getOptions();
	}

	public String getParamString(String name) {
		return event.getOption(name).getAsString();
	}

	public User getParamUser(String name) {
		return event.getOption(name).getAsUser();
	}

	@Override
	public User getReplyPrivateUser() {
		return event.getUser();
	}

	public User getUser() {
		return interaction.getUser();
	}

	public boolean hasReplied() {
		return replied;
	}

	public boolean isFromType(ChannelType type) {
		return interaction.getChannel().getType() == type;
	}

	public Optional<Attachment> optParamAttachment(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsAttachment);
	}

	public Optional<Boolean> optParamBoolean(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsBoolean);
	}

	public Optional<Double> optParamDouble(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsDouble);
	}

	public Optional<GuildChannel> optParamGuildChannel(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsGuildChannel);
	}

	public Optional<Long> optParamLong(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsLong);
	}

	public Optional<Member> optParamMember(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsMember);
	}

	public Optional<IMentionable> optParamMentionable(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsMentionable);
	}

	public Optional<MessageChannel> optParamMessageChannel(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsMessageChannel);
	}

	public Optional<Role> optParamRole(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsRole);
	}

	public Optional<String> optParamString(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsString);
	}

	public Optional<User> optParamUser(String name) {
		return Optional.ofNullable(event.getOption(name)).map(OptionMapping::getAsUser);
	}

	@Override
	public Message reply(Message message) {
		replied = true;
		return hook.sendMessage(message).setEphemeral(ephemeral).complete();
	}

	@Override
	public Message replyEmbed(MessageEmbed embed, MessageEmbed... embeds) {
		replied = true;
		return hook.sendMessageEmbeds(embed, embeds).setEphemeral(ephemeral).complete();
	}

	@Override
	public Message replyFile(byte[] data, String filename) {
		replied = true;
		return hook.sendFile(data, filename).setEphemeral(ephemeral).complete();
	}

}
