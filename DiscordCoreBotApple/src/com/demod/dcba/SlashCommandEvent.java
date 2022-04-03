package com.demod.dcba;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class SlashCommandEvent extends ParamPayloadEvent implements EventReply {

	private final SlashCommandInteractionEvent event;
	private final CommandReporting reporting;
	private final InteractionHook hook;
	private final Interaction interaction;
	private final boolean ephemeral;

	private boolean replied = false;

	public SlashCommandEvent(SlashCommandInteractionEvent event, CommandReporting reporting, InteractionHook hook,
			boolean ephemeral) {
		super(event);
		this.event = event;
		this.reporting = reporting;
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

	@Override
	public User getReplyPrivateUser() {
		return event.getUser();
	}

	@Override
	public CommandReporting getReporting() {
		return reporting;
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

	@Override
	public Message reply(Message message) {
		replied = true;
		Message ret = hook.sendMessage(message).setEphemeral(ephemeral).complete();
		reporting.addReply(ret);
		return ret;
	}

	@Override
	public Message replyEmbed(MessageEmbed embed, MessageEmbed... embeds) {
		replied = true;
		Message ret = hook.sendMessageEmbeds(embed, embeds).setEphemeral(ephemeral).complete();
		reporting.addReply(ret);
		return ret;
	}

	@Override
	public Message replyFile(byte[] data, String filename) {
		replied = true;
		Message ret = hook.sendFile(data, filename).setEphemeral(ephemeral).complete();
		reporting.addReply(ret);
		return ret;
	}

}
