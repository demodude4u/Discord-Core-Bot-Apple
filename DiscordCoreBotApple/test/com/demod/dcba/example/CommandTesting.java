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
				.addSlashCommand("params", (event, embed) -> {
					event.optParamMapping("boolean")
							.ifPresent(o -> embed.addField(o.getName(), "" + o.getAsBoolean(), true));
					event.optParamMapping("channel")
							.ifPresent(o -> embed.addField(o.getName(), "" + o.getAsGuildChannel().getName(), true));
					event.optParamMapping("integer")
							.ifPresent(o -> embed.addField(o.getName(), "" + o.getAsLong(), true));
					event.optParamMapping("mentionable").ifPresent(
							o -> embed.addField(o.getName(), "" + o.getAsMentionable().getAsMention(), true));
					event.optParamMapping("number")
							.ifPresent(o -> embed.addField(o.getName(), "" + o.getAsDouble(), true));
					event.optParamMapping("role")
							.ifPresent(o -> embed.addField(o.getName(), "" + o.getAsRole().getName(), true));
					event.optParamMapping("string")
							.ifPresent(o -> embed.addField(o.getName(), "" + o.getAsString(), true));
					event.optParamMapping("user")
							.ifPresent(o -> embed.addField(o.getName(), "" + o.getAsUser().getName(), true));
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
				//
				//
				.addSlashCommand("busy", event -> {
					long seconds = event.getParamMapping("seconds").getAsLong();
					Uninterruptibles.sleepUninterruptibly(seconds, TimeUnit.SECONDS);
					return "Waited " + seconds + " seconds!";
				})//
				.withHelp("Simulate a command that takes some time to finish.")//
				.withParam(OptionType.INTEGER, "seconds", "Seconds to wait before the command is completed.")
				//
				//
				.create();

		bot.startAsync().awaitRunning();
		System.in.read(); // Wait for <enter>
		bot.stopAsync().awaitTerminated();
	}

}
