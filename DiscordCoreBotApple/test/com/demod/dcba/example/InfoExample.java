package com.demod.dcba.example;

import java.io.IOException;

import com.demod.dcba.DCBA;
import com.demod.dcba.DiscordBot;

public class InfoExample {

	public static void main(String[] args) throws IOException {
		DiscordBot bot = DCBA.builder()//
				.setInfo("Info Example Bot")//
				.withVersion("1.2.3")//
				.withSupport("Contact Demod for help!")//
				.withTechnology("FakeDoors", "We sell fake doors here!")//
				.withTechnology("Noop", "1.2", "This does nothing.")//
				.withCredits("Testers", "A Guy", "A Gay Guy", "The Dude")//
				.withCredits("Special Thanks", "Mountain Dew")//
				.create();

		bot.startAsync().awaitRunning();
		System.in.read(); // Wait for <enter>
		bot.stopAsync().awaitTerminated();
	}

}
