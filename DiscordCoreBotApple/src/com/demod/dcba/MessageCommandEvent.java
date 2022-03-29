package com.demod.dcba;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;

public class MessageCommandEvent implements EventReply {

	private final MessageContextInteractionEvent event;
	private final InteractionHook hook;
	private final boolean ephemeral;

	private boolean replied = false;

	public MessageCommandEvent(MessageContextInteractionEvent event, InteractionHook hook, boolean ephemeral) {
		this.event = event;
		this.hook = hook;
		this.ephemeral = ephemeral;
	}

	public ChannelType getChannelType() {
		return event.getChannelType();
	}

	public String getCommandString() {
		return event.getCommandString();
	}

	public Guild getGuild() {
		return event.getGuild();
	}

	public MessageContextInteraction getInteraction() {
		return event.getInteraction();
	}

	public JDA getJDA() {
		return event.getJDA();
	}

	public Member getMember() {
		return event.getMember();
	}

	public Message getMessage() {
		return event.getInteraction().getTarget();
	}

	public MessageChannel getMessageChannel() {
		return event.getMessageChannel();
	}

	@Override
	public User getReplyPrivateUser() {
		return event.getUser();
	}

	public User getUser() {
		return event.getUser();
	}

	public boolean hasReplied() {
		return replied;
	}

	public boolean isFromType(ChannelType type) {
		return event.getChannel().getType() == type;
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
