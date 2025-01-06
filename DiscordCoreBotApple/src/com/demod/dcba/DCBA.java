package com.demod.dcba;

import java.util.Optional;
import java.util.function.Function;

import com.demod.dcba.ReactionWatcher.SimpleWatcher;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public final class DCBA {

	public static interface Builder {
		Builder addButtonHandler(ButtonHandler handler);

		Builder addReactionWatcher(ReactionWatcher watcher);

		default Builder addReactionWatcher(ReactionWatcher.SimpleWatcher watcher) {
			return addReactionWatcher((ReactionWatcher) watcher);
		}

		SlashCommandBuilder addSlashCommand(String path, String description, SlashCommandHandler handler);

		SlashCommandBuilder addSlashCommand(String path, String description, SlashCommandHandler commandhandler,
				AutoCompleteHandler autoCompleteHandler);

		Builder addStringSelectHandler(StringSelectHandler handler);

		DiscordBot create();

		InfoBuilder setInfo(String botName);

		Builder withCommandPrefix(String commandPrefix);

		Builder withCustomSetup(Function<JDABuilder, JDABuilder> customSetup);
	}

	private static abstract class BuilderDeferred implements Builder {
		BuilderImpl builder;

		public BuilderDeferred(BuilderImpl builder) {
			this.builder = builder;
		}

		@Override
		public Builder addButtonHandler(ButtonHandler handler) {
			return builder.addButtonHandler(handler);
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
		public SlashCommandBuilder addSlashCommand(String path, String description, SlashCommandHandler commandhandler,
				AutoCompleteHandler autoCompleteHandler) {
			return builder.addSlashCommand(path, description, commandhandler, autoCompleteHandler);
		}

		@Override
		public Builder addStringSelectHandler(StringSelectHandler handler) {
			return builder.addStringSelectHandler(handler);
		}

		@Override
		public DiscordBot create() {
			return builder.create();
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

	}

	private static class BuilderImpl implements Builder, InfoBuilder {
		DiscordBot bot = new DiscordBot();

		SlashCommandBuilderImpl slashCommandBuilder = new SlashCommandBuilderImpl(this);

		private BuilderImpl() {
		}

		@Override
		public Builder addButtonHandler(ButtonHandler handler) {
			bot.setButtonHandler(Optional.of(handler));
			return this;
		}

		@Override
		public Builder addReactionWatcher(ReactionWatcher watcher) {
			bot.setReactionWatcher(Optional.of(watcher));
			return this;
		}

		@Override
		public SlashCommandBuilder addSlashCommand(String path, String description, SlashCommandHandler handler) {
			if (slashCommandBuilder.command != null) {
				bot.addCommand(slashCommandBuilder.command);
				slashCommandBuilder.command = null;
			}
			slashCommandBuilder.command = new SlashCommandDefinition(path, description, handler);
			return slashCommandBuilder;
		}

		@Override
		public SlashCommandBuilder addSlashCommand(String path, String description, SlashCommandHandler commandhandler,
				AutoCompleteHandler autoCompleteHandler) {
			if (slashCommandBuilder.command != null) {
				bot.addCommand(slashCommandBuilder.command);
				slashCommandBuilder.command = null;
			}
			slashCommandBuilder.command = new SlashCommandDefinition(path, description, commandhandler,
					autoCompleteHandler);
			return slashCommandBuilder;
		}

		@Override
		public Builder addStringSelectHandler(StringSelectHandler handler) {
			bot.setStringSelectHandler(Optional.of(handler));
			return this;
		}

		@Override
		public DiscordBot create() {
			if (slashCommandBuilder.command != null) {
				bot.addCommand(slashCommandBuilder.command);
				slashCommandBuilder.command = null;
			}
			bot.initialize();
			return bot;
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
		public InfoBuilder withCustomField(String label, String description) {
			bot.getInfo().addCustomField(label, description);
			return this;
		}

		@Override
		public Builder withCustomSetup(Function<JDABuilder, JDABuilder> customSetup) {
			bot.setCustomSetup(customSetup);
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

		InfoBuilder withCustomField(String label, String description);

		InfoBuilder withInvite(Permission... permissions);

		InfoBuilder withSupport(String supportMessage);

		InfoBuilder withTechnology(String name, String description);

		InfoBuilder withTechnology(String name, String version, String description);

		InfoBuilder withVersion(String version);
	}

	public static interface SlashCommandBuilder extends Builder {
		SlashCommandBuilder adminOnly();

		SlashCommandBuilder ephemeral();

		SlashCommandBuilder guildChannelOnly();

		SlashCommandBuilder privateChannelOnly();

		SlashCommandBuilder withAutoParam(OptionType type, String name, String description);

		SlashCommandBuilder withOptionalAutoParam(OptionType type, String name, String description);

		SlashCommandBuilder withOptionalParam(OptionType type, String name, String description);

		SlashCommandBuilder withoutReporting();

		SlashCommandBuilder withParam(OptionType type, String name, String description);
	}

	private static class SlashCommandBuilderImpl extends BuilderDeferred implements SlashCommandBuilder {

		SlashCommandDefinition command = null;

		public SlashCommandBuilderImpl(BuilderImpl builder) {
			super(builder);
		}

		@Override
		public SlashCommandBuilder adminOnly() {
			command.setRestriction(CommandRestriction.ADMIN_ONLY);
			return this;
		}

		@Override
		public SlashCommandBuilder ephemeral() {
			command.setRestriction(CommandRestriction.EPHEMERAL);
			return this;
		}

		@Override
		public SlashCommandBuilder guildChannelOnly() {
			command.setRestriction(CommandRestriction.GUILD_CHANNEL_ONLY);
			command.clearRestriction(CommandRestriction.PRIVATE_CHANNEL_ONLY);
			return this;
		}

		@Override
		public SlashCommandBuilder privateChannelOnly() {
			command.setRestriction(CommandRestriction.PRIVATE_CHANNEL_ONLY);
			command.clearRestriction(CommandRestriction.GUILD_CHANNEL_ONLY);
			return this;
		}

		@Override
		public SlashCommandBuilder withAutoParam(OptionType type, String name, String description) {
			command.addOption(new SlashCommandOptionDefinition(type, name, description, true, true));
			return this;
		}

		@Override
		public SlashCommandBuilder withOptionalAutoParam(OptionType type, String name, String description) {
			command.addOption(new SlashCommandOptionDefinition(type, name, description, false, true));
			return this;
		}

		@Override
		public SlashCommandBuilder withOptionalParam(OptionType type, String name, String description) {
			command.addOption(new SlashCommandOptionDefinition(type, name, description, false, false));
			return this;
		}

		@Override
		public SlashCommandBuilder withoutReporting() {
			command.setRestriction(CommandRestriction.NO_REPORTING);
			return this;
		}

		@Override
		public SlashCommandBuilder withParam(OptionType type, String name, String description) {
			command.addOption(new SlashCommandOptionDefinition(type, name, description, true, false));
			return this;
		}

	}

	public static Builder builder() {
		return new BuilderImpl();
	}

}
