package com.graphaware.neo4j.it;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerLoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.Duration.ofSeconds;
import static java.util.Objects.requireNonNull;

public class ContainerizedCausalCluster {

    private static final Logger log = LoggerFactory.getLogger(ContainerizedCausalCluster.class);

    private final String baseFolder;
    private final Consumer<GenericContainer<?>> containerSetup;


    private Network network = Network.newNetwork();

    private GenericContainer<?> core1;
    private GenericContainer<?> core2;
    private GenericContainer<?> core3;

    public ContainerizedCausalCluster(String baseFolder, Consumer<GenericContainer<?>> containerSetup) {
        this.containerSetup = requireNonNull(containerSetup);
        requireNonNull(baseFolder);

        this.baseFolder = baseFolder.endsWith("/") ? baseFolder : baseFolder + "/";

        initCores();
    }

    private void initCores() {
        core1 = neo4jContainer("core1", network);
        core2 = neo4jContainer("core2", network);
        core3 = neo4jContainer("core3", network);
    }

    private GenericContainer<?> neo4jContainer(String name, Network network) {
        GenericContainer<?> container = new GenericContainer<>("neo4j:3.5.12-enterprise")
                .withNetworkAliases(name)
                .withNetworkMode("bridge")
                .withNetwork(network)
                .withExposedPorts(7473, 7474, 7687)
//                .withFileSystemBind(baseFolder + name + "/data", "/data")
//                .withFileSystemBind(baseFolder + name + "/logs", "/logs")
                .withFileSystemBind(baseFolder + "plugins", "/plugins")
                .withLogConsumer(new Slf4jLogConsumer(DockerLoggerFactory.getLogger(name)))
                .waitingFor(new NoWaitStrategy())
                .withEnv("NEO4J_AUTH", "none")
                .withEnv("NEO4J_dbms_mode", "CORE")
                .withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes")
                .withEnv("NEO4J_causal__clustering_minimum__core__cluster__size__at__formation", "3")
                .withEnv("NEO4J_causal__clustering_minimum__core__cluster__size__at__runtime", "3")
                .withEnv("NEO4J_causal__clustering_initial__discovery__members", "core1:5000,core2:5000,core3:5000");

        containerSetup.accept(container);
        return container;
    }

    public void startCluster() {
        createFolders("core1");
        createFolders("core2");
        createFolders("core3");

        core1.start();
        core2.start();
        core3.start();

        awaitCoreStartup(core1, ofSeconds(120));
        awaitCoreStartup(core2, ofSeconds(120));
        awaitCoreStartup(core3, ofSeconds(120));
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
        long start = System.currentTimeMillis();
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
        }

        // This expects 1 network, testcontainers can't do more than 1 currently
        String ip = container.getContainerInfo().getNetworkSettings().getNetworks().values().iterator().next().getIpAddress();
        log.info("Core available at: http://{}:7474", ip);
    }

    public void shutdownCluster() {
        core1.stop();
        core2.stop();
        core3.stop();
    }


    public List<String> logs() {
        return newArrayList(core1.getLogs(), core2.getLogs(), core3.getLogs());
    }
}
