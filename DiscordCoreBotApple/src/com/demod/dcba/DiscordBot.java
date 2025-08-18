package com.demod.dcba;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractIdleService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.SplitUtil;
import net.dv8tion.jda.api.utils.SplitUtil.Strategy;

public class DiscordBot extends AbstractIdleService {

	private static final String COMMAND_INFO = "info";
	private static final String COMMAND_FEEDBACK = "feedback";

	private final Map<String, SlashCommandDefinition> commandSlash = new LinkedHashMap<>();

	private final InfoDefinition info = new InfoDefinition();

	private ExecutorService commandService = null;

	private Optional<ReactionWatcher> reactionWatcher = Optional.empty();
	private Optional<ButtonHandler> buttonHandler = Optional.empty();
	private Optional<StringSelectHandler> stringSelectHandler = Optional.empty();
	private Optional<MessageContextHandler> messageContextHandler = Optional.empty();
	private Optional<PrivateMessageHandler> privateMessageHandler = Optional.empty();
	private String messageContextLabel = null;

	private final JSONObject configJson;

	private JDA jda;

	private final LocalDateTime botStarted = LocalDateTime.now();

	private Function<JDABuilder, JDABuilder> customSetup = null;

	private Optional<String> reportingUserID = Optional.empty();
	private Optional<String> reportingChannelID = Optional.empty();

	private boolean async;
	private final ConcurrentHashMap<String, Future<?>> activeUsers = new ConcurrentHashMap<>();

	DiscordBot() {
		configJson = loadConfig();
	}

	public void addCommand(SlashCommandDefinition command) {
		commandSlash.put(command.getPath(), command);
	}

	// Hold my beer
	@SuppressWarnings("unchecked")
	private void buildUpdateCommands(CommandListUpdateAction updateCommands) {
		Map<String, Object> root = new LinkedHashMap<>();

		for (SlashCommandDefinition command : commandSlash.values()) {
			String[] pathSplit = command.getPath().split("/");
			Map<String, Object> group = root;
			for (int i = 0; i < pathSplit.length; i++) {
				String name = pathSplit[i];
				if (i == pathSplit.length - 1) {
					group.put(name, command);
				} else {
					Object subGroup = group.get(name);
					if (subGroup == null) {
						group.put(name, subGroup = new LinkedHashMap<String, Object>());
					}
					group = (Map<String, Object>) subGroup;
				}
			}
		}

		for (Entry<String, Object> rootEntry : root.entrySet()) {
			SlashCommandData commandData;
			if (rootEntry.getValue() instanceof SlashCommandDefinition) {
				SlashCommandDefinition commandDefinition = (SlashCommandDefinition) rootEntry.getValue();
				commandData = Commands.slash(rootEntry.getKey(), commandDefinition.getDescription());
				for (SlashCommandOptionDefinition option : commandDefinition.getOptions()) {
					commandData = commandData.addOption(option.getType(), option.getName(), option.getDescription(),
							option.isRequired(), option.isAutoComplete());
				}
			} else {
				Map<String, Object> sub = (Map<String, Object>) rootEntry.getValue();
				commandData = Commands.slash(rootEntry.getKey(),
						sub.keySet().stream().collect(Collectors.joining(", ")));
				for (Entry<String, Object> subEntry : sub.entrySet()) {
					if (subEntry.getValue() instanceof SlashCommandDefinition) {
						SlashCommandDefinition commandDefinition = (SlashCommandDefinition) subEntry.getValue();
						SubcommandData subcommandData = new SubcommandData(subEntry.getKey(),
								commandDefinition.getDescription());
						for (SlashCommandOptionDefinition option : commandDefinition.getOptions()) {
							subcommandData = subcommandData.addOption(option.getType(), option.getName(),
									option.getDescription(), option.isRequired(), option.isAutoComplete());
						}
						commandData = commandData.addSubcommands(subcommandData);
					} else {
						Map<String, SlashCommandDefinition> subSub = (Map<String, SlashCommandDefinition>) subEntry
								.getValue();
						SubcommandGroupData subcommandGroupData = new SubcommandGroupData(subEntry.getKey(),
								subSub.keySet().stream().collect(Collectors.joining(", ")));
						for (Entry<String, SlashCommandDefinition> subSubEntry : subSub.entrySet()) {
							SubcommandData subcommandData = new SubcommandData(subSubEntry.getKey(),
									subSubEntry.getValue().getDescription());
							for (SlashCommandOptionDefinition option : subSubEntry.getValue().getOptions()) {
								subcommandData = subcommandData.addOption(option.getType(), option.getName(),
										option.getDescription(), option.isRequired(), option.isAutoComplete());
							}
							subcommandGroupData = subcommandGroupData.addSubcommands(subcommandData);
						}
						commandData = commandData.addSubcommandGroups(subcommandGroupData);
					}
				}
			}
			updateCommands = updateCommands.addCommands(commandData);
		}

		if (messageContextHandler.isPresent()) {
			CommandData commandData = Commands.message(messageContextLabel);
			updateCommands = updateCommands.addCommands(commandData);
		}
	}

