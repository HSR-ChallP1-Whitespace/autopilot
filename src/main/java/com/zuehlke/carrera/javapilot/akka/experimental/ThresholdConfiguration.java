package com.zuehlke.carrera.javapilot.akka.experimental;

import com.zuehlke.carrera.javapilot.akka.Observable;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

/**
 */
public class ThresholdConfiguration {

    private final float threshold;
    private final Observable observable;
    private final int lowerPowerValue;
    private final int higherPowerValue;

    public ThresholdConfiguration(float threshold, Observable observable, int lowerPowerValue, int higherPowerValue) {
        this.threshold = threshold;
        this.observable = observable;
        this.lowerPowerValue = lowerPowerValue;
        this.higherPowerValue = higherPowerValue;
    }

    public boolean isAboveThreshold(SensorEvent event) {
        switch ( observable ) {
            case A0:
                return event.getA()[0] > threshold;
            case A1:
                return event.getA()[1] > threshold;
            case A2:
                return event.getA()[2] > threshold;
            case G0:
                return event.getG()[0] > threshold;
            case G1:
                return event.getG()[1] > threshold;
            case G2:
                return event.getG()[2] > threshold;
            case M0:
                return event.getM()[0] > threshold;
            case M1:
                return event.getM()[1] > threshold;
            case M2:
                return event.getM()[2] > threshold;
        }
        return false;
    }

    public int getLowerPowerValue() {
        return lowerPowerValue;
    }
    public int getHigherPowerValue() {
        return higherPowerValue;
    }


}
