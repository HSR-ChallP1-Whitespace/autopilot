package com.zuehlke.carrera.javapilot;


import akka.actor.ActorRef;
import com.zuehlke.carrera.api.PilotApi;
import com.zuehlke.carrera.api.PilotApiImpl;
import com.zuehlke.carrera.api.channel.PilotToRelayChannelNames;
import com.zuehlke.carrera.api.client.Client;
import com.zuehlke.carrera.api.client.rabbit.RabbitClient;
import com.zuehlke.carrera.api.seralize.JacksonSerializer;
import com.zuehlke.carrera.api.seralize.Serializer;
import com.zuehlke.carrera.connection.*;
import com.zuehlke.carrera.javapilot.config.PilotProperties;
import com.zuehlke.carrera.javapilot.services.PilotService;
import com.zuehlke.carrera.javapilot.services.PilotToRelayConnection;
import com.zuehlke.carrera.javapilot.services.SimulatorService;
import com.zuehlke.carrera.relayapi.messages.TrainingRequest;
import com.zuehlke.carrera.relayapi.messages.TrainingResponse;
import com.zuehlke.carrera.simulator.config.SimulatorProperties;
import com.zuehlke.carrera.simulator.model.RaceTrackSimulatorSystem;
import org.apache.commons.cli.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties({SimulatorProperties.class})  // loaded from classpath:/application.yml
@EnableWebSocketMessageBroker
public class PilotApplication implements CommandLineRunner{

    @Value("${javapilot.trainingUrl}")
    private String relayTrainingUrl;

    @Value("${javapilot.name}")
    private String team;

    @Value("${javapilot.accessCode}")
    private String accessCode;

    @Autowired
    private SimulatorService simulatorService;

    @Autowired
    private PilotService pilotService;

    @Autowired
    private PilotProperties settings;

    private enum Function {
        simulator,
        pilot,
        both
    };

    /**
     * Primary entry point of Carrera Simulator
     * @param args runtime arguments
     */
    public static void main(String[] args) {

        SpringApplication.run(PilotApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {

        Options options = new Options();
        options.addOption("i", true, "Team ID");
        options.addOption("a", true, "Access code");
        options.addOption("p", true, "Protocol: any of 'memory' (default), 'rabbit', or 'ws'");
        //options.addOption("t", false, "Start Training on a simulator");
        options.addOption("d", true, "The requested race design");
        options.addOption("f", true, "either of 'simulator', 'pilot'. Defaults to 'both'. Requires rabbit");

        List<String> arglist = new ArrayList<>();
        for (String arg : args) {
            if (!arg.contains("--")) {
                arglist.add(arg);
            }
        }
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, arglist.toArray(new String[ arglist.size()]));

        String design = "Budapest";

        if ( cmd.hasOption("i")) {
            team = cmd.getOptionValue("i");
        }
        if ( cmd.hasOption("a")) {
            accessCode = cmd.getOptionValue("a");
        }
        if ( cmd.hasOption("d")) {
            design = cmd.getOptionValue("d");
        }



        Protocol protocol = Protocol.memory;
        if ( cmd.hasOption("p")) {
            protocol = Protocol.valueOf(cmd.getOptionValue("p"));
        }
        Function function = Function.both;
        if ( cmd.hasOption("f")) {
            function = Function.valueOf(cmd.getOptionValue("f"));
        }

        connectWithProtocol ( protocol, function );

        String url = relayTrainingUrl + "/" + design;
        boolean recordData = cmd.hasOption("r");

        if ( cmd.hasOption("t")) {
            try {
                String description = cmd.getOptionValue("t");
                new RestTemplate().postForObject(url, new TrainingRequest(team, accessCode, design, description, recordData ), TrainingResponse.class);
            } catch ( HttpClientErrorException hcee ) {
                System.err.println("Couldn't connect to " + url);
            }
        }
    }

    private void connectWithProtocol(Protocol protocol, Function function ) {

        switch ( protocol ) {
            case rabbit:
                // if not "only simulator", then connect the pilot
                if (!function.equals(Function.simulator)) {
                    connectPilotWithRabbit(pilotService.getPilotActor());
                }
                // if not "only pilot", then connect the simulator
                if (!function.equals(Function.pilot)) {
                    connectSimulatorWithRabbit(simulatorService.getSystem());
                }
        }
    }


    private void connectPilotWithRabbit(ActorRef pilot) {

        Client client = new RabbitClient();
        client.connect(settings.getRabbitUrl());
        Serializer serializer = new JacksonSerializer();
        PilotApi pilotApi = new PilotApiImpl(client, new PilotToRelayChannelNames(settings.getName()), serializer);

        ConnectionFactoryFromPilots factory = new RabbitConnectionFactoryFromPilots(pilotApi,
                settings.getName(), settings.getAccessCode(), settings.getRabbitUrl());

        PilotToRelayConnection pilotConnection = factory.create(
                (start)->pilot.tell(start, ActorRef.noSender()),
                (stop)->pilot.tell(stop, ActorRef.noSender()),
                (sensor)->pilot.tell(sensor, ActorRef.noSender()),
                (velo)->pilot.tell(velo, ActorRef.noSender()),
                (penalty)->pilot.tell(penalty, ActorRef.noSender()),
                (roundPassed)->pilot.tell(roundPassed, ActorRef.noSender())
        );

        pilot.tell(pilotConnection, ActorRef.noSender());
    }

    private void connectSimulatorWithRabbit (RaceTrackSimulatorSystem system ) {

        Client client = new RabbitClient();
        client.connect(settings.getRabbitUrl());
        Serializer serializer = new JacksonSerializer();
        TowardsPilotApi towardsPilotApi = new SimulatorTowardsPilotApiImpl(
                client, new PilotToRelayChannelNames(settings.getName()), serializer);

        ConnectionFactoryTowardsPilots factory = new RabbitConnectionFactoryTowardsPilots(towardsPilotApi);

        TowardsPilotsConnection towardsPilotsConnection = factory.create(system::setPower);

        system.register ( towardsPilotsConnection );
    }
}