	private SlashCommandDefinition createCommandFeedback() {
		SlashCommandDefinition command = new SlashCommandDefinition(COMMAND_FEEDBACK,
				"Send feedback or ideas to my developer!", new SlashCommandHandler() {
					@Override
					public void handleCommand(SlashCommandEvent event) throws Exception {
						event.getReporting().setAttention();
						event.reply("Thank you for your feedback!");
					}
				});
		command.addOption(
				new SlashCommandOptionDefinition(OptionType.STRING, "feedback", "The feedback details.", true, false));
		command.addOption(new SlashCommandOptionDefinition(OptionType.ATTACHMENT, "attachment",
				"Any file along with the feedback.", false, false));
		command.setRestriction(CommandRestriction.EPHEMERAL);
		return command;
	}

	private SlashCommandDefinition createCommandInfo() {
		return new SlashCommandDefinition(COMMAND_INFO, "Shows information about this bot.", new SlashCommandHandler() {
			@Override
			public void handleCommand(SlashCommandEvent event) throws Exception {
				EmbedBuilder builder = new EmbedBuilder();
				info.getSupportMessage().ifPresent(s -> builder.addField("Support", s, false));
				info.getBotName().ifPresent(n -> builder.addField("Bot Name", n, true));
				info.getVersion().ifPresent(v -> builder.addField("Bot Version", v, true));
				if (info.isAllowInvite()) {
					builder.addField("Server Invite", "[Link](" + jda.getInviteUrl(info.getInvitePermissions()) + ")",
							true);
				}
				builder.addField("Technologies", info.getTechnologies().stream().collect(Collectors.joining("\n")),
						false);
				for (String group : info.getCredits().keySet()) {
					builder.addField(group, info.getCredits().get(group).stream().collect(Collectors.joining("\n")),
							false);
				}

				int guildCount = jda.getGuilds().size();
				String uptimeFormatted = getDurationFormatted(botStarted, LocalDateTime.now());
				long ping = jda.getGatewayPing();
				builder.addField("Total Servers", guildCount + " servers", true);
				builder.addField("Uptime", uptimeFormatted, true);
				builder.addField("Ping to Discord", ping + " ms", true);
				for (Entry<String, String> entry : info.getCustomFields()) {
					builder.addField(entry.getKey(), entry.getValue(), true);
				}

				event.replyEmbed(builder.build());
			}
		});
	}

	private CommandReporting createReporting(CommandInteractionPayload event) {
		String author;
		if (event.getChannelType() == ChannelType.PRIVATE) {
			author = event.getUser().getName();
		} else {
			author = event.getGuild().getName() + " / #" + event.getMessageChannel().getName() + " / "
					+ event.getUser().getName();
		}

		String authorIconURL = event.getUser().getEffectiveAvatarUrl();

		String command = event.getCommandString();
		for (OptionMapping optionMapping : event.getOptions()) {
			if (optionMapping.getType() == OptionType.ATTACHMENT) {
				command += " " + optionMapping.getAsAttachment().getUrl();
			}
		}

		return new CommandReporting(author, authorIconURL, command, Instant.now());
	}

