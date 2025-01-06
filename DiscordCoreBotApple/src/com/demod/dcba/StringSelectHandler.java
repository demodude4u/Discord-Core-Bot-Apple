package com.demod.dcba;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

@FunctionalInterface
public interface StringSelectHandler {
	void onStringSelectInteraction(StringSelectInteractionEvent event);
}
