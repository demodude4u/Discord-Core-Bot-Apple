package com.demod.dcba.example;

import java.io.IOException;

import com.demod.dcba.DCBA;
import com.demod.dcba.DiscordBot;

public class HelloExample {

	public static void main(String[] args) throws IOException {
		DiscordBot bot = DCBA.builder()//
				.addSlashCommand("hello", "The bot will say hi to you!",
						event -> event.reply("Hi " + event.getUser().getAsMention() + "!"))//
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
				.create();

		bot.startAsync().awaitRunning();
		System.in.read(); // Wait for <enter>
		bot.stopAsync().awaitTerminated();
	}

}
