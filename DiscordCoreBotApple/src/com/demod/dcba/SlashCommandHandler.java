package com.demod.dcba;

@FunctionalInterface
public interface SlashCommandHandler {
	void handleCommand(SlashCommandEvent event) throws Exception;
}
