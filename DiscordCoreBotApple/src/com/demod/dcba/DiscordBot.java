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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.util.concurrent.AbstractIdleService;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.FileUpload;

public class DiscordBot extends AbstractIdleService {

	private static final String COMMAND_INFO = "info";
	private static final String COMMAND_FEEDBACK = "feedback";

	private final Map<String, SlashCommandDefinition> commandSlash = new LinkedHashMap<>();

	private final InfoDefinition info = new InfoDefinition();

	private final ExecutorService commandService = Executors.newSingleThreadExecutor();

	private Optional<String> commandPrefix = Optional.empty();
	private Optional<ReactionWatcher> reactionWatcher = Optional.empty();

	private final JSONObject configJson;

	private JDA jda;

	private final LocalDateTime botStarted = LocalDateTime.now();

	private Function<JDABuilder, JDABuilder> customSetup = null;

	private Optional<String> reportingUserID = Optional.empty();
	private Optional<String> reportingChannelID = Optional.empty();

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

	public Optional<String> getCommandPrefix() {
		return commandPrefix;
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

	public void setCommandPrefix(Optional<String> commandPrefix) {
		this.commandPrefix = commandPrefix;
	}

	public void setCustomSetup(Function<JDABuilder, JDABuilder> customSetup) {
		this.customSetup = customSetup;
	}

	public void setReactionWatcher(Optional<ReactionWatcher> reactionWatcher) {
		this.reactionWatcher = reactionWatcher;
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

		if (configJson.has("command_prefix")) {
			setCommandPrefix(Optional.of(configJson.getString("command_prefix")));
		}

		JDABuilder builder = JDABuilder.createDefault(configJson.getString("bot_token"))//
				.setEnableShutdownHook(false)//
				.addEventListeners(new ListenerAdapter() {
					@Override
					public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
						SlashCommandDefinition commandDefinition = commandSlash
								.get(event.getFullCommandName().replace(' ', '/'));
						Optional<AutoCompleteHandler> autoCompleteHandler = commandDefinition.getAutoCompleteHandler();
						if (autoCompleteHandler.isPresent()) {
							AutoCompleteEvent autoCompleteEvent = new AutoCompleteEvent(event);
							autoCompleteHandler.get().handleAutoComplete(autoCompleteEvent);
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
		reporting.getExceptionsWithBlame().stream().forEach(e -> {
			if (e.getBlame().isPresent()) {
				System.err.println("(" + e.getBlame().get() + ")");
			}
			e.getException().printStackTrace();
		});

		try {
			List<MessageEmbed> embeds = reporting.createEmbeds();

			if (reportingUserID.isPresent()) {
				PrivateChannel privateChannel = jda.openPrivateChannelById(reportingUserID.get()).complete();
				for (MessageEmbed embed : embeds) {
					privateChannel.sendMessageEmbeds(embed).complete();
				}
			}

			if (reportingChannelID.isPresent()) {
				TextChannel textChannel = jda.getTextChannelById(reportingChannelID.get());
				if (textChannel != null) {
					for (MessageEmbed embed : embeds) {
						textChannel.sendMessageEmbeds(embed).complete();
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
