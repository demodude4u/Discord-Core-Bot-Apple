package com.demod.dcba;

import java.util.Optional;

public final class DCBA {

	public static interface Builder {
		CommandBuilder addCommand(String command, CommandHandler handler);

		default CommandBuilder addCommand(String command, CommandHandler.SimpleResponse handler) {
			return addCommand(command, (CommandHandler) handler);
		}

		DiscordBot create();

		InfoBuilder setInfo(String botName);

		Builder withCommandPrefix(String commandPrefix);
	}

	private static class BuilderImpl implements Builder, CommandBuilder, InfoBuilder {
		DiscordBot bot = new DiscordBot();

		CommandDefinition command = null;

		private BuilderImpl() {
		}

		@Override
		public CommandBuilder addCommand(String name, CommandHandler handler) {
			if (command != null) {
				bot.addCommand(command);
				command = null;
			}
			command = new CommandDefinition(name, handler);
			return this;
		}

		@Override
		public DiscordBot create() {
			if (command != null) {
				bot.addCommand(command);
				command = null;
			}
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
		public CommandBuilder withHelp(String description) {
			command.setHelp(Optional.of(description));
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

	public static interface CommandBuilder extends Builder {
		CommandBuilder withHelp(String description);
	}

	public static interface InfoBuilder extends Builder {
		InfoBuilder withCredits(String group, String... names);

		InfoBuilder withSupport(String supportMessage);

		InfoBuilder withTechnology(String name, String description);

		InfoBuilder withTechnology(String name, String version, String description);

		InfoBuilder withVersion(String version);
	}

	public static Builder builder() {
		return new BuilderImpl();
	}

}
