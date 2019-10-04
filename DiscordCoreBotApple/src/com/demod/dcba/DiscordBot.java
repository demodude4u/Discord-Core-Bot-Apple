package com.demod.dcba;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.demod.dcba.CommandHandler.NoArgHandler;
import com.demod.dcba.CommandHandler.SimpleResponse;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Uninterruptibles;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DiscordBot extends AbstractIdleService {

	private static final String COMMAND_INFO = "info";
	private static final String COMMMAND_HELP = "help";
	private static final String COMMAND_PREFIX = "prefix";
	private static final String COMMAND_SETPREFIX = "setprefix";
	private static final String COMMAND_BOTSTATS = "botstats";

	private final Map<String, CommandDefinition> commands = new LinkedHashMap<>();

	private final InfoDefinition info = new InfoDefinition();
	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private Optional<String> commandPrefix = Optional.empty();
	private Optional<TextWatcher> textWatcher = Optional.empty();
	private Optional<ReactionWatcher> reactionWatcher = Optional.empty();
	private boolean ignorePrivateChannels = false;
	private boolean selfBot = false;

	private ExceptionHandler exceptionHandler;

	private final JSONObject configJson;

	private JDA jda;

	private final LocalDateTime botStarted = LocalDateTime.now();

	DiscordBot() {
		configJson = loadConfig();

		exceptionHandler = (command, event, e) -> {
			e.printStackTrace();
			DiscordUtils.replyTo(event.getChannel(), "Unhandled Error: [" + e.getClass().getSimpleName() + "] "
					+ ((e.getMessage() != null) ? e.getMessage() : ""));
		};
	}

	public void addCommand(CommandDefinition command) {
		commands.put(command.getName().toLowerCase(), command);
		command.getAliases().ifPresent(aliases -> {
			for (String alias : aliases) {
				commands.put(alias.toLowerCase(), command);
			}
		});
	}

	private CommandDefinition createCommandBotStats() {
		return new CommandDefinition(COMMAND_BOTSTATS, true, "Display statistics about the usage of this bot.",
				new NoArgHandler() {
					@Override
					public void handleCommand(MessageReceivedEvent event) {
						JDA jda = event.getJDA();
						List<Guild> guilds = jda.getGuilds();

						int guildCount = guilds.size();
						int memberCount = guilds.stream().mapToInt(g -> g.getMembers().size()).sum();
						String uptimeFormatted = getDurationFormatted(botStarted, LocalDateTime.now());
						long ping = jda.getPing();
						long responseTotal = jda.getResponseTotal();

						EmbedBuilder builder = new EmbedBuilder();
						builder.addField("Total Servers", guildCount + " servers", true);
						builder.addField("Total Members", memberCount + " members", true);
						builder.addField("Uptime", uptimeFormatted, true);
						builder.addField("Ping to Discord", ping + " ms", true);
						builder.addField("Responses to Discord", responseTotal + " responses", true);

						event.getChannel().sendMessage(builder.build()).complete();
					}
				});
	}

	private CommandDefinition createCommandHelp() {
		return new CommandDefinition(COMMMAND_HELP, false, "Lists the commands available for this bot.",
				new NoArgHandler() {
					@Override
					public void handleCommand(MessageReceivedEvent event) {
						boolean showAdmin = event.getChannelType() != ChannelType.PRIVATE
								&& event.getMember().hasPermission(Permission.ADMINISTRATOR);
						List<String> helps = commands.values().stream()
								.filter(c -> c.getHelp().isPresent() && (!c.isAdminOnly() || showAdmin))
								.sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
								.map(c -> "```" + commandPrefix.orElse("") + c.getName()
										+ (c.isAdminOnly() ? " (ADMIN ONLY)" : "") + "```" + c.getHelp().get())
								.collect(Collectors.toList());
						DiscordUtils.replyTo(event.getAuthor().openPrivateChannel().complete(), helps);
						if (!event.isFromType(ChannelType.PRIVATE)) {
							event.getChannel().sendMessage("I have sent you a message, "
									+ event.getAuthor().getAsMention() + "! :slight_smile:").complete();
						}
					}
				});
	}

	private CommandDefinition createCommandInfo() {
		return new CommandDefinition(COMMAND_INFO, false, "Shows information about this bot.", new NoArgHandler() {
			@Override
			public void handleCommand(MessageReceivedEvent event) {
				EmbedBuilder builder = new EmbedBuilder();
				Optional<String> effectivePrefix = getEffectivePrefix(event);
				effectivePrefix.ifPresent(p -> builder.addField("Command Prefix", p, true));
				builder.addField("Command Help", effectivePrefix.map(s -> "Type").orElse("Mention me and type") + " `"
						+ effectivePrefix.orElse("") + "help` to get a list of commands.", true);
				info.getSupportMessage().ifPresent(s -> builder.addField("Support", s, false));
				info.getBotName().ifPresent(n -> builder.addField("Bot Name", n, true));
				info.getVersion().ifPresent(v -> builder.addField("Bot Version", v, true));
				if (info.isAllowInvite()) {
					builder.addField("Server Invite",
							"[Link](" + jda.asBot().getInviteUrl(info.getInvitePermissions()) + ")", true);
				}
				builder.addField("Technologies", info.getTechnologies().stream().collect(Collectors.joining("\n")),
						false);
				for (String group : info.getCredits().keySet()) {
					builder.addField(group, info.getCredits().get(group).stream().collect(Collectors.joining("\n")),
							false);
				}

				event.getChannel().sendMessage(builder.build()).complete();
			}
		});
	}

	private CommandDefinition createCommandPrefix() {
		return new CommandDefinition(COMMAND_PREFIX, false, "Shows the prefix used by this bot.", new NoArgHandler() {
			@Override
			public void handleCommand(MessageReceivedEvent event) {
				if (event.getChannelType() == ChannelType.PRIVATE && !commandPrefix.isPresent()) {
					event.getChannel().sendMessage(
							"No prefix has been set for private channels! Type the commands without a prefix. :slight_smile:")
							.complete();
					return;
				}

				Optional<String> effectivePrefix = getEffectivePrefix(event);

				if (effectivePrefix.isPresent()) {
					event.getChannel().sendMessage("```Prefix: " + effectivePrefix.get() + "```").complete();
				} else {
					event.getChannel()
							.sendMessage("No prefix has been set for this server! Type "
									+ jda.getSelfUser().getAsMention() + " before the command instead. :slight_smile:")
							.complete();
				}
			}
		});
	}

	private CommandDefinition createCommandSetPrefix() {
		return new CommandDefinition("setPrefix", true, "Change the prefix used by this bot.", new CommandHandler() {
			@Override
			public void handleCommand(MessageReceivedEvent event, String[] args) {
				JSONObject guildJson = GuildSettings.get(event.getGuild().getId());
				if (args.length != 1) {
					event.getChannel().sendMessage("Specify a prefix to be used. The prefix cannot include any spaces.")
							.complete();
				}
				String prefix = args[0].trim();
				guildJson.put(COMMAND_PREFIX, prefix);
				GuildSettings.save(event.getGuild().getId(), guildJson);
				event.getChannel().sendMessage("Prefix has been changed to `" + prefix + "`").complete();
			}
		});
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

	private Optional<String> getEffectivePrefix(MessageReceivedEvent event) {
		Optional<String> effectivePrefix = commandPrefix;
		if (event.getChannelType() == ChannelType.TEXT) {
			JSONObject guildJson = GuildSettings.get(event.getGuild().getId());
			if (guildJson.has(COMMAND_PREFIX)) {
				effectivePrefix = Optional.of(guildJson.getString(COMMAND_PREFIX));
			}
		}
		return effectivePrefix;
	}

	public InfoDefinition getInfo() {
		return info;
	}

	public JDA getJDA() {
		return jda;
	}

	void initialize() {
		if (!selfBot) {
			if (!commands.containsKey(COMMAND_INFO)) {
				commands.put(COMMAND_INFO, createCommandInfo());
			}
			if (!commands.containsKey(COMMMAND_HELP)) {
				commands.put(COMMMAND_HELP, createCommandHelp());
			}
			if (!commands.containsKey(COMMAND_PREFIX)) {
				commands.put(COMMAND_PREFIX, createCommandPrefix());
			}
			if (!commands.containsKey(COMMAND_SETPREFIX)) {
				commands.put(COMMAND_SETPREFIX, createCommandSetPrefix());
			}
			if (!commands.containsKey(COMMAND_BOTSTATS)) {
				commands.put(COMMAND_BOTSTATS, createCommandBotStats());
			}
		}

		if (configJson.has("simple")) {
			JSONObject secretJson = configJson.getJSONObject("simple");
			secretJson.keySet().forEach(k -> {
				String response = secretJson.getString(k);
				commands.put(k, new CommandDefinition(k, (SimpleResponse) (e -> response)));
			});
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

	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	public void setIgnorePrivateChannels(boolean ignorePrivateChannels) {
		this.ignorePrivateChannels = ignorePrivateChannels;
	}

	public void setReactionWatcher(Optional<ReactionWatcher> reactionWatcher) {
		this.reactionWatcher = reactionWatcher;
	}

	public void setSelfBot(boolean selfBot) {
		this.selfBot = selfBot;
	}

	public void setTextWatcher(Optional<TextWatcher> textWatcher) {
		this.textWatcher = textWatcher;
	}

	@Override
	protected void shutDown() {
		jda.shutdown();
	}

	@Override
	protected void startUp() throws Exception {
		info.addTechnology("[DCBA](https://github.com/demodude4u/Discord-Core-Bot-Apple)", Optional.empty(),
				"Discord Core Bot Apple");
		info.addTechnology("[JDA](https://github.com/DV8FromTheWorld/JDA)", Optional.of("3.0"), "Java Discord API");

		if (configJson.has("command_prefix")) {
			setCommandPrefix(Optional.of(configJson.getString("command_prefix")));
		}

		jda = new JDABuilder(selfBot ? AccountType.CLIENT : AccountType.BOT)//
				.setToken(configJson.getString("bot_token"))//
				.setEnableShutdownHook(false)//
				.addEventListener(new ListenerAdapter() {
					@Override
					public void onMessageDelete(MessageDeleteEvent event) {
						if (textWatcher.isPresent()) {
							textWatcher.get().deletedMessage(event);
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
					public void onMessageReceived(MessageReceivedEvent event) {
						if (textWatcher.isPresent()) {
							textWatcher.get().seenMessage(event);
						}

						Message message = event.getMessage();
						MessageChannel channel = message.getChannel();
						String rawContent = message.getContentRaw().trim();

						String mentionMe = event.getJDA().getSelfUser().getAsMention();

						boolean isPrivateChannel = channel instanceof PrivateChannel;
						if (isPrivateChannel && ignorePrivateChannels) {
							return;
						}

						boolean startsWithMentionMe = message.getMentionedUsers().stream()
								.anyMatch(u -> u.getIdLong() == event.getJDA().getSelfUser().getIdLong())
								&& rawContent.startsWith(mentionMe);
						if (startsWithMentionMe) {
							rawContent = rawContent.substring(mentionMe.length()).trim();
						}

						Optional<String> effectivePrefix = getEffectivePrefix(event);
						boolean startsWithCommandPrefix = effectivePrefix.isPresent()
								&& rawContent.startsWith(effectivePrefix.get());
						if (startsWithCommandPrefix) {
							rawContent = rawContent.substring(effectivePrefix.get().length()).trim();
						}

						boolean isFromMe = event.getJDA().getSelfUser().equals(event.getAuthor());

						if (!event.getAuthor().isBot()
								&& (isPrivateChannel || startsWithMentionMe || startsWithCommandPrefix)
								&& (!selfBot || isFromMe)) {
							String[] split = rawContent.split("\\s+");
							if (split.length > 0) {
								String command = split[0];
								String[] args = Arrays.copyOfRange(split, 1, split.length);
								CommandDefinition commandDefinition = commands.get(command.toLowerCase());
								if (commandDefinition != null) {
									boolean isPermitted = true;
									if (commandDefinition.isAdminOnly()) {
										if (isPrivateChannel) {
											isPermitted = false;
										} else {
											isPermitted = event.getMember().hasPermission(Permission.ADMINISTRATOR);
										}
									}

									if (isPermitted) {
										event.getChannel().sendTyping().complete();
										AtomicBoolean keepTyping = new AtomicBoolean(true);
										executorService.submit(() -> {
											Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
											while (keepTyping.get()) {
												event.getChannel().sendTyping().complete();
												Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
											}
										});
										try {
											commandDefinition.getHandler().handleCommand(event, args);
										} catch (Exception e) {
											exceptionHandler.handleException(commandDefinition, event, e);
										}
										keepTyping.set(false);
									}
								}
							}
						}
					}

					@Override
					public void onMessageUpdate(MessageUpdateEvent event) {
						if (textWatcher.isPresent()) {
							textWatcher.get().editedMessage(event);
						}
					}
				}).buildBlocking();
	}
}
