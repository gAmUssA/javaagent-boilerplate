package support;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.HttpWaitStrategy;

import java.time.Duration;

public class BasicTestApp<SELF extends BasicTestApp<SELF>> extends GenericContainer<SELF> {

    public BasicTestApp(String script) {
        super("zeroturnaround/groovy:2.4.5");

        withClasspathResourceMapping("agent.jar", "/agent.jar", BindMode.READ_ONLY);
        withClasspathResourceMapping(script, "/app/app.groovy", BindMode.READ_ONLY);

        // Cache Grapes
        addFileSystemBind(System.getProperty("user.home") + "/.groovy", "/root/.groovy/", BindMode.READ_WRITE);

        withEnv("JAVA_OPTS",
                new StringBuilder()
                        .append("-javaagent:/agent.jar ")
                        .append("-Dgrape.report.downloads=true -Divy.message.logger.level=2 ")
                        .toString()
        );

        withExposedPorts(4567);
        withCommand("/opt/groovy/bin/groovy /app/app.groovy");

        withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("BasicTestApp")));

        setWaitStrategy(
                new HttpWaitStrategy()
                        .forPath("/hello/")
                        .withStartupTimeout(Duration.ofMinutes(1))
        );
    }

    public String getURL() {
        return "http://" + getContainerIpAddress() + ":" + getMappedPort(4567);
    }
}