package com.demod.dcba;

import java.io.InputStream;
import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

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
	public Message replyEmbed(List<MessageEmbed> embeds, List<List<ItemComponent>> actionRows) {
		replied = true;
		WebhookMessageCreateAction<Message> action = hook.sendMessageEmbeds(embeds);
		for (List<ItemComponent> actionRow : actionRows) {
			if (!actionRow.isEmpty()) {
				action = action.addActionRow(actionRow);
			}
		}
		Message ret = action.setEphemeral(ephemeral).complete();
		reporting.addReply(ret);
		return ret;
	}

	@Override
	public Message replyFile(InputStream data, String filename, List<List<ItemComponent>> actionRows) {
		replied = true;
		WebhookMessageCreateAction<Message> action = hook.sendFiles(FileUpload.fromData(data, filename));
		for (List<ItemComponent> actionRow : actionRows) {
			if (!actionRow.isEmpty()) {
				action = action.addActionRow(actionRow);
			}
		}
		Message ret = action.setEphemeral(ephemeral).complete();
		reporting.addReply(ret);
		return ret;
	}

	@Override
	public Message replyPrivateEmbed(MessageEmbed embed, MessageEmbed... embeds) {
		PrivateChannel privateChannel = getReplyPrivateUser().openPrivateChannel().complete();
		Message ret = privateChannel.sendMessageEmbeds(embed, embeds).complete();
		reporting.addReply(ret);
		return ret;
	}

	@Override
	public Message replyPrivateFile(byte[] data, String filename) {
		PrivateChannel privateChannel = getReplyPrivateUser().openPrivateChannel().complete();
		Message ret = privateChannel.sendFiles(FileUpload.fromData(data, filename)).complete();
		reporting.addReply(ret);
		return ret;
	}

}
