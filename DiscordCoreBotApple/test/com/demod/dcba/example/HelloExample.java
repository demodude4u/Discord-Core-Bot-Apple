package com.demod.dcba.example;

import java.io.IOException;

import com.demod.dcba.DCBA;
import com.demod.dcba.DiscordBot;

public class HelloExample {

	public static void main(String[] args) throws IOException {
		DiscordBot bot = DCBA.builder()//
				.withCommandPrefix("!")//
				//
				.addCommand("hello", (event) -> "Hi " + event.getAuthor().getAsMention() + "!")//
				.withAliases("hi", "hey", "howdy")//
				.withHelp("The bot will say hi to you!")//
				//
				.addCommand("helloAdmin", (event) -> "Hi admin " + event.getAuthor().getAsMention() + "!")//
				.adminOnly()//
				.withHelp("The bot will say hi to you, and mention you as an admin!")//
				//
				.create();

		bot.startAsync().awaitRunning();
		System.in.read(); // Wait for <enter>
		bot.stopAsync().awaitTerminated();
	}

}
