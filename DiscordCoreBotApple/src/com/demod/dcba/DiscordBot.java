package com.demod.dcba;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.util.concurrent.AbstractIdleService;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DiscordBot extends AbstractIdleService {

	private final Map<String, CommandDefinition> commands = new LinkedHashMap<>();
	private final InfoDefinition info = new InfoDefinition();

	private Optional<String> commandPrefix = Optional.empty();

	private final JSONObject config;
	private JDA jda;

	DiscordBot() {
		config = loadConfig();

		commands.put("info", new CommandDefinition("info", "Shows information about this bot.", new CommandHandler() {
			@Override
			public void handleCommand(MessageReceivedEvent event) {
				EmbedBuilder builder = new EmbedBuilder();
				commandPrefix.ifPresent(p -> builder.addField("Command Prefix", p, true));
				info.getBotName().ifPresent(n -> builder.addField("Bot Name", n, true));
				info.getVersion().ifPresent(v -> builder.addField("Bot Version", v, true));
				info.getSupportMessage().ifPresent(s -> builder.addField("Support", s, true));
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
						List<String> helps = commands.values().stream().filter(c -> c.getHelp().isPresent())
								.sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
								.map(c -> "```" + commandPrefix.orElse("") + c.getName() + "```" + c.getHelp().get())
								.collect(Collectors.toList());
						DiscordUtils.replyTo(event, helps);
					}
				}));

		if (commandPrefix.isPresent()) {
			commands.put("prefix",
					new CommandDefinition("prefix", "Shows the prefix used by this bot.", new CommandHandler() {
						@Override
						public void handleCommand(MessageReceivedEvent event) {
							event.getChannel().sendMessage("```Prefix: " + commandPrefix.get() + "```").complete();
						}
					}));
		}
	}

	public void addCommand(CommandDefinition command) {
		commands.put(command.getName(), command);
	}

	public Optional<String> getCommandPrefix() {
		return commandPrefix;
	}

	public InfoDefinition getInfo() {
		return info;
	}

	private JSONObject loadConfig() {
		try (Scanner scanner = new Scanner(new FileInputStream("config.json"), "UTF-8")) {
			scanner.useDelimiter("\\A");
			return new JSONObject(scanner.next());
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
		info.addTechnology("DCBA", Optional.of("0.0.1"), "Discord Core Bot Apple");
		info.addTechnology("JDA", Optional.of("3.0"), "Java Discord API");

		jda = new JDABuilder(AccountType.BOT).setToken(config.getString("discord_bot_token"))
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
										event.getChannel().sendTyping().complete();
										commandDefinition.getHandler().handleCommand(event);
									}
								}
							}
						}
					}
				}).buildBlocking();
	}

}
