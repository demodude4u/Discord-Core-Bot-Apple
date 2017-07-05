package com.demod.dcba;

import java.io.FileInputStream;
import java.io.IOException;
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

import com.demod.dcba.CommandHandler.SimpleResponse;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Uninterruptibles;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DiscordBot extends AbstractIdleService {

	private final Map<String, CommandDefinition> commands = new LinkedHashMap<>();
	private final InfoDefinition info = new InfoDefinition();
	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private Optional<String> commandPrefix = Optional.empty();

	private final JSONObject configJson;

	private JDA jda;

	DiscordBot() {
		configJson = loadConfig();

		commands.put("info", new CommandDefinition("info", "Shows information about this bot.", new CommandHandler() {
			@Override
			public void handleCommand(MessageReceivedEvent event) {
				EmbedBuilder builder = new EmbedBuilder();
				commandPrefix.ifPresent(p -> builder.addField("Command Prefix", p, true));
				builder.addField("Command Help", commandPrefix.map(s -> "Type").orElse("Mention me and type") + " `"
						+ commandPrefix.orElse("") + "help` to get a list of commands.", true);
				info.getSupportMessage().ifPresent(s -> builder.addField("Support", s, true));
				if (info.isAllowInvite()) {
					builder.addField("Server Invite",
							"[Link](" + jda.asBot().getInviteUrl(info.getInvitePermissions()) + ")", false);
				}
				info.getBotName().ifPresent(n -> builder.addField("Bot Name", n, true));
				info.getVersion().ifPresent(v -> builder.addField("Bot Version", v, true));
				builder.addField("Technologies", info.getTechnologies().stream().collect(Collectors.joining("\n")),
						false);
				for (String group : info.getCredits().keySet()) {
					builder.addField(group, info.getCredits().get(group).stream().collect(Collectors.joining("\n")),
							false);
				}

				event.getChannel().sendMessage(builder.build()).complete();
			}
		}));

		commands.put("help",
				new CommandDefinition("help", "Lists the commands available for this bot.", new CommandHandler() {
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
				}));

		if (configJson.has("simple")) {
			JSONObject secretJson = configJson.getJSONObject("simple");
			secretJson.keySet().forEach(k -> {
				String response = secretJson.getString(k);
				commands.put(k, new CommandDefinition(k, (SimpleResponse) (e -> response)));
			});
		}
	}

	public void addCommand(CommandDefinition command) {
		commands.put(command.getName().toLowerCase(), command);
	}

	public Optional<String> getCommandPrefix() {
		return commandPrefix;
	}

	public InfoDefinition getInfo() {
		return info;
	}

	public JDA getJDA() {
		return jda;
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

	@Override
	protected void shutDown() throws Exception {
		jda.shutdown();
	}

	@Override
	protected void startUp() throws Exception {
		info.addTechnology("DCBA", Optional.empty(), "Discord Core Bot Apple");
		info.addTechnology("JDA", Optional.of("3.0"), "Java Discord API");

		if (configJson.has("command_prefix")) {
			setCommandPrefix(Optional.of(configJson.getString("command_prefix")));
		}

		if (commandPrefix.isPresent()) {
			commands.put("prefix",
					new CommandDefinition("prefix", "Shows the prefix used by this bot.", new CommandHandler() {
						@Override
						public void handleCommand(MessageReceivedEvent event) {
							event.getChannel().sendMessage("```Prefix: " + commandPrefix.get() + "```").complete();
						}
					}));
		}

		jda = new JDABuilder(AccountType.BOT)//
				.setToken(configJson.getString("bot_token"))//
				.setEnableShutdownHook(false)//
				.addEventListener(new ListenerAdapter() {
					@Override
					public void onMessageReceived(MessageReceivedEvent event) {
						Message message = event.getMessage();
						MessageChannel channel = message.getChannel();
						String rawContent = message.getRawContent().trim();

						String mentionMe = event.getJDA().getSelfUser().getAsMention();

						boolean isPrivateChannel = channel instanceof PrivateChannel;

						boolean startsWithMentionMe = message.getMentionedUsers().stream()
								.anyMatch(u -> u.getIdLong() == event.getJDA().getSelfUser().getIdLong())
								&& rawContent.startsWith(mentionMe);
						if (startsWithMentionMe) {
							rawContent = rawContent.substring(mentionMe.length()).trim();
						}
						boolean startsWithCommandPrefix = commandPrefix.isPresent()
								&& rawContent.startsWith(commandPrefix.get());
						if (startsWithCommandPrefix) {
							rawContent = rawContent.substring(commandPrefix.get().length()).trim();
						}
						if (!event.getAuthor().isBot()
								&& (isPrivateChannel || startsWithMentionMe || startsWithCommandPrefix)) {
							if (rawContent.isEmpty()) {
								commands.get("info").getHandler().handleCommand(event);
							} else {
								String[] split = rawContent.split("\\s+");
								if (split.length > 0) {
									String command = split[0];
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
												commandDefinition.getHandler().handleCommand(event);
											} catch (Exception e) {
												e.printStackTrace();
											}
											keepTyping.set(false);
										}
									}
								}
							}
						}
					}
				}).buildBlocking();
	}
}