	private CommandReporting createReporting(MessageReceivedEvent event) {
		String author;
		if (event.getChannelType() == ChannelType.PRIVATE) {
			author = event.getAuthor().getName();
		} else {
			author = event.getGuild().getName() + " / #" + event.getChannel().getName() + " / "
					+ event.getAuthor().getName();
		}

		String authorIconURL = event.getAuthor().getEffectiveAvatarUrl();
		Message message = event.getMessage();
		String command = message.getContentStripped();
		for (Attachment attachment : message.getAttachments()) {
			command += " " + attachment.getUrl();
		}

		return new CommandReporting(author, authorIconURL, command, Instant.now());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CommandReporting createReporting(GenericComponentInteractionCreateEvent event) {
		String author;
		if (event.getChannelType() == ChannelType.PRIVATE) {
			author = event.getUser().getName();
		} else {
			author = event.getGuild().getName() + " / #" + event.getMessageChannel().getName() + " / "
					+ event.getUser().getName();
		}

		String authorIconURL = event.getUser().getEffectiveAvatarUrl();

		String command = event.getComponentId();

		if (event instanceof GenericSelectMenuInteractionEvent) {
			List values = ((GenericSelectMenuInteractionEvent) event).getValues();
			command += "[" + values.stream().map(Object::toString).collect(Collectors.joining(",")) + "]";
		}

		return new CommandReporting(author, authorIconURL, command, Instant.now());
	}

	private String getDurationFormatted(LocalDateTime then, LocalDateTime now) {
		List<ChronoUnit> units = Arrays.asList(//
				ChronoUnit.YEARS, //
				ChronoUnit.MONTHS, //
				ChronoUnit.DAYS, //
				ChronoUnit.HOURS, //
				ChronoUnit.DAYS, //
				ChronoUnit.MINUTES, //
				ChronoUnit.SECONDS);

		List<String> result = new ArrayList<>();
		LocalDateTime accumulator = then;
		for (ChronoUnit chronoUnit : units) {
			long duration = chronoUnit.between(accumulator, now);
			if (duration > 0) {
				String label = chronoUnit.name().toLowerCase().substring(0, chronoUnit.name().length() - 1);
				result.add(duration + " " + label + (duration > 1 ? "s" : ""));
				accumulator = accumulator.plus(chronoUnit.getDuration().multipliedBy(duration));
			}
		}

		return result.stream().collect(Collectors.joining(", "));
	}

	public InfoDefinition getInfo() {
		return info;
	}

	public JDA getJDA() {
		return jda;
	}

	void initialize() {
		if (async) {
			commandService = Executors.newWorkStealingPool();
		} else {
			commandService = Executors.newSingleThreadExecutor();
		}

		if (!commandSlash.containsKey(COMMAND_INFO)) {
			addCommand(createCommandInfo());
		}
		if (!commandSlash.containsKey(COMMAND_FEEDBACK)) {
			addCommand(createCommandFeedback());
		}
	}

	private JSONObject loadConfig() {
		try (Scanner scanner = new Scanner(new FileInputStream("config.json"), "UTF-8")) {
			scanner.useDelimiter("\\A");
			return new JSONObject(scanner.next()).getJSONObject("discord");
		} catch (JSONException | IOException e) {
			e.printStackTrace();
			System.err.println("################################");
			System.err.println("Missing or bad config.json file!");
			System.err.println("################################");
			System.exit(0);
			return null;
		}
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public void setButtonHandler(Optional<ButtonHandler> buttonHandler) {
		this.buttonHandler = buttonHandler;
	}

	public void setCustomSetup(Function<JDABuilder, JDABuilder> customSetup) {
		this.customSetup = customSetup;
	}

	public void setMessageContextHandler(Optional<MessageContextHandler> messageContextHandler) {
		this.messageContextHandler = messageContextHandler;
	}

	public void setMessageContextLabel(String messageContextLabel) {
		this.messageContextLabel = messageContextLabel;
	}

	public void setReactionWatcher(Optional<ReactionWatcher> reactionWatcher) {
		this.reactionWatcher = reactionWatcher;
	}

	public void setStringSelectHandler(Optional<StringSelectHandler> stringSelectHandler) {
		this.stringSelectHandler = stringSelectHandler;
	}

	public void setPrivateMessageHandler(Optional<PrivateMessageHandler> privateMessageHandler) {
		this.privateMessageHandler = privateMessageHandler;
	}

	@Override
	protected void shutDown() {
		jda.shutdown();
	}

	@Override
	protected void startUp() throws Exception {
		info.addTechnology("[DCBA](https://github.com/demodude4u/Discord-Core-Bot-Apple)", Optional.empty(),
				"Discord Core Bot Apple");
		info.addTechnology("[JDA](https://github.com/discord-jda/JDA)", Optional.of("5.2.1"), "Java Discord API");

		JDABuilder builder = JDABuilder.createDefault(configJson.getString("bot_token"))//
				.setEnableShutdownHook(false)//
				.addEventListeners(new ListenerAdapter() {
					@Override
					public void onButtonInteraction(ButtonInteractionEvent event) {
						if (buttonHandler.isPresent()) {
							Future<?> future = activeUsers.get(event.getUser().getId());
							if (future != null && !future.isDone()) {
								event.reply("I am already processing your selection, please wait...").setEphemeral(true)
										.complete();
								return;
							}
							future = commandService.submit(() -> {
								CommandReporting reporting = createReporting(event);
								reporting.addField(new Field("Context",
										"[Message](" + event.getMessage().getJumpUrl() + ")", true));
								try {
									buttonHandler.get().onButtonInteraction(event, reporting);
								} catch (Exception e) {
									e.printStackTrace();
									reporting.addException(e);
								} finally {
									submitReport(reporting);
									activeUsers.remove(event.getUser().getId());
								}
							});
							activeUsers.put(event.getUser().getId(), future);
						}
					}

					@Override
					public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
						SlashCommandDefinition commandDefinition = commandSlash
								.get(event.getFullCommandName().replace(' ', '/'));
						Optional<AutoCompleteHandler> autoCompleteHandler = commandDefinition.getAutoCompleteHandler();
						if (autoCompleteHandler.isPresent()) {
							commandService.submit(() -> {
								AutoCompleteEvent autoCompleteEvent = new AutoCompleteEvent(event);
								autoCompleteHandler.get().handleAutoComplete(autoCompleteEvent);
							});
						}
					}

					@Override
					public void onMessageContextInteraction(MessageContextInteractionEvent event) {
						if (messageContextHandler.isPresent()) {
							commandService.submit(() -> {
								CommandReporting reporting = createReporting(event);
								reporting.addField(new Field("Context",
										"[Message](" + event.getTarget().getJumpUrl() + ")", true));
								try {
									messageContextHandler.get().onMessageContextInteraction(event, reporting);
								} catch (Exception e) {
									e.printStackTrace();
									reporting.addException(e);
								} finally {
									submitReport(reporting);
								}
							});
						}
					}

					@Override
					public void onMessageReactionAdd(MessageReactionAddEvent event) {
						if (reactionWatcher.isPresent()) {
							reactionWatcher.get().seenReaction(event);
						}
					}

					@Override
					public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
						if (reactionWatcher.isPresent()) {
							reactionWatcher.get().seenReactionRemoved(event);
						}
					}

					@Override
					public void onMessageReactionRemoveAll(MessageReactionRemoveAllEvent event) {
						if (reactionWatcher.isPresent()) {
							reactionWatcher.get().seenAllReactionRemoved(event);
						}
					}

					@Override
					public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
						SlashCommandDefinition commandDefinition = commandSlash
								.get(event.getFullCommandName().replace(' ', '/'));
						boolean ephemeral = commandDefinition.hasRestriction(CommandRestriction.EPHEMERAL);

						CommandReporting reporting = createReporting(event);
						InteractionHook hook = event.deferReply(ephemeral).complete();
						SlashCommandEvent commandEvent = new SlashCommandEvent(event, reporting, hook, ephemeral);

						commandService.submit(() -> {
							try {
								commandDefinition.getHandler().handleCommand(commandEvent);
							} catch (Exception e) {
								System.err.println("Uncaught Exception!");
								e.printStackTrace();
								reporting.addException(e);
							} finally {
								if (!commandDefinition.hasRestriction(CommandRestriction.NO_REPORTING)) {
									submitReport(reporting);
								}

								if (!reporting.getExceptionsWithBlame().isEmpty()) {
									commandEvent.replyEmbed(new EmbedBuilder().setColor(Color.red)
											.appendDescription("Sorry, there was a problem completing your request.\n"
													+ reporting.getExceptionsWithBlame().stream()
															.map(e -> "`" + e.getException().getMessage() + "`")
															.distinct().collect(Collectors.joining("\n")))
											.build());
								}

								if (!commandEvent.hasReplied()) {
									hook.deleteOriginal().complete();
								}
							}
						});
					}

					@Override
					public void onStringSelectInteraction(StringSelectInteractionEvent event) {
						if (stringSelectHandler.isPresent()) {
							Future<?> future = activeUsers.get(event.getUser().getId());
							if (future != null && !future.isDone()) {
								event.reply("I am already processing your selection, please wait...").setEphemeral(true)
										.complete();
								return;
							}
							future = commandService.submit(() -> {
								CommandReporting reporting = createReporting(event);
								reporting.addField(new Field("Context",
										"[Message](" + event.getMessage().getJumpUrl() + ")", true));
								try {
									stringSelectHandler.get().onStringSelectInteraction(event, reporting);
								} catch (Exception e) {
									e.printStackTrace();
									reporting.addException(e);
								} finally {
									submitReport(reporting);
									activeUsers.remove(event.getUser().getId());
								}
							});
							activeUsers.put(event.getUser().getId(), future);
						}
					}

					@Override
					public void onMessageReceived(MessageReceivedEvent event) {
						if (event.getChannelType() != ChannelType.PRIVATE) {
							return;
						}
						if (event.getAuthor().isBot()) {
							return;
						}
						if (privateMessageHandler.isPresent()) {
							commandService.submit(() -> {
								CommandReporting reporting = createReporting(event);
								try {
									privateMessageHandler.get().onPrivateMessageReceived(event, reporting);
								} catch (Exception e) {
									e.printStackTrace();
									reporting.addException(e);
								} finally {
									submitReport(reporting);
								}
							});
						}
					}
				});
		if (customSetup != null) {
			builder = customSetup.apply(builder);
		}
		jda = builder.build().awaitReady();
		jda.setRequiredScopes("bot", "applications.commands");

