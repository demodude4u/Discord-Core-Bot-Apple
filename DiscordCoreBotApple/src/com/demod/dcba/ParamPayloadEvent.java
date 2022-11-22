package com.demod.dcba;

import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public abstract class ParamPayloadEvent {

	private final CommandInteractionPayload payload;

	protected ParamPayloadEvent(CommandInteractionPayload payload) {
		this.payload = payload;
	}

	public boolean getParamBoolean(String name) {
		return payload.getOption(name).getAsBoolean();
	}

	public GuildChannelUnion getParamChannel(String name) {
		return payload.getOption(name).getAsChannel();
	}

	public double getParamDouble(String name) {
		return payload.getOption(name).getAsDouble();
	}

	public long getParamLong(String name) {
		return payload.getOption(name).getAsLong();
	}

	public Member getParamMember(String name) {
		return payload.getOption(name).getAsMember();
	}

	public IMentionable getParamMentionable(String name) {
		return payload.getOption(name).getAsMentionable();
	}

	public Role getParamRole(String name) {
		return payload.getOption(name).getAsRole();
	}

	public List<OptionMapping> getParams() {
		return payload.getOptions();
	}

	public String getParamString(String name) {
		return payload.getOption(name).getAsString();
	}

	public User getParamUser(String name) {
		return payload.getOption(name).getAsUser();
	}

	public Optional<Attachment> optParamAttachment(String name) {
		return Optional.ofNullable(payload.getOption(name)).map(OptionMapping::getAsAttachment);
	}

	public Optional<Boolean> optParamBoolean(String name) {
		return Optional.ofNullable(payload.getOption(name)).map(OptionMapping::getAsBoolean);
	}

	public Optional<GuildChannelUnion> optParamChannel(String name) {
		return Optional.ofNullable(payload.getOption(name)).map(OptionMapping::getAsChannel);
	}

	public Optional<Double> optParamDouble(String name) {
		try {
			return Optional.ofNullable(payload.getOption(name)).map(OptionMapping::getAsDouble);
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	public Optional<Long> optParamLong(String name) {
		try {
			return Optional.ofNullable(payload.getOption(name)).map(OptionMapping::getAsLong);
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	public Optional<Member> optParamMember(String name) {
		return Optional.ofNullable(payload.getOption(name)).map(OptionMapping::getAsMember);
	}

	public Optional<IMentionable> optParamMentionable(String name) {
		return Optional.ofNullable(payload.getOption(name)).map(OptionMapping::getAsMentionable);
	}

	public Optional<Role> optParamRole(String name) {
		return Optional.ofNullable(payload.getOption(name)).map(OptionMapping::getAsRole);
	}

	public Optional<String> optParamString(String name) {
		return Optional.ofNullable(payload.getOption(name)).map(OptionMapping::getAsString);
	}

	public Optional<User> optParamUser(String name) {
		return Optional.ofNullable(payload.getOption(name)).map(OptionMapping::getAsUser);
	}

}
