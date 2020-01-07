package com.graphaware.neo4j.it;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.FrameConsumerResultCallback;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerLoggerFactory;
import org.testcontainers.utility.LogUtils;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.Duration.ofSeconds;
import static java.util.Objects.requireNonNull;
import static org.testcontainers.containers.output.OutputFrame.OutputType.STDERR;
import static org.testcontainers.containers.output.OutputFrame.OutputType.STDOUT;

public class ContainerizedCausalCluster {

    private static final Logger log = LoggerFactory.getLogger(ContainerizedCausalCluster.class);

    private static final int BOLT_PORT = 7687;
    private static final int HTTPS_PORT = 7473;
    private static final int HTTP_PORT = 7474;
    private static final int DEBUG_PORT = 5005;

    private final String baseFolder;
    private final Consumer<GenericContainer<?>> containerSetup;

    private Network network = Network.newNetwork();

    private List<GenericContainer<?>> cores = new ArrayList<>();

    private Driver driver;

    public ContainerizedCausalCluster(String baseFolder, Consumer<GenericContainer<?>> containerSetup) {
        this.containerSetup = requireNonNull(containerSetup);
        requireNonNull(baseFolder);

        this.baseFolder = baseFolder.endsWith("/") ? baseFolder : baseFolder + "/";

        initCores();
    }

    private void initCores() {
        // We want to number cores from 1 to avoid issues with local 7474 etc ports, add a dummy null core to 0
        cores.add(null);

        cores.add(neo4jContainer(1, network));
        cores.add(neo4jContainer(2, network));
        cores.add(neo4jContainer(3, network));
    }

    private GenericContainer<?> neo4jContainer(int number, Network network) {
        String name = "core" + number;

        GenericContainer<?> container = new GenericContainer<>("neo4j:3.5.13-enterprise")
                .withNetworkAliases(name)
                .withNetworkMode("bridge")
                .withNetwork(network)
//                .withFileSystemBind(baseFolder + name + "/data", "/data")
//                .withFileSystemBind(baseFolder + name + "/logs", "/logs")
                .withFileSystemBind(baseFolder + "plugins", "/plugins")
                .withLogConsumer(new Slf4jLogConsumer(DockerLoggerFactory.getLogger(name)))
                .waitingFor(new NoWaitStrategy())
                .withEnv("NEO4J_AUTH", "none")
                .withEnv("NEO4J_dbms_mode", "CORE")
                .withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes")

//                .withEnv("NEO4J_causal__clustering_leader__election__timeout", "1s") // Should speedup elections
//                .withEnv("NEO4J_causal__clustering_handshake__timeout", "5s") // Should speedup elections

                /*
                port 5005 is not exposed in this image by default, there is some bug in the testcontainers library which prevents this to be exposed using the portBinding (see below)
                You can use custom image, see the Dockerfile in resources folder
                Build with
                $ docker build . --tag neo4j:3.5.13-enterprise-custom
                 */
//                .withEnv("NEO4J_dbms_jvm_additional", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005")
                .withEnv("NEO4J_causal__clustering_minimum__core__cluster__size__at__formation", "3")
                .withEnv("NEO4J_causal__clustering_minimum__core__cluster__size__at__runtime", "3")
                .withEnv("NEO4J_causal__clustering_initial__discovery__members", "core1:5000,core2:5000,core3:5000");

        portBinding(number, container);
        containerSetup.accept(container);
        return container;
    }

    private void portBinding(int number, GenericContainer<?> container) {
        final int http = HTTP_PORT + 10_000 * number;
        int https = HTTPS_PORT + 10_000 * number;
        int bolt = BOLT_PORT + 10_000 * number;
        int debug = DEBUG_PORT + 10_000 * number;
        List<String> bindings = newArrayList(
                http + ":" + HTTP_PORT,
                https + ":" + HTTPS_PORT,
                bolt + ":" + BOLT_PORT,
                debug + ":" + DEBUG_PORT
                );

        container.setPortBindings(bindings);
        container.withEnv("NEO4J_dbms_connector_bolt_advertised__address", "localhost:" + bolt);
    }

    public void startCluster() {
        createFolders("core1");
        createFolders("core2");
        createFolders("core3");

        for (int i = 1; i < cores.size(); i++) {
            cores.get(i).start();
        }

        for (int i = 1; i < cores.size(); i++) {
            awaitCoreStartup(cores.get(i), ofSeconds(120));
        }


        Config config = Config.builder().withConnectionLivenessCheckTimeout(1, TimeUnit.MILLISECONDS)
                .build();
        driver = GraphDatabase.driver(boltRoutingUrl(), config);
    }

    private void createFolders(String name) {
        File data = new File("target/" + name + "/data");
        data.mkdirs();
        data.setWritable(true, false);
        File logs = new File("target/" + name + "/logs");
        logs.mkdirs();
        logs.setWritable(true, false);
    }

