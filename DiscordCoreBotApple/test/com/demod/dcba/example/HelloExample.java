package com.demod.dcba.example;

import java.io.IOException;

import com.demod.dcba.DCBA;
import com.demod.dcba.DiscordBot;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelloExample {

	public static void main(String[] args) throws IOException {
		DiscordBot bot = DCBA.builder()//
				.withCommandPrefix("!")//
				//
				.addSlashCommand("hello", "The bot will say hi to you!",
						event -> event.reply("Hi " + event.getUser().getAsMention() + "!"))//
				.withLegacyWarning("hello", "hi", "hey", "howdy")//
				//
				.addSlashCommand("secret/hello", "The bot will say hi to you, secretly!",
						event -> event.reply("Hi " + event.getUser().getAsMention() + "!"))//
				.ephemeral()//
				//
				.addSlashCommand("private/hello", "The bot will say hi to you, privately!",
						event -> event.replyPrivate("Hi " + event.getUser().getAsMention() + "!"))//
				.ephemeral()//
				//
				.addSlashCommand("admin/hello", "The bot will say hi to you, if you are an admin!",
						event -> event.reply("Hi admin " + event.getUser().getAsMention() + "!"))//
				.adminOnly()//
				//
				.addMessageCommand("Say Hello!", event -> {
					event.getMessage().reply("Hello " + event.getMessage().getAuthor().getAsMention() + "!").complete();
					// event.reply("Hello " + event.getMessage().getAuthor().getAsMention() + "!");
				})
				//
				.addTextWatcher((MessageReceivedEvent event) -> System.out
						.println((event.getChannelType() == ChannelType.TEXT ? event.getGuild().getName() : "???")
								+ " #" + event.getChannel().getName() + " " + event.getAuthor().getName() + " -> "
								+ event.getMessage().getContentDisplay()))//
				//
				.create();

		bot.startAsync().awaitRunning();
		System.in.read(); // Wait for <enter>
		bot.stopAsync().awaitTerminated();
	}

}
