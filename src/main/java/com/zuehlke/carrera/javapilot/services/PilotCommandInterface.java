package com.zuehlke.carrera.javapilot.services;

import com.zuehlke.carrera.relayapi.messages.RaceStartMessage;
import com.zuehlke.carrera.relayapi.messages.RaceStopMessage;

/**
 * defines the command interface to the pilot
 */
public interface PilotCommandInterface {

    public void send ( RaceStartMessage message );

    public void send ( RaceStopMessage message );
}
