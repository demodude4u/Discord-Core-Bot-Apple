package com.demod.dcba;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@FunctionalInterface
public interface ButtonHandler {
	void onButtonInteraction(ButtonInteractionEvent event, CommandReporting reporting) throws Exception;
}
