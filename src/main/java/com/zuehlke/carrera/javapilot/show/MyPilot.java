package com.zuehlke.carrera.javapilot.show;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Super complete, fancy interesting java doc goes here...
 */
public class MyPilot {

    private PowerApi powerApi;

    @Autowired
    public MyPilot ( PowerApi api ) {
        this.powerApi = api;
    }

    public void input ( RaceEvent event ) {

        // consider the new event
        int power = consider ( event );

        powerApi.setPower ( power );
    }


    private synchronized  int consider(RaceEvent event) {
        return 0;
    }
}
