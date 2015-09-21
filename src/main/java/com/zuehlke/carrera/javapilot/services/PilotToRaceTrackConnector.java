package com.zuehlke.carrera.javapilot.services;

import com.zuehlke.carrera.relayapi.messages.PowerControl;
import com.zuehlke.carrera.simulator.model.RaceTrackSimulatorSystem;

/**
 *  connects the pilots out-channel (power value) to the racetrack
 */
public class PilotToRaceTrackConnector implements PilotToRelayConnection{

    private RaceTrackSimulatorSystem simulator;


    public PilotToRaceTrackConnector(RaceTrackSimulatorSystem simulator) {
        this.simulator = simulator;
    }

    @Override
    public void announce(String optionalUrl) {
        // nothing to implement in local connector
    }

    @Override
    public void send(PowerControl powerControl) {
        simulator.setPower(powerControl);
    }

    @Override
    public void ensureConnection() {
        // nothing to implement in local connector
    }
}
