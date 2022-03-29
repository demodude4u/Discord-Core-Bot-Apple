package com.demod.dcba;

@FunctionalInterface
public interface MessageCommandHandler {
	void handleCommand(MessageCommandEvent event) throws Exception;
}
