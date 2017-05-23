package com.demod.dcba;

public final class DiscordConsts {
	public static final int MAX_MESSAGE_SIZE = 2000;

	/**
	 * Add this to the beginning of a message to help prevent bot message loops.
	 * It is a zero-width space.
	 */
	public static final char SAFETY_CHAR = '\u200B';

	private DiscordConsts() {
	}
}
