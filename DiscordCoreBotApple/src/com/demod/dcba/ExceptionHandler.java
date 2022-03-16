package com.demod.dcba;

@FunctionalInterface
public interface ExceptionHandler {
	void handleException(CommandDefinition command, CommandEvent event, Exception e);
}
