package com.demod.dcba;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@FunctionalInterface
public interface PrivateMessageHandler {
    void onPrivateMessageReceived(MessageReceivedEvent event, CommandReporting reporting) throws Exception;
}
