package com.zuehlke.carrera.javapilot.services;

import akka.actor.ActorRef;
import com.zuehlke.carrera.relayapi.messages.*;
import com.zuehlke.carrera.simulator.model.PilotInterface;

/**
 *  Implementation to connect a RaceTrackSimulatorSystem to a Pilot System locally
 */
public class RacetrackToPilotConnector implements PilotInterface, PilotCommandInterface {

    ActorRef pilotEntryPoint;

    public void registerPilot ( ActorRef pilotEntryPoint ) {
        this.pilotEntryPoint = pilotEntryPoint;
    }

    @Override
    public void send(SensorEvent message) {
        sendMessage(message);
    }

    @Override
    public void send(VelocityMessage message) {
        sendMessage(message);
    }

    @Override
    public void send(PenaltyMessage message) {
        sendMessage(message);
    }

    @Override
    public void send(RoundTimeMessage message) {
        sendMessage(message);
    }

    @Override
    public void ensureConnection(String url) {
        // don't need this in local mode
    }

    @Override
    public void send(RaceStartMessage message) {
        sendMessage ( message );
    }

    @Override
    public void send(RaceStopMessage message) {
        sendMessage ( message );
    }

    private void sendMessage (Object message) {
        // Messages may arrive during startup prior to entry point registration. Just ignore them
        if ( pilotEntryPoint != null ) {
            pilotEntryPoint.tell ( message, ActorRef.noSender());
        }
    }

}
