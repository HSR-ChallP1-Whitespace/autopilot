//  The contents of this file are subject to the Mozilla Public License
//  Version 1.1 (the "License"); you may not use this file except in
//  compliance with the License. You may obtain a copy of the License
//  at http://www.mozilla.org/MPL/
//
//  Software distributed under the License is distributed on an "AS IS"
//  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
//  the License for the specific language governing rights and
//  limitations under the License.
//
//  The Original Code is RabbitMQ.
//
//  The Initial Developer of the Original Code is GoPivotal, Inc.
//  Copyright (c) 2007-2015 Pivotal Software, Inc.  All rights reserved.
//


package com.zuehlke.carrera.javapilot.show;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;
import com.zuehlke.carrera.api.seralize.JacksonSerializer;
import com.zuehlke.carrera.relayapi.messages.PowerControl;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

public class ConfirmDontLoseMessages {
    static int msgCount = 10000;
    final static String QUEUE_NAME = "confirm-test";
    static ConnectionFactory connectionFactory;

    public static void main1(String[] args)
            throws IOException, InterruptedException
    {
        if (args.length > 0) {
            msgCount = Integer.parseInt(args[0]);
        }

        connectionFactory = new ConnectionFactory();

        // Consume msgCount messages.
        (new Thread(new Consumer())).start();
        // Publish msgCount messages and wait for confirms.
        (new Thread(new Publisher())).start();
    }

    public static void main(String[] args)
            throws IOException, InterruptedException, TimeoutException {

        connectionFactory = new ConnectionFactory();
        String POWER_QUEUE = "/app/pilots/power";
        String json = new JacksonSerializer().serialize(new PowerControl(200, "team", "access", 0L));
        Connection conn = connectionFactory.newConnection();
        Channel ch = conn.createChannel();
        //ch.queueDeclare(POWER_QUEUE, true, false, false, null);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().expiration("0").build();
        ch.basicPublish("", POWER_QUEUE, props, json.getBytes());

        String SENSOR_QUEUE = "/topic/pilots/starterkit/sensor";
        SensorEvent sensorEvent = SensorEvent.createEmptyCarSensor().withRaceTrackId("simulator");
        json = new JacksonSerializer().serialize(sensorEvent);

        //ch.queueDeclare(SENSOR_QUEUE, true, false, false, null);
        ch.basicPublish("", SENSOR_QUEUE, props, json.getBytes());

        ch.close();
        conn.close();
    }

    @SuppressWarnings("ThrowablePrintedToSystemOut")
    static class Publisher implements Runnable {
        public void run() {
            try {
                long startTime = System.currentTimeMillis();

                // Setup
                Connection conn = connectionFactory.newConnection();
                Channel ch = conn.createChannel();
                ch.queueDeclare(QUEUE_NAME, true, false, false, null);
                ch.confirmSelect();

                // Publish
                for (long i = 0; i < msgCount; ++i) {
                    ch.basicPublish("", QUEUE_NAME,
                            MessageProperties.PERSISTENT_BASIC,
                            "nop".getBytes());
                }

                ch.waitForConfirmsOrDie();

                // Cleanup
                ch.queueDelete(QUEUE_NAME);
                ch.close();
                conn.close();

                long endTime = System.currentTimeMillis();
                System.out.printf("Test took %.3fs\n",
                        (float)(endTime - startTime)/1000);
            } catch (Throwable e) {
                System.out.println("foobar :(");
                System.out.print(e);
            }
        }
    }


    static class Consumer implements Runnable {
        public void run() {
            try {
                // Setup
                Connection conn = connectionFactory.newConnection();
                Channel ch = conn.createChannel();
                ch.queueDeclare(QUEUE_NAME, true, false, false, null);

                // Consume
                QueueingConsumer qc = new QueueingConsumer(ch);
                ch.basicConsume(QUEUE_NAME, true, qc);
                for (int i = 0; i < msgCount; ++i) {
                    qc.nextDelivery();
                }

                // Cleanup
                ch.close();
                conn.close();
            } catch (Throwable e) {
                System.out.println("Whoosh!");
                System.out.print(e);
            }
        }
    }
}