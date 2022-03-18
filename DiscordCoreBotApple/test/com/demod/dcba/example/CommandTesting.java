package com.demod.dcba.example;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.demod.dcba.DCBA;
import com.demod.dcba.DiscordBot;
import com.google.common.util.concurrent.Uninterruptibles;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CommandTesting {

	public static void main(String[] args) throws IOException {
		DiscordBot bot = DCBA.builder()//
				.withCommandPrefix("!")//
				//
				//
				.addSimpleCommand("params", (event, embed) -> {
					event.optParamBoolean("boolean").ifPresent(o -> embed.addField("boolean", "" + o, true));
					event.optParamGuildChannel("channel")
							.ifPresent(o -> embed.addField("channel", "" + o.getName(), true));
					event.optParamLong("integer").ifPresent(o -> embed.addField("integer", "" + o, true));
					event.optParamMentionable("mentionable")
							.ifPresent(o -> embed.addField("mentionable", "" + o.getAsMention(), true));
					event.optParamDouble("number").ifPresent(o -> embed.addField("number", "" + o, true));
					event.optParamRole("role").ifPresent(o -> embed.addField("role", "" + o.getName(), true));
					event.optParamString("string").ifPresent(o -> embed.addField("string", "" + o, true));
					event.optParamUser("user").ifPresent(o -> embed.addField("user", "" + o.getName(), true));
					event.optParamAttachment("attachment")
							.ifPresent(o -> embed.addField("attachment", "" + o.getUrl(), true));
				})//
				.withHelp("The bot will recite the parameters that it recognized.")//
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
				.addSimpleCommand("busy", event -> {
					long seconds = event.getParamLong("seconds");
					Uninterruptibles.sleepUninterruptibly(seconds, TimeUnit.SECONDS);
					return "Waited " + seconds + " seconds!";
				})//
				.withHelp("Simulate a command that takes some time to finish.")//
				.withParam(OptionType.INTEGER, "seconds", "Seconds to wait before the command is completed.")
				//
				//
				.addCommand("multi-reply", event -> {
					String[] messages = event.getParamString("messages").split(",");
					for (String message : messages) {
						event.reply(message.trim());
					}
				})//
				.withHelp("Replies back with multiple messages.")
				.withParam(OptionType.STRING, "messages", "Comma delimited messages.")
				//
				//
				.create();

		bot.startAsync().awaitRunning();
		System.in.read(); // Wait for <enter>
		bot.stopAsync().awaitTerminated();
	}

}
