package ch.hsr.whitespace.javapilot.model.data.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

import ch.hsr.whitespace.javapilot.akka.messages.PowerChangeMessage;

public class Race implements Serializable {

	private static final long serialVersionUID = -6459971924626608210L;

	private long startTime;
	private long endTime;
	private List<SensorEvent> sensorEvents;
	private List<PowerChangeMessage> powerChanges;
	private List<PenaltyMessage> penalties;
	private List<VelocityMessage> velocities;
	private List<RoundTimeMessage> roundTimes;

	public Race() {
		this.sensorEvents = new ArrayList<>();
		this.powerChanges = new ArrayList<>();
		this.penalties = new ArrayList<>();
		this.velocities = new ArrayList<>();
		this.roundTimes = new ArrayList<>();
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public List<SensorEvent> getSensorEvents() {
		return sensorEvents;
	}

	public void setSensorEvents(List<SensorEvent> sensorEvents) {
		this.sensorEvents = sensorEvents;
	}

	public List<PowerChangeMessage> getPowerChanges() {
		return powerChanges;
	}

	public void setPowerChanges(List<PowerChangeMessage> powerChanges) {
		this.powerChanges = powerChanges;
	}

	public List<PenaltyMessage> getPenalties() {
		return penalties;
	}

	public void setPenalties(List<PenaltyMessage> penalties) {
		this.penalties = penalties;
	}

	public List<VelocityMessage> getVelocities() {
		return velocities;
	}

	public void setVelocities(List<VelocityMessage> velocities) {
		this.velocities = velocities;
	}

	public List<RoundTimeMessage> getRoundTimes() {
		return roundTimes;
	}

	public void setRoundTimes(List<RoundTimeMessage> roundTimes) {
		this.roundTimes = roundTimes;
	}

}