		reportingUserID = Optional.ofNullable(configJson.optString("reporting_user_id", null));
		reportingChannelID = Optional.ofNullable(configJson.optString("reporting_channel_id", null));

		if (configJson.has("debug_guild_commands")) {
			String guildId = configJson.getString("debug_guild_commands");
			Guild guild = jda.getGuildById(guildId);
			CommandListUpdateAction updateCommands = guild.updateCommands();
			buildUpdateCommands(updateCommands);
			updateCommands.queue();
		}

		CommandListUpdateAction updateCommands = jda.updateCommands();
		buildUpdateCommands(updateCommands);
		updateCommands.queue();
	}

	public synchronized void submitReport(CommandReporting reporting) {
		reporting.getExceptionsWithBlame().stream().map(e -> {
			try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
				if (e.getBlame().isPresent()) {
					pw.println("(" + e.getBlame().get() + ")");
				}
				e.getException().printStackTrace(pw);
				pw.flush();
				return sw.toString();
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
		}).distinct().forEach(System.err::print);

		try {
			List<MessageEmbed> embeds = reporting.createEmbeds();
			if (embeds.isEmpty()) {
				return;
			}
			List<String> urls = reporting.createURLList();

			List<String> urlReplies;
			if (urls.isEmpty()) {
				urlReplies = ImmutableList.of();
			} else {
				urlReplies = SplitUtil.split(urls.stream().collect(Collectors.joining("\n")),
						Message.MAX_CONTENT_LENGTH, true, Strategy.NEWLINE, Strategy.ANYWHERE);
			}

			if (reportingUserID.isPresent()) {
				PrivateChannel privateChannel = jda.openPrivateChannelById(reportingUserID.get()).complete();
				for (MessageEmbed embed : embeds) {
					privateChannel.sendMessageEmbeds(embed).queue();
				}
				for (String urlReply : urlReplies) {
					privateChannel.sendMessage(urlReply).queue();
				}
			}

			if (reportingChannelID.isPresent()) {
				TextChannel textChannel = jda.getTextChannelById(reportingChannelID.get());
				if (textChannel != null) {
					for (MessageEmbed embed : embeds) {
						textChannel.sendMessageEmbeds(embed).queue();
					}
					for (String urlReply : urlReplies) {
						textChannel.sendMessage(urlReply).queue();
					}
				}
			}

		} catch (Exception e) {
			if (reportingUserID.isPresent()) {
				PrivateChannel privateChannel = jda.openPrivateChannelById(reportingUserID.get()).complete();
				privateChannel.sendMessage("Failed to create report!").complete();
				try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
					e.printStackTrace();
					e.printStackTrace(pw);
					pw.flush();
					privateChannel.sendFiles(FileUpload.fromData(sw.toString().getBytes(), "Exception.txt")).complete();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
