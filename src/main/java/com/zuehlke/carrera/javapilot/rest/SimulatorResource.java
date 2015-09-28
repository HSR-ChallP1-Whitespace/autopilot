package com.zuehlke.carrera.javapilot.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.zuehlke.carrera.javapilot.services.SimulatorService;
import com.zuehlke.carrera.relayapi.messages.RaceStartMessage;
import com.zuehlke.carrera.relayapi.messages.RaceStopMessage;
import com.zuehlke.carrera.simulator.model.racetrack.TrackInfo;

@RestController
@RequestMapping("/api/simulator")
public class SimulatorResource {

	@Autowired
	private SimulatorService simulatorService;

	@RequestMapping(value = "/track", method = RequestMethod.GET, produces = "application/json")
	public TrackInfo getTrack() {
		return simulatorService.getTrackInfo();
	}

	@RequestMapping(value = "/startRace", method = RequestMethod.POST)
	public void startRace() {
		simulatorService.fireRaceStartEvent(new RaceStartMessage("local", "TRAINING", "", 0L, "", false));
	}

	@RequestMapping(value = "/stopRace", method = RequestMethod.POST)
	public void stopRace() {
		simulatorService.fireRaceStopEvent(new RaceStopMessage());
	}

	@RequestMapping(value = "/start", method = RequestMethod.POST)
	public void startSimulator() {
		simulatorService.startClock();
	}

	@RequestMapping(value = "/stop", method = RequestMethod.POST)
	public void stopSimulator() {
		simulatorService.stopClock();
	}

	@RequestMapping(value = "/reset", method = RequestMethod.POST)
	public void resetSimulator() {
		simulatorService.reset();
	}

	@RequestMapping(value = "/powerup/{delta}", method = RequestMethod.POST)
	public void powerup(@PathVariable int delta) {
		simulatorService.powerup(delta);
	}

	@RequestMapping(value = "/powerdown/{delta}", method = RequestMethod.POST)
	public void powerdown(@PathVariable int delta) {
		simulatorService.powerdown(delta);
	}

	@RequestMapping(value = "/selectDesign", method = RequestMethod.POST, produces = "application/json")
	public TrackInfo selectDesign(@RequestBody String trackDesign) {
		return simulatorService.selectDesign(trackDesign);
	}
}