    private void awaitCoreStartup(GenericContainer<?> container, Duration timeout) {
        /*long start = System.currentTimeMillis();
        while (true) {
            String logs = container.getLogs();

            if (logs.contains("Remote interface available at")) {
                break;
            }

            if (System.currentTimeMillis() - start > timeout.toMillis()) {
                throw new RuntimeException("core didn't start in time");
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interupted while waiting for core to come up", e);
            }
        }*/

       new HttpWaitStrategy()
               .forPort(HTTP_PORT)
               .waitUntilReady(container);

        // This expects 1 network, testcontainers can't do more than 1 currently
        String ip = container.getContainerInfo().getNetworkSettings().getNetworks().values().iterator().next().getIpAddress();
        log.info("Core available at: http://{}:7474", ip);
    }

    public void shutdownCluster() {
        try {
            driver.close();
        } catch (Exception e) {
            // do nothing here, need to stop the cores as well
        } finally {
            driver = null;
        }

        for (int i = 1; i < cores.size(); i++) {
            cores.get(i).stop();
        }
    }

    /**
     * Returns list of logs from the containers, each item contains full log of a container from the start
     */
    public List<String> logs() {
        List<String> logs = new ArrayList<>();
        for (int i = 1; i < cores.size(); i++) {
            logs.add(cores.get(i).getLogs());
        }
        return logs;
    }

    /**
     * Returns bolt+routing://... url with ip and mapped port of one of the instances, use this to instantiate
     * the driver to access whole cluster
     */
    public String boltRoutingUrl() {
        return "bolt+routing://" + cores.get(1).getContainerIpAddress() + ":" + cores.get(1).getMappedPort(BOLT_PORT);
    }

    public Driver getDriver() {
        return driver;
    }

    /**
     * Return core number which is currently a leader
     */
    public int leader() {
        try (Session session = driver.session()) {
            // Use write transaction which goes to a leader
            String address = session.writeTransaction(tx -> {
                return tx.run("CALL dbms.cluster.overview() YIELD role,addresses " +
                        "WITH role,addresses WHERE role = 'LEADER' " +
                        "UNWIND addresses AS address " +
                        "WITH address WHERE address STARTS WITH 'bolt' " +
                        "RETURN address").single().get("address").asString();
            });

            // This is a bit of a hack - the address is bolt://localhost:N7687
            return Integer.parseInt(address.substring(17, 18));
        } catch (NoSuchRecordException e) {
            return -1;
        }
    }

    public void restartLeader() {
        int leaderNumber = leader();

        log.info("Restarting leader, current leader={}", leaderNumber);

        GenericContainer<?> leader = cores.get(leaderNumber);
        leader.getDockerClient().restartContainerCmd(leader.getContainerId()).exec();

        while (true) {
            leaderNumber = leader();
            if (leaderNumber != -1) {
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        log.info("New leader={}", leaderNumber);
    }

    public void restart() {
        log.info("Restarting cluster");
        for (int i = 1; i < cores.size(); i++) {
            GenericContainer<?> core = cores.get(i);
            core.getDockerClient().restartContainerCmd(core.getContainerId()).exec();
            followOutput(core.getDockerClient(), core.getContainerId(), new Slf4jLogConsumer(DockerLoggerFactory.getLogger("core" + i)));
        }

        for (int i = 1; i < cores.size(); i++) {
            awaitCoreStartup(cores.get(i), Duration.ofSeconds(120));
        }
        final int leader = leader();
        log.info("Cluster restarted, leader={}", leader);
    }

    public void restartCore(int number) {
        int leader = leader();
        log.info("Restarting core={}, current leader={}", number, leader);

        final GenericContainer<?> core = cores.get(number);
        core.getDockerClient().restartContainerCmd(core.getContainerId()).exec();
        followOutput(core.getDockerClient(), core.getContainerId(), new Slf4jLogConsumer(DockerLoggerFactory.getLogger("core" + number)));

        awaitCoreStartup(core, Duration.ofSeconds(120));
        leader = leader();
        log.info("Restarted core={}, current leader={}", number, leader);
    }

    private static void followOutput(DockerClient dockerClient, String containerId, Consumer<OutputFrame> consumer) {
        OutputFrame.OutputType[] types = {STDOUT, STDOUT};
        final LogContainerCmd cmd = dockerClient.logContainerCmd(containerId).withFollowStream(true)
                .withSince((int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        final FrameConsumerResultCallback callback = new FrameConsumerResultCallback();
        for (OutputFrame.OutputType type : types) {
            callback.addConsumer(type, consumer);
            if (type == STDOUT) cmd.withStdOut(true);
            if (type == STDERR) cmd.withStdErr(true);
        }
        cmd.exec(callback);
    }
}
