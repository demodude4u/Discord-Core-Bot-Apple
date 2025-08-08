package com.demod.dcba.example;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import com.demod.dcba.DCBA;
import com.demod.dcba.DiscordBot;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Uninterruptibles;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CommandTesting {

	public static void main(String[] args) throws IOException {
		DiscordBot bot = DCBA.builder()//
				.addSlashCommand("params", "The bot will recite the parameters that are recognized.", event -> {
					EmbedBuilder embed = new EmbedBuilder();
					event.optParamBoolean("boolean").ifPresent(o -> embed.addField("boolean", "" + o, true));
					event.optParamChannel("channel").ifPresent(o -> embed.addField("channel", "" + o.getName(), true));
					event.optParamLong("integer").ifPresent(o -> embed.addField("integer", "" + o, true));
					event.optParamMentionable("mentionable")
							.ifPresent(o -> embed.addField("mentionable", "" + o.getAsMention(), true));
					event.optParamDouble("number").ifPresent(o -> embed.addField("number", "" + o, true));
					event.optParamRole("role").ifPresent(o -> embed.addField("role", "" + o.getName(), true));
					event.optParamString("string").ifPresent(o -> embed.addField("string", "" + o, true));
					event.optParamUser("user").ifPresent(o -> embed.addField("user", "" + o.getName(), true));
					event.optParamAttachment("attachment")
							.ifPresent(o -> embed.addField("attachment", "" + o.getUrl(), true));
					event.replyEmbed(embed.build());
				})//
				.withOptionalParam(OptionType.BOOLEAN, "boolean", "description")//
				.withOptionalParam(OptionType.CHANNEL, "channel", "description")//
				.withOptionalParam(OptionType.INTEGER, "integer", "description")//
				.withOptionalParam(OptionType.MENTIONABLE, "mentionable", "description")//
				.withOptionalParam(OptionType.NUMBER, "number", "description")//
				.withOptionalParam(OptionType.ROLE, "role", "description")//
				.withOptionalParam(OptionType.STRING, "string", "description")//
				.withOptionalParam(OptionType.USER, "user", "description")//
				.withOptionalParam(OptionType.ATTACHMENT, "attachment", "description")
				//
				//
				.addSlashCommand("busy", "Simulate a command that takes some time to finish.", event -> {
					long seconds = event.getParamLong("seconds");
					Uninterruptibles.sleepUninterruptibly(seconds, TimeUnit.SECONDS);
					event.reply("Waited " + seconds + " seconds!");
				})//
				.withParam(OptionType.INTEGER, "seconds", "Seconds to wait before the command is completed.")
				//
				//
				.addSlashCommand("multi-reply", "Replies back with multiple messages.", event -> {
					String[] messages = event.getParamString("messages").split(",");
					for (String message : messages) {
						event.reply(message.trim());
					}
				})//
				.withParam(OptionType.STRING, "messages", "Comma delimited messages.")
				//
				//
				.addSlashCommand("path/test/command", "Command path test.", event -> event.reply("Success!"))
				.addSlashCommand("path/test/command2", "Command path test.", event -> event.reply("Success!"))
				.addSlashCommand("path/test2", "Command path test.", event -> event.reply("Success!"))
				//
				//
				.addSlashCommand("autocomplete/range", "Suggest numbers near what you entered.",
						event -> event.reply("Number: " + event.getParamLong("number")), event -> {
							Optional<Long> number = event.optParamLong("number");
							if (number.isPresent()) {
								event.replyIntegers(LongStream.rangeClosed(number.get() - 5, number.get() + 5).boxed()
										.collect(Collectors.toList()));
							} else {
								event.replyIntegers(ImmutableList.of());
							}
						})//
				.withAutoParam(OptionType.INTEGER, "number", "Number to auto-complete about.")
				//
				//
				.create();

		bot.startAsync().awaitRunning();
		System.in.read(); // Wait for <enter>
		bot.stopAsync().awaitTerminated();
	}

}
