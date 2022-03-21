package com.demod.dcba;

import java.util.Optional;
import java.util.function.Function;

import com.demod.dcba.ReactionWatcher.SimpleWatcher;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public final class DCBA {

	public static interface Builder {
		MessageCommandBuilder addMessageCommand(String name, MessageCommandHandler handler);

		Builder addReactionWatcher(ReactionWatcher watcher);

		default Builder addReactionWatcher(ReactionWatcher.SimpleWatcher watcher) {
			return addReactionWatcher((ReactionWatcher) watcher);
		}

		SlashCommandBuilder addSlashCommand(String path, String description, SlashCommandHandler handler);

		Builder addTextWatcher(TextWatcher watcher);

		default Builder addTextWatcher(TextWatcher.SimpleWatcher watcher) {
			return addTextWatcher((TextWatcher) watcher);
		}

		DiscordBot create();

		Builder ignorePrivateChannels();

		InfoBuilder setInfo(String botName);

		Builder withCommandPrefix(String commandPrefix);

		Builder withCustomSetup(Function<JDABuilder, JDABuilder> customSetup);

		Builder withExceptionHandler(ExceptionHandler handler);
	}

	private static abstract class BuilderDeferred implements Builder {
		BuilderImpl builder;

		public BuilderDeferred(BuilderImpl builder) {
			this.builder = builder;
		}

		@Override
		public MessageCommandBuilder addMessageCommand(String name, MessageCommandHandler handler) {
			return builder.addMessageCommand(name, handler);
		}

		@Override
		public Builder addReactionWatcher(ReactionWatcher watcher) {
			return builder.addReactionWatcher(watcher);
		}

		@Override
		public Builder addReactionWatcher(SimpleWatcher watcher) {
			return builder.addReactionWatcher(watcher);
		}

		@Override
		public SlashCommandBuilder addSlashCommand(String path, String description, SlashCommandHandler handler) {
			return builder.addSlashCommand(path, description, handler);
		}

		@Override
		public Builder addTextWatcher(com.demod.dcba.TextWatcher.SimpleWatcher watcher) {
			return builder.addTextWatcher(watcher);
		}

		@Override
		public Builder addTextWatcher(TextWatcher watcher) {
			return builder.addTextWatcher(watcher);
		}

		@Override
		public DiscordBot create() {
			return builder.create();
		}

		@Override
		public Builder ignorePrivateChannels() {
			return builder.ignorePrivateChannels();
		}

		@Override
		public InfoBuilder setInfo(String botName) {
			return builder.setInfo(botName);
		}

		@Override
		public Builder withCommandPrefix(String commandPrefix) {
			return builder.withCommandPrefix(commandPrefix);
		}

		@Override
		public Builder withCustomSetup(Function<JDABuilder, JDABuilder> customSetup) {
			return builder.withCustomSetup(customSetup);
		}

		@Override
		public Builder withExceptionHandler(ExceptionHandler handler) {
			return builder.withExceptionHandler(handler);
		}

	}

	private static class BuilderImpl implements Builder, InfoBuilder {
		DiscordBot bot = new DiscordBot();

		SlashCommandBuilderImpl slashCommandBuilder = new SlashCommandBuilderImpl(this);
		MessageCommandBuilderImpl messageCommandBuilder = new MessageCommandBuilderImpl(this);

		private BuilderImpl() {
		}

		@Override
		public MessageCommandBuilder addMessageCommand(String name, MessageCommandHandler handler) {
			if (messageCommandBuilder.command != null) {
				bot.addCommand(messageCommandBuilder.command);
				messageCommandBuilder.command = null;
			}
			messageCommandBuilder.command = new MessageCommandDefinition(name, handler);
			return messageCommandBuilder;
		}

		@Override
		public Builder addReactionWatcher(ReactionWatcher watcher) {
			bot.setReactionWatcher(Optional.of(watcher));
			return this;
		}

		@Override
		public SlashCommandBuilder addSlashCommand(String name, String description, SlashCommandHandler handler) {
			if (slashCommandBuilder.command != null) {
				bot.addCommand(slashCommandBuilder.command);
				slashCommandBuilder.command = null;
			}
			slashCommandBuilder.command = new SlashCommandDefinition(name, description, handler);
			return slashCommandBuilder;
		}

		@Override
		public Builder addTextWatcher(TextWatcher watcher) {
			bot.setTextWatcher(Optional.of(watcher));
			return this;
		}

		@Override
		public DiscordBot create() {
			if (slashCommandBuilder.command != null) {
				bot.addCommand(slashCommandBuilder.command);
				slashCommandBuilder.command = null;
			}
			if (messageCommandBuilder.command != null) {
				bot.addCommand(messageCommandBuilder.command);
				messageCommandBuilder.command = null;
			}
			bot.initialize();
			return bot;
		}

		@Override
		public Builder ignorePrivateChannels() {
			bot.setIgnorePrivateChannels(true);
			return this;
		}

		@Override
		public InfoBuilder setInfo(String botName) {
			bot.getInfo().setBotName(Optional.of(botName));
			return this;
		}

		@Override
		public Builder withCommandPrefix(String commandPrefix) {
			bot.setCommandPrefix(Optional.of(commandPrefix));
			return this;
		}

		@Override
		public InfoBuilder withCredits(String group, String... names) {
			bot.getInfo().addCredits(group, names);
			return this;
		}

		@Override
		public Builder withCustomSetup(Function<JDABuilder, JDABuilder> customSetup) {
			bot.setCustomSetup(customSetup);
			return this;
		}

		@Override
		public Builder withExceptionHandler(ExceptionHandler handler) {
			bot.setExceptionHandler(handler);
			return this;
		}

		@Override
		public InfoBuilder withInvite(Permission... permissions) {
			bot.getInfo().setAllowInvite(true);
			bot.getInfo().setInvitePermissions(permissions);
			return this;
		}

		@Override
		public InfoBuilder withSupport(String supportMessage) {
			bot.getInfo().setSupport(Optional.of(supportMessage));
			return this;
		}

		@Override
		public InfoBuilder withTechnology(String name, String description) {
			bot.getInfo().addTechnology(name, Optional.empty(), description);
			return this;
		}

		@Override
		public InfoBuilder withTechnology(String name, String version, String description) {
			bot.getInfo().addTechnology(name, Optional.of(version), description);
			return this;
		}

		@Override
		public InfoBuilder withVersion(String version) {
			bot.getInfo().setVersion(Optional.of(version));
			return this;
		}
	}

	public static interface InfoBuilder extends Builder {
		InfoBuilder withCredits(String group, String... names);

		InfoBuilder withInvite(Permission... permissions);

		InfoBuilder withSupport(String supportMessage);

		InfoBuilder withTechnology(String name, String description);

		InfoBuilder withTechnology(String name, String version, String description);

		InfoBuilder withVersion(String version);
	}

	public static interface MessageCommandBuilder extends Builder {
		MessageCommandBuilder adminOnly();

		MessageCommandBuilder ephemeral();

		MessageCommandBuilder guildChannelOnly();

		MessageCommandBuilder privateChannelOnly();
	}

	private static class MessageCommandBuilderImpl extends BuilderDeferred implements MessageCommandBuilder {

		MessageCommandDefinition command = null;

		public MessageCommandBuilderImpl(BuilderImpl builder) {
			super(builder);
		}

		@Override
		public MessageCommandBuilder adminOnly() {
			command.setRestriction(MessageCommandDefinition.Restriction.ADMIN_ONLY);
			return this;
		}

		@Override
		public MessageCommandBuilder ephemeral() {
			command.setRestriction(MessageCommandDefinition.Restriction.EPHEMERAL);
			return this;
		}

		@Override
		public MessageCommandBuilder guildChannelOnly() {
			command.setRestriction(MessageCommandDefinition.Restriction.GUILD_CHANNEL_ONLY);
			command.clearRestriction(MessageCommandDefinition.Restriction.PRIVATE_CHANNEL_ONLY);
			return this;
		}

		@Override
		public MessageCommandBuilder privateChannelOnly() {
			command.setRestriction(MessageCommandDefinition.Restriction.PRIVATE_CHANNEL_ONLY);
			command.clearRestriction(MessageCommandDefinition.Restriction.GUILD_CHANNEL_ONLY);
			return this;
		}

	}

	public static interface SlashCommandBuilder extends Builder {
		SlashCommandBuilder adminOnly();

		SlashCommandBuilder ephemeral();

		SlashCommandBuilder guildChannelOnly();

		SlashCommandBuilder privateChannelOnly();

		SlashCommandBuilder withLegacyWarning(String... names);

		SlashCommandBuilder withOptionalParam(OptionType type, String name, String description);

		SlashCommandBuilder withParam(OptionType type, String name, String description);
	}

	private static class SlashCommandBuilderImpl extends BuilderDeferred implements SlashCommandBuilder {

		SlashCommandDefinition command = null;

		public SlashCommandBuilderImpl(BuilderImpl builder) {
			super(builder);
		}

		@Override
		public SlashCommandBuilder adminOnly() {
			command.setRestriction(SlashCommandDefinition.Restriction.ADMIN_ONLY);
			return this;
		}

		@Override
		public SlashCommandBuilder ephemeral() {
			command.setRestriction(SlashCommandDefinition.Restriction.EPHEMERAL);
			return this;
		}

		@Override
		public SlashCommandBuilder guildChannelOnly() {
			command.setRestriction(SlashCommandDefinition.Restriction.GUILD_CHANNEL_ONLY);
			command.clearRestriction(SlashCommandDefinition.Restriction.PRIVATE_CHANNEL_ONLY);
			return this;
		}

		@Override
		public SlashCommandBuilder privateChannelOnly() {
			command.setRestriction(SlashCommandDefinition.Restriction.PRIVATE_CHANNEL_ONLY);
			command.clearRestriction(SlashCommandDefinition.Restriction.GUILD_CHANNEL_ONLY);
			return this;
		}

		@Override
		public SlashCommandBuilder withLegacyWarning(String... names) {
			for (String name : names) {
				command.addLegacy(name);
			}
			return this;
		}

		@Override
		public SlashCommandBuilder withOptionalParam(OptionType type, String name, String description) {
			command.addOption(new SlashCommandOptionDefinition(type, name, description, false));
			return this;
		}

		@Override
		public SlashCommandBuilder withParam(OptionType type, String name, String description) {
			command.addOption(new SlashCommandOptionDefinition(type, name, description, true));
			return this;
		}

	}

	public static Builder builder() {
		return new BuilderImpl();
	}

}
