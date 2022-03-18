package com.demod.dcba;

import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.MessageBuilder.SplitPolicy;
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

public class CommandEvent {

	private final SlashCommandInteractionEvent event;
	private final InteractionHook hook;
	private final Interaction interaction;

	private boolean replied = false;

	public CommandEvent(SlashCommandInteractionEvent event, InteractionHook hook) {
		this.event = event;
		this.hook = hook;
		this.interaction = hook.getInteraction();
	}

	public Attachment getAttachment(String name) {
		return event.getOption(name).getAsAttachment();
	}

	public User getAuthor() {
		return interaction.getUser();
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

	public Message reply(Message message) {
		replied = true;
		return hook.sendMessage(message).setEphemeral(false).complete();
	}

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

	public Message replyEmbed(MessageEmbed build) {
		replied = true;
		return hook.sendMessageEmbeds(build).setEphemeral(false).complete();
	}

	public Message replyFile(byte[] data, String filename) {
		replied = true;
		return hook.sendFile(data, filename).setEphemeral(false).complete();
	}

}
